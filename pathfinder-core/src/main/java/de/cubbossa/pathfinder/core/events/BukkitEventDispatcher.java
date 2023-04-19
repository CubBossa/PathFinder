package de.cubbossa.pathfinder.core.events;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.event.EventDispatcher;
import de.cubbossa.pathfinder.api.event.Listener;
import de.cubbossa.pathfinder.api.event.NodeCreateEvent;
import de.cubbossa.pathfinder.api.event.NodeDeleteEvent;
import de.cubbossa.pathfinder.api.event.NodeSaveEvent;
import de.cubbossa.pathfinder.api.event.PathFinderEvent;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.core.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeDeletedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeSavedEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
public class BukkitEventDispatcher implements EventDispatcher {

  private final Map<Listener<?>, org.bukkit.event.Listener> listenerMap = new HashMap<>();
  private final Map<Class<? extends PathFinderEvent>, Class<? extends Event>> classMapping = Map.of(
     NodeCreateEvent.class, NodeCreatedEvent.class,
     NodeSaveEvent.class, NodeSavedEvent.class,
     NodeDeleteEvent.class, NodeDeletedEvent.class
  );
  private final Logger logger;

  private void log(String message) {
    logger.log(Level.INFO, message);
  }

  private void dispatchEvent(Event event) {
    try {
      dispatchEventWithFuture(event).get(1, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private CompletableFuture<Void> dispatchEventWithFuture(Event event) {
    log("Dispatching Event '" + event.getClass().getSimpleName() + "'.");
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
    return future.exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  @Override
  public <N extends Node<N>> void dispatchNodeCreate(N node) {
    dispatchEvent(new NodeCreatedEvent<>(node));
  }

  @Override
  public void dispatchSaveNode(Node<?> node) {

  }

  @Override
  public void dispatchLoadNode(Node<?> node) {

  }

  @Override
  public <N extends Node<N>> void dispatchNodeDelete(N node) {
    dispatchEvent(new NodeDeletedEvent(node));
  }

  @Override
  public void dispatchNodesDelete(Collection<Node<?>> nodes) {
    for (Node<?> node : nodes) {
      dispatchEvent(new NodeDeletedEvent(node));
    }
  }

  @Override
  public void dispatchNodeUnassign(Node<?> node, Collection<NodeGroup> groups) {

  }

  @Override
  public void dispatchNodeAssign(Node<?> node, Collection<NodeGroup> groups) {

  }

  @SneakyThrows
  @Override
  public <E extends PathFinderEvent> Listener<E> listen(Class<E> eventType, Consumer<? super E> event) {
    log("Registering Event Listener for '" + eventType.getSimpleName() + "'.");
    org.bukkit.event.Listener listener = new org.bukkit.event.Listener() {
      @EventHandler
      public void onEvent(Event e) {
        event.accept((E) e);
      }
    };
    Method method = listener.getClass().getMethod("onEvent", Event.class);
    Bukkit.getPluginManager().registerEvent(
        classMapping.get(eventType), listener, EventPriority.HIGHEST,
        (listener1, event1) -> {
          try {
            method.invoke(listener1, event1);
          } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        },
        PathPlugin.getInstance()
    );
    Listener<E> internalListener = new Listener<>(UUID.randomUUID(), eventType, event);
    listenerMap.put(internalListener, listener);
    return internalListener;
  }

  @SneakyThrows
  @Override
  public void drop(Listener<?> listener) {
    HandlerList handlerList = (HandlerList) listener.eventType().getMethod("getHandlerList").invoke(classMapping.get(listener.eventType()));
    handlerList.unregister(listenerMap.remove(listener));
  }
}
