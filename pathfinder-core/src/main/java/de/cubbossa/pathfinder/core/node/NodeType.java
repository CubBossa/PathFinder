package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.data.NodeDataStorage;
import jdk.jshell.spi.ExecutionControl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class NodeType<N extends Node<N>> implements Keyed, Named, NodeDataStorage<N> {

  public record NodeCreationContext(Location location, boolean persistent) {
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

  @Override
  public Map<Integer, N> loadNodes(Collection<NodeGroup> groups) {
    return null;
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
