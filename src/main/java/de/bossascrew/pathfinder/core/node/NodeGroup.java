package de.bossascrew.pathfinder.core.node;

import de.bossascrew.pathfinder.Named;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.core.node.implementation.Waypoint;
import de.bossascrew.pathfinder.core.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.module.discovering.DiscoverHandler;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
@Setter
public class NodeGroup extends HashSet<Node> implements Keyed, Named, Discoverable, Navigable {

    private final NamespacedKey key;
    private String nameFormat;
    private Component displayName;
    private boolean discoverable;
    private Collection<String> searchTerms;

    public NodeGroup(NamespacedKey key, String nameFormat) {
        this(key, nameFormat, new HashSet<>());
    }

    public NodeGroup(NamespacedKey key, String nameFormat, Collection<Waypoint> nodes) {
        super(nodes);
        this.key = key;
        this.setNameFormat(nameFormat);
        this.discoverable = false;
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

    public void removeSearchTerms(Collection<String> terms) {
        searchTerms.removeAll(terms);
    }

    public void addSearchTerms(Collection<String> terms) {
        searchTerms.addAll(terms);
    }

    public void clearSearchTerms(Collection<String> terms) {
        searchTerms.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeGroup)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NodeGroup group = (NodeGroup) o;

        return key.equals(group.key);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public boolean fulfillsDiscoveringRequirements(Player player) {
        for (Node node : this) {
            if(node == null) {
                PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Node is null");
                continue;
            }
            float dist = DiscoverHandler.getInstance().getDiscoveryDistance(player.getUniqueId(), RoadMapHandler.getInstance().getRoadMap(node.getRoadMapKey()));
            if (node.getPosition().getX() - player.getLocation().getX() > dist) {
                continue;
            }
            if (node.getPosition().distance(player.getLocation().toVector()) > dist) {
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    public NamespacedKey getUniqueKey() {
        return key;
    }
}
