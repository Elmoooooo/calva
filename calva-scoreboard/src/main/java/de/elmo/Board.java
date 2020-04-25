package de.elmo;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Board {

    private Board(String title, HashMap<Integer, List<String>> lines) {
        this.title = title;
        this.lines = lines;
    }

    public static Board of(String title) {
        return new Board(title, Maps.newHashMap());
    }

    private final String title;

    private final HashMap<Integer, List<String>> lines;

    private int getLowestIndex() {
        return this.lines.keySet().stream().reduce(Integer::min).orElse(10);
    }


    private void insertLine(int index) {
        if (this.lines.containsKey(index)) {
            return;
        }
        this.lines.put(index, Lists.newArrayList());
    }


    public Board append(String line) {
        int index = getLowestIndex() - 1;

        insertLine(index);
        this.lines.get(index)
                .add(line);

        return this;
    }

    public Board append(List<String> lines) {
        lines.forEach(this::append);
        return this;
    }

    public Board line(int index, String text) {
        insertLine(index);
        this.lines.get(index)
                .add(text);
        return this;
    }

    public Board clearLines() {
        this.lines.clear();
        return this;
    }


    public void sendScoreboard(Player player) {


        Scoreboard board = new Scoreboard();
        ScoreboardObjective objective = board.registerObjective(this.title, IScoreboardCriteria.b);

        PacketPlayOutScoreboardObjective removeScoreboardPacket = new PacketPlayOutScoreboardObjective(objective, 1);
        PacketPlayOutScoreboardObjective createScoreboardPacket = new PacketPlayOutScoreboardObjective(objective, 0);

        PacketPlayOutScoreboardDisplayObjective displayScoreboardPacket = new PacketPlayOutScoreboardDisplayObjective(1, objective);

        objective.setDisplayName(this.title);


        Objects.requireNonNull((((CraftPlayer) player).getHandle()).playerConnection);
        Stream.of(new Packet[]{removeScoreboardPacket, createScoreboardPacket, displayScoreboardPacket})
                .forEach((((CraftPlayer) player).getHandle()).playerConnection::sendPacket);


        this.lines.keySet().forEach(key -> this.lines.get(key).forEach(scoreLine -> {
            ScoreboardScore score = new ScoreboardScore(board, objective, scoreLine);
            score.setScore(key);
            PacketPlayOutScoreboardScore scoreboardScore = new PacketPlayOutScoreboardScore(score);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(scoreboardScore);
        }));

    }


}
