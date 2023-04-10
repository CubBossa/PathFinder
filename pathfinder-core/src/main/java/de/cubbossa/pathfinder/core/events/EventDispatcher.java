package de.cubbossa.pathfinder.core.events;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class EventDispatcher {

  private void dispatchEvent(Event event) {
    try {
      dispatchEventWithFuture(event).get(1, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private CompletableFuture<Void> dispatchEventWithFuture(Event event) {
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
      dispatchEventWithFuture(event).join();
      future.complete(null);
    });
    return future;
  }

  private <T> CompletableFuture<T> eventCancelled() {
    return CompletableFuture.failedFuture(new RuntimeException("Event cancelled"));
  }

  public <N extends Node<N>> void dispatchNodeCreate(N node) {

  }

  public <N extends Node<N>> void dispatchNodeDelete(Node<?> node) {

  }

  public <N extends Node<N>> void dispatchNodesDelete(Collection<UUID> nodes) {

  }
}
