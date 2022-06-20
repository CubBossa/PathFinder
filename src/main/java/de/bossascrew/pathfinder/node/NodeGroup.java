package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.roadmap.RoadMap;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Getter
@Setter
public class NodeGroup extends HashSet<Node> implements Keyed, Findable {

    private final NamespacedKey key;
    private final RoadMap roadMap;
    private String nameFormat;
    private Component displayName;
    private boolean findable;
    private Collection<String> searchTerms;

    public NodeGroup(NamespacedKey key, RoadMap roadMap, String nameFormat) {
        this(key, roadMap, nameFormat, null);
    }

    public NodeGroup(NamespacedKey key, RoadMap roadMap, String nameFormat, Collection<Waypoint> nodes) {
        super(nodes);
        this.key = key;
        this.roadMap = roadMap;
        this.nameFormat = nameFormat;
        this.findable = false;
    }

    @Override
    public RoadMap getRoadMap() {
        return roadMap;
    }

    @Override
    public Collection<Node> getGroup() {
        return new HashSet<>(this);
    }
}
