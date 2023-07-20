package de.cubbossa.pathfinder.events;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.Listener;
import de.cubbossa.pathapi.event.*;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.events.discovering.PlayerDiscoverEvent;
import de.cubbossa.pathfinder.events.discovering.PlayerForgetEvent;
import de.cubbossa.pathfinder.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.events.node.NodeDeletedEvent;
import de.cubbossa.pathfinder.events.node.NodeSavedEvent;
import de.cubbossa.pathfinder.events.nodegroup.GroupCreatedEvent;
import de.cubbossa.pathfinder.events.nodegroup.GroupDeleteEvent;
import de.cubbossa.pathfinder.events.nodegroup.GroupSaveEvent;
import de.cubbossa.pathfinder.events.path.PathCancelEvent;
import de.cubbossa.pathfinder.events.path.PathStartEvent;
import de.cubbossa.pathfinder.events.path.PathStopEvent;
import de.cubbossa.pathfinder.events.path.PathTargetFoundEvent;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitEventDispatcher implements EventDispatcher<Player> {

  private final Map<Listener<?>, org.bukkit.event.Listener> listenerMap = new HashMap<>();
  private final Map<Class<? extends PathFinderEvent>, Class<? extends Event>> classMapping = new HashMap<>();
  private final Logger logger;

  public BukkitEventDispatcher(Logger logger) {
    this.logger = logger;
    setEventMapping();
  }

  private void setEventMapping() {
    classMapping.clear();
    classMapping.put(NodeCreateEvent.class, NodeCreatedEvent.class);
    classMapping.put(NodeSaveEvent.class, NodeSavedEvent.class);
    classMapping.put(NodeDeleteEvent.class, NodeDeletedEvent.class);

    classMapping.put(NodeGroupCreateEvent.class, GroupCreatedEvent.class);
    classMapping.put(NodeGroupDeleteEvent.class, GroupDeleteEvent.class);
    classMapping.put(NodeGroupSaveEvent.class, GroupSaveEvent.class);

    classMapping.put(de.cubbossa.pathapi.event.PathStartEvent.class, PathStartEvent.class);
    classMapping.put(PathCancelledEvent.class, PathCancelEvent.class);
    classMapping.put(PathStoppedEvent.class, PathStopEvent.class);
    classMapping.put(PathTargetReachedEvent.class, PathTargetFoundEvent.class);

    classMapping.put(PlayerDiscoverLocationEvent.class, PlayerDiscoverEvent.class);
    classMapping.put(PlayerForgetLocationEvent.class, PlayerForgetEvent.class);
  }

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
    if (Bukkit.isPrimaryThread()) {
      return CompletableFuture.completedFuture(dispatchEventInMainThread(event));
    }
    return CompletableFuture.supplyAsync(() -> dispatchEventInMainThread(event), BukkitPathFinder.mainThreadExecutor());
  }

  private boolean dispatchEventInMainThread(Event event) {
    Bukkit.getPluginManager().callEvent(event);
    return !(event instanceof Cancellable cancellable && cancellable.isCancelled());
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
  public void dispatchGroupSave(NodeGroup group) {
    dispatchEvent(new GroupSaveEvent(group));
  }

  @Override
  public boolean dispatchPlayerFindEvent(PathPlayer<Player> player, NodeGroup group, DiscoverableModifier modifier, LocalDateTime findDate) {
    return dispatchEvent(new PlayerDiscoverEvent(player, group, modifier, findDate));
  }

  @Override
  public boolean dispatchPlayerForgetEvent(PathPlayer<Player> player, NamespacedKey group) {
    NodeGroup g = PathFinderProvider.get().getStorage().loadGroup(group).join().orElseThrow();
    DiscoverableModifier modifier = g.<DiscoverableModifier>getModifier(DiscoverableModifier.KEY).orElseThrow();
    return dispatchEvent(new PlayerForgetEvent(player, g, modifier));
  }

  @Override
  public boolean dispatchVisualizerChangeEvent(PathVisualizer<?, ?> visualizer) {
    return true;
  }

  @Override
  public boolean dispatchPathStart(PathPlayer<Player> player, VisualizerPath<Player> path, Location target, float findDistanceRadius) {
    return dispatchEvent(new PathStartEvent(player, path, target, findDistanceRadius));
  }

  @Override
  public boolean dispatchPathTargetReached(PathPlayer<Player> player, VisualizerPath<Player> path) {
    return dispatchEvent(new PathTargetFoundEvent(player, path));
  }

  @Override
  public void dispatchPathStopped(PathPlayer<Player> player, VisualizerPath<Player> path, Location target, float distance) {

  }

  @Override
  public boolean dispatchPathCancel(PathPlayer<Player> player, VisualizerPath<Player> path) {
    return dispatchEvent(new PathCancelEvent(player, path));
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
            throw new RuntimeException("Error in event listener for event " + eventType, e.getCause());
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
