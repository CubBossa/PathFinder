package de.cubbossa.pathfinder.core;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.NodeCreateEvent;
import de.cubbossa.pathfinder.core.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupCreateEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.data.ApplicationLayer;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.util.Collection;
import java.util.List;
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
    CompletableFuture<Void> future = new CompletableFuture<>();
    if (Bukkit.isPrimaryThread()) {
      Bukkit.getPluginManager().callEvent(event);
      if (event instanceof Cancellable cancellable && cancellable.isCancelled()) {
        future.completeExceptionally(new RuntimeException("Event cancelled."));
        return future;
      }
      future.complete(null);
      return future;
    }
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
      future.thenCombine(callEvent(event), (a, b) -> a);
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
        .thenApply(n -> {
          callEvent(new NodeCreatedEvent<>(n));
          return n;
        })
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
  }

  @Override
  public CompletableFuture<Void> deleteNodes(NodeSelection nodes) {

    return getNodes(nodes)
        .thenAccept(n -> {
          super.deleteNodes(nodes)
              .thenCombine(callEvent(new NodesDeletedEvent(n)), (a, b) -> a)
              .join();
        });
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
}
