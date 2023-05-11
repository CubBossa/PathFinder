package de.cubbossa.pathfinder.events;

import de.cubbossa.pathapi.event.Listener;
import de.cubbossa.pathapi.event.*;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.events.node.NodeDeletedEvent;
import de.cubbossa.pathfinder.events.node.NodeSavedEvent;
import de.cubbossa.pathfinder.events.nodegroup.GroupCreatedEvent;
import de.cubbossa.pathfinder.events.nodegroup.GroupDeleteEvent;
import de.cubbossa.pathfinder.events.path.PathStartEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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

@RequiredArgsConstructor
public class BukkitEventDispatcher implements EventDispatcher<Player> {

  private final Map<Listener<?>, org.bukkit.event.Listener> listenerMap = new HashMap<>();
  private final Map<Class<? extends PathFinderEvent>, Class<? extends Event>> classMapping = Map.of(
      NodeCreateEvent.class, NodeCreatedEvent.class,
      NodeSaveEvent.class, NodeSavedEvent.class,
      NodeDeleteEvent.class, NodeDeletedEvent.class,
      NodeGroupCreateEvent.class, GroupCreatedEvent.class,
      NodeGroupDeleteEvent.class, GroupDeleteEvent.class
  );
  private final Logger logger;

  private void log(String message) {
    logger.log(Level.INFO, message);
  }

  private boolean dispatchEvent(Event event) {
    try {
      CompletableFuture<Boolean> future = dispatchEventWithFuture(event);
      return future.get(500, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new RuntimeException("Calling event " + event.getEventName() + " took more than 500 milli seconds - skipping.", e);
    }
  }

  private CompletableFuture<Boolean> dispatchEventWithFuture(Event event) {
    log("Dispatching Event '" + event.getClass().getSimpleName() + "'.");
    if (Bukkit.isPrimaryThread()) {
      return CompletableFuture.completedFuture(dispatchEventInMainThread(event));
    }
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    JavaPlugin pl = PathFinderPlugin.getInstance();
    new BukkitRunnable() {
      @Override
      public void run() {
        boolean cancelled = dispatchEventInMainThread(event);
        future.complete(cancelled);
      }
    }.runTask(PathFinderPlugin.getInstance());
    Bukkit.getScheduler().scheduleSyncDelayedTask(pl, () -> {

    });
    return future;
  }

  private boolean dispatchEventInMainThread(Event event) {
    Bukkit.getPluginManager().callEvent(event);
    return event instanceof Cancellable cancellable && cancellable.isCancelled();
  }

  @Override
  public <N extends Node> void dispatchNodeCreate(N node) {
    dispatchEvent(new NodeCreatedEvent<>(node));
  }

  @Override
  public void dispatchNodeSave(Node node) {
    dispatchEvent(new NodeSavedEvent(node));
  }

  @Override
  public void dispatchNodeLoad(Node node) {
  }

  @Override
  public <N extends Node> void dispatchNodeDelete(N node) {
    dispatchEvent(new NodeDeletedEvent(node));
  }

  @Override
  public void dispatchNodesDelete(Collection<Node> nodes) {
    for (Node node : nodes) {
      dispatchEvent(new NodeDeletedEvent(node));
    }
  }

  @Override
  public void dispatchNodeUnassign(Node node, Collection<NodeGroup> groups) {

  }

  @Override
  public void dispatchNodeAssign(Node node, Collection<NodeGroup> groups) {

  }

  @Override
  public void dispatchGroupCreate(NodeGroup group) {
    dispatchEvent(new GroupCreatedEvent(group));
  }

  @Override
  public void dispatchGroupDelete(NodeGroup group) {
    dispatchEvent(new GroupDeleteEvent(group));
  }

  @Override
  public boolean dispatchPathStart(PathPlayer<Player> player, VisualizerPath<Player> path, Location target, float findDistanceRadius) {
    return dispatchEvent(new PathStartEvent(player, path, target, findDistanceRadius));
  }

  @SneakyThrows
  @Override
  public <E extends PathFinderEvent> Listener<E> listen(Class<E> eventType,
                                                        Consumer<? super E> event) {
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
        PathFinderPlugin.getInstance()
    );
    Listener<E> internalListener = new Listener<>(UUID.randomUUID(), eventType, event);
    listenerMap.put(internalListener, listener);
    return internalListener;
  }

  @SneakyThrows
  @Override
  public void drop(Listener<?> listener) {
    HandlerList handlerList = (HandlerList) listener.eventType().getMethod("getHandlerList")
        .invoke(classMapping.get(listener.eventType()));
    handlerList.unregister(listenerMap.remove(listener));
  }
}
