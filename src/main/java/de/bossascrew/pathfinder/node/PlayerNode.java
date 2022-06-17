package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.roadmap.RoadMap;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Getter
public class PlayerNode implements Node {

    private final Player player;

    public PlayerNode(Player player) {
        this.player = player;
    }

    @Override
    public Collection<String> getSearchTerms() {
        return new HashSet<>();
    }

    @Override
    public int getNodeId() {
        return -1;
    }

    @Override
    public NamespacedKey getRoadMapKey() {
        return null;
    }

    @Override
    public String getNameFormat() {
        return "";
    }

    @Override
    public void setNameFormat(String format) {

    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public Vector getPosition() {
        return player.getLocation().toVector();
    }

    @Override
    public void setPosition(Vector position) {

    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public Collection<Edge> getEdges() {

        RoadMap roadMap;

        Vector pos = player.getLocation().toVector();
        List<Node> other = roadMap.getNodes().stream().sorted(Comparator.comparingDouble(n -> n.getPosition().distance(pos)));
        other.forEach(node -> );

        return null;
    }

    @Override
    public @Nullable NamespacedKey getGroupKey() {
        return null;
    }

    @Override
    public void setGroupKey(@Nullable NamespacedKey key) {

    }

    @Override
    public @Nullable String getPermission() {
        return null;
    }

    @Override
    public void setPermission(@Nullable String permission) {

    }

    @Override
    public @Nullable Double getBezierTangentLength() {
        return null;
    }

    @Override
    public void setBezierTangentLength(Double value) {

    }

    @Override
    public Edge connect(Node target) {
        return null;
    }

    @Override
    public void disconnect(Node target) {

    }

    @Override
    public int compareTo(@NotNull Node o) {
        return 0;
    }
}
