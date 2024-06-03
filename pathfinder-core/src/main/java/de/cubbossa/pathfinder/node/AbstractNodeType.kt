package de.cubbossa.pathfinder.node;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.storage.NodeStorageImplementation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public abstract class AbstractNodeType<N extends Node> implements NodeType<N> {

  private final NamespacedKey key;
  protected NodeStorageImplementation<N> storage;

  public AbstractNodeType(NamespacedKey key) {
    this(key, null);
  }

  public AbstractNodeType(NamespacedKey key, NodeStorageImplementation<N> storage) {
    this.key = key;
    this.storage = storage;
  }

  public abstract N createNodeInstance(Context context);

  @Override
  public @Nullable N createAndLoadNode(Context context) {
    N node = createNodeInstance(context);
    saveNode(node);
    return node;
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
