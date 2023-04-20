package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.NodeDataStorage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import de.cubbossa.pathapi.misc.NamespacedKey;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public abstract class AbstractNodeType<N extends Node<N>> implements
		NodeType<N> {

  private final NamespacedKey key;
  private final ItemStack displayItem;
  private final MiniMessage miniMessage;
  private final NodeDataStorage<N> storage;
  private String nameFormat;
  private Component displayName;

  public AbstractNodeType(NamespacedKey key, String name, ItemStack displayItem, MiniMessage miniMessage) {
    this(key, name, displayItem, miniMessage, null);
  }

  public AbstractNodeType(NamespacedKey key, String name, ItemStack displayItem, MiniMessage miniMessage, NodeDataStorage<N> storage) {
    this.key = key;
    this.miniMessage = miniMessage;
    this.setNameFormat(name);
    this.displayItem = displayItem;
    this.storage = storage;
  }

  @Override
  public void setNameFormat(String name) {
    this.nameFormat = name;
    this.displayName = miniMessage.deserialize(name);
  }

  // pass to storage methods.

  @Override
  public Optional<N> loadNode(UUID uuid) {
    return storage != null ? storage.loadNode(uuid) : Optional.empty();
  }

  @Override
  public Collection<N> loadNodes(Collection<UUID> ids) {
    return storage != null ? storage.loadNodes(ids) : new HashSet<>();
  }

  @Override
  public Collection<N> loadAllNodes() {
    return storage != null ? storage.loadAllNodes() : new HashSet<>();
  }

  @Override
  public void saveNode(N node) {
    if (storage != null) {
      storage.saveNode(node);
    }
  }

  @Override
  public void deleteNode(N node) {
    if (storage != null) {
      storage.deleteNode(node);
    }
  }
}
