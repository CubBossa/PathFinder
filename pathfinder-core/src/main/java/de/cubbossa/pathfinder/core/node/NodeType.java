package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.Named;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.data.NodeDataStorage;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import jdk.jshell.spi.ExecutionControl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public abstract class NodeType<N extends Node<N>> implements Keyed, Named, NodeDataStorage<N> {

  public record NodeCreationContext(Location location) {
  }

  private final NamespacedKey key;
  private final ItemStack displayItem;
  private final MiniMessage miniMessage;
  private String nameFormat;
  private Component displayName;

  private NodeDataStorage<N> storage;

  public NodeType(NamespacedKey key, String name, ItemStack displayItem, MiniMessage miniMessage) {
    this(key, name, displayItem, miniMessage, null);
  }

  public NodeType(NamespacedKey key, String name, ItemStack displayItem, MiniMessage miniMessage, NodeDataStorage<N> storage) {
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
  public CompletableFuture<N> createNodeInStorage(NodeCreationContext context) {
    return storage.createNodeInStorage(context);
  }

  @Override
  public CompletableFuture<N> getNodeFromStorage(UUID id) {
    return storage.getNodeFromStorage(id);
  }

  @Override
  public CompletableFuture<Collection<N>> getNodesFromStorage(NodeSelection ids) {
    return storage.getNodesFromStorage(ids);
  }

  @Override
  public CompletableFuture<Collection<N>> getNodesFromStorage() {
    return storage.getNodesFromStorage();
  }

  @Override
  public CompletableFuture<Void> updateNodeInStorage(UUID id, Consumer<N> nodeConsumer) {
    return storage.updateNodeInStorage(id, nodeConsumer);
  }

  @Override
  public CompletableFuture<Void> updateNodesInStorage(NodeSelection nodeIds,
                                                      Consumer<N> nodeConsumer) {
    return storage.updateNodesInStorage(nodeIds, nodeConsumer);
  }

  @Override
  public CompletableFuture<Void> deleteNodesFromStorage(NodeSelection nodes) {
    return storage.deleteNodesFromStorage(nodes);
  }
}
