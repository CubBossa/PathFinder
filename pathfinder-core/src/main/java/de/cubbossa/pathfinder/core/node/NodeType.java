package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.storage.NodeDataStorage;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

@Getter
@Setter
public abstract class NodeType<N extends Node<N>> implements Keyed, Named, NodeDataStorage<N> {

  private final NamespacedKey key;
  private final ItemStack displayItem;
  private final MiniMessage miniMessage;
  private String nameFormat;
  private Component displayName;

  private Optional<NodeDataStorage<N>> storage;

  public NodeType(NamespacedKey key, String name, ItemStack displayItem, MiniMessage miniMessage) {
    this(key, name, displayItem, miniMessage, null);
  }

  public NodeType(NamespacedKey key, String name, ItemStack displayItem, MiniMessage miniMessage, NodeDataStorage<N> storage) {
    this.key = key;
    this.miniMessage = miniMessage;
    this.setNameFormat(name);
    this.displayItem = displayItem;
    this.storage = storage == null ? Optional.empty() : Optional.of(storage);
  }

  @Override
  public void setNameFormat(String name) {
    this.nameFormat = name;
    this.displayName = miniMessage.deserialize(name);
  }

  // pass to storage methods.

  @Override
  public Optional<N> loadNode(UUID uuid) {
    return storage.isPresent() ? storage.get().loadNode(uuid) : Optional.empty();
  }

  @Override
  public Collection<N> loadNodes(Collection<UUID> ids) {
    return storage.isPresent() ? storage.get().loadNodes(ids) : new HashSet<>();
  }

  @Override
  public Collection<N> loadAllNodes() {
    return storage.isPresent() ? storage.get().loadAllNodes() : new HashSet<>();
  }

  @Override
  public void saveNode(N node) {
    storage.ifPresent(s -> s.saveNode(node));
  }

  @Override
  public void deleteNode(N node) {
    storage.ifPresent(s -> s.deleteNode(node));
  }
}
