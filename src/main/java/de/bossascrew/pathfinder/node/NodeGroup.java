package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.Named;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.HashSet;

@Getter
@Setter
public class NodeGroup extends HashSet<Node> implements Keyed, Named, Findable, Navigable {

    private final NamespacedKey key;
    private final RoadMap roadMap;
    private String nameFormat;
    private Component displayName;
    private boolean findable;
    private Collection<String> searchTerms;

    public NodeGroup(NamespacedKey key, RoadMap roadMap, String nameFormat) {
        this(key, roadMap, nameFormat, new HashSet<>());
    }

    public NodeGroup(NamespacedKey key, RoadMap roadMap, String nameFormat, Collection<Waypoint> nodes) {
        super(nodes);
        this.key = key;
        this.roadMap = roadMap;
        this.setNameFormat(nameFormat);
        this.findable = false;
        this.searchTerms = new HashSet<>();
    }

    public void setNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
        this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(nameFormat);
    }

    @Override
    public Collection<Node> getGroup() {
        return new HashSet<>(this);
    }
}
