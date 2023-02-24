package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.data.NodeDataStorage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class NodeType<N extends Node<N>> implements Keyed, Named, NodeDataStorage<N> {

  public record NodeCreationContext(RoadMap roadMap, int id, Location location,
                                    boolean persistent) {
  }

  private final NamespacedKey key;
  private final ItemStack displayItem;
  private String nameFormat;
  private Component displayName;

  private NodeDataStorage<N> storage;

  public NodeType(NamespacedKey key, String name, ItemStack displayItem) {
    this(key, name, displayItem, null);
  }

  public NodeType(NamespacedKey key, String name, ItemStack displayItem,
                  NodeDataStorage<N> storage) {
    this.key = key;
    this.setNameFormat(name);
    this.displayItem = displayItem;
    this.storage = storage;
  }

  @Override
  public void setNameFormat(String name) {
    this.nameFormat = name;
    this.displayName = PathPlugin.getInstance().getMiniMessage().deserialize(name);
  }

  public abstract N createNode(NodeCreationContext context);

  public Map<Integer, N> loadNodes(RoadMap roadMap) {
    if (storage != null) {
      return storage.loadNodes(roadMap);
    }
    return new HashMap<>();
  }

  @Override
  public void updateNode(N node) {
    if (storage != null) {
      storage.updateNode(node);
    }
  }

  public void deleteNodes(Collection<Integer> nodeIds) {
    if (storage != null) {
      storage.deleteNodes(nodeIds);
    }
  }
}
