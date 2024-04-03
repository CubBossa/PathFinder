package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.NodeStorageImplementation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Getter
@Setter
public abstract class AbstractNodeType<N extends Node> implements
    NodeType<N> {

  private final NamespacedKey key;
  private final MiniMessage miniMessage = MiniMessage.miniMessage();
  protected NodeStorageImplementation<N> storage;
  private String nameFormat;
  private Component displayName;

  public AbstractNodeType(NamespacedKey key, String name) {
    this(key, name, null);
  }

  public AbstractNodeType(NamespacedKey key, String name, NodeStorageImplementation<N> storage) {
        this.key = key;
        this.setNameFormat(name);
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
    return getStorage() != null ? storage.loadNode(uuid) : Optional.empty();
  }

  @Override
  public Collection<N> loadNodes(Collection<UUID> ids) {
    return getStorage() != null ? storage.loadNodes(ids) : new HashSet<>();
  }

  @Override
  public Collection<N> loadAllNodes() {
    return getStorage() != null ? storage.loadAllNodes() : new HashSet<>();
  }

  @Override
  public void saveNode(N node) {
    if (getStorage() != null) {
      storage.saveNode(node);
    }
  }

  @Override
  public void deleteNodes(Collection<N> node) {
    if (getStorage() != null) {
      storage.deleteNodes(node);
    }
  }
}
