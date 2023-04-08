package de.cubbossa.pathfinder.core;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.EdgesCreateEvent;
import de.cubbossa.pathfinder.core.events.node.EdgesDeleteEvent;
import de.cubbossa.pathfinder.core.events.node.NodeCreateEvent;
import de.cubbossa.pathfinder.core.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeleteEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupCreateEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.storage.ApplicationLayer;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class EventsLayer extends PassLayer implements ApplicationLayer {

  private final ApplicationLayer subLayer;

  public EventsLayer(ApplicationLayer layer) {
    super(layer);
    this.subLayer = layer;
  }

  public MessageLayer messageLayer(CommandSender sender) {
    return new MessageLayer(sender, this);
  }

  private CompletableFuture<Void> callEvent(Event event) {
    return callEvent(event, null);
  }

  private <T> CompletableFuture<T> callEvent(Event event, T data) {
    CompletableFuture<T> future = new CompletableFuture<>();
    if (Bukkit.isPrimaryThread()) {
      Bukkit.getPluginManager().callEvent(event);
      if (event instanceof Cancellable cancellable && cancellable.isCancelled()) {
        future.completeExceptionally(new RuntimeException("Event cancelled."));
        return future;
      }
      future.complete(data);
      return future;
    }
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
      callEvent(event, data).join();
      future.complete(data);
    });
    return future;
  }

  private <T> CompletableFuture<T> eventCancelled() {
    return CompletableFuture.failedFuture(new RuntimeException("Event cancelled"));
  }

  @Override
  public <N extends Node<N>> CompletableFuture<N> createNode(NodeType<N> type,
                                                             Location location) {

    NodeCreateEvent<N> event = new NodeCreateEvent<>(type, location);
    return callEvent(event)
        .thenCompose(result -> subLayer.createNode(type, location))
        .thenCompose(n -> callEvent(new NodeCreatedEvent<>(n), n))
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
  }

  @Override
  public CompletableFuture<Void> deleteNodes(NodeSelection nodes) {

    return getNodes(nodes)
        .thenCompose(n -> callEvent(new NodesDeleteEvent(n), n))
        .thenCombine(subLayer.deleteNodes(nodes), (a, b) -> a)
        .thenCompose(n -> callEvent(new NodesDeletedEvent(n), null));
  }

  @Override
  public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
    NodeGroupCreateEvent event = new NodeGroupCreateEvent(key);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return eventCancelled();
    }
    return subLayer.createNodeGroup(key);
  }

  @Override
  public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
    NodeGroupDeleteEvent event = new NodeGroupDeleteEvent(key);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return eventCancelled();
    }
    return subLayer.deleteNodeGroup(key);
  }

  @Override
  public CompletableFuture<Void> assignNodesToGroups(Collection<NamespacedKey> groups,
                                                     NodeSelection selection) {

    NodeGroupAssignEvent e = new NodeGroupAssignEvent(selection, groups);
    return callEvent(e)
        .thenCompose(result -> super.assignNodesToGroups(e.getModifiedGroups(), selection))
        .thenApply(u -> {
          NodeSelection sel = new NodeSelection(e.getModifiedGroupables());
          callEvent(new NodeGroupAssignedEvent(sel, e.getModifiedGroups())).join();
          return u;
        });
  }

  @Override
  public CompletableFuture<Void> assignNodesToGroup(NamespacedKey group,
                                                    NodeSelection selection) {
    NodeGroupAssignEvent event = new NodeGroupAssignEvent(selection, List.of(group));
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      return eventCancelled();
    }
    NodeSelection sel = new NodeSelection(event.getModifiedGroupables());
    return super.assignNodesToGroup(event.getModifiedGroups().stream().findAny().get(), selection)
        .thenRun(() -> {
          NodeGroupAssignedEvent postEvent =
              new NodeGroupAssignedEvent(sel, event.getModifiedGroups());
          Bukkit.getPluginManager().callEvent(postEvent);
        });
  }

  @Override
  public CompletableFuture<Collection<Edge>> connectNodes(NodeSelection start, NodeSelection end) {
    return callEvent(new EdgesCreateEvent.Pre(start, end))
        .thenCombine(super.connectNodes(start, end), (a, b) -> b)
        .thenApply(e -> callEvent(new EdgesCreateEvent.Post(e), e).join());
  }

  @Override
  public CompletableFuture<Edge> connectNodes(UUID start, UUID end, double weight) {
    return callEvent(new EdgesCreateEvent.Pre(new NodeSelection(start), new NodeSelection(end)))
        .thenCombine(super.connectNodes(start, end, weight), (a, b) -> b)
        .thenApply(e -> callEvent(new EdgesCreateEvent.Post(List.of(e)), e).join());
  }

  @Override
  public CompletableFuture<Edge> connectNodes(UUID start, UUID end) {
    return callEvent(new EdgesCreateEvent.Pre(new NodeSelection(start), new NodeSelection(end)))
        .thenCombine(super.connectNodes(start, end), (a, b) -> b)
        .thenApply(e -> callEvent(new EdgesCreateEvent.Post(List.of(e)), e).join());
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(UUID start, UUID end) {
    return callEvent(new EdgesDeleteEvent.Pre(new NodeSelection(start), new NodeSelection(end)))
        .thenCompose(u -> super.disconnectNodes(start, end))
        .thenCompose(u -> callEvent(new EdgesDeleteEvent.Post(new NodeSelection(start), new NodeSelection(end))));
  }

  @Override
  public CompletableFuture<Void> disconnectNodes(NodeSelection start, NodeSelection end) {
    return callEvent(new EdgesDeleteEvent.Pre(start, end))
        .thenCompose(u -> super.disconnectNodes(start, end))
        .thenCompose(u -> callEvent(new EdgesDeleteEvent.Post(start, end)));
  }
}
