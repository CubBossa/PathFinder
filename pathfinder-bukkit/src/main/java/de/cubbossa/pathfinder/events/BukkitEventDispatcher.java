package de.cubbossa.pathfinder.events;

import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.event.Listener;
import de.cubbossa.pathfinder.event.NodeCreateEvent;
import de.cubbossa.pathfinder.event.NodeDeleteEvent;
import de.cubbossa.pathfinder.event.NodeGroupCreateEvent;
import de.cubbossa.pathfinder.event.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.event.NodeGroupSaveEvent;
import de.cubbossa.pathfinder.event.NodeSaveEvent;
import de.cubbossa.pathfinder.event.PathCancelledEvent;
import de.cubbossa.pathfinder.event.PathFinderEvent;
import de.cubbossa.pathfinder.event.PathStoppedEvent;
import de.cubbossa.pathfinder.event.PathTargetReachedEvent;
import de.cubbossa.pathfinder.event.PlayerDiscoverLocationEvent;
import de.cubbossa.pathfinder.event.PlayerDiscoverProgressEvent;
import de.cubbossa.pathfinder.event.PlayerForgetLocationEvent;
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
import de.cubbossa.pathfinder.group.DiscoverProgressModifier;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
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
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

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

    classMapping.put(de.cubbossa.pathfinder.event.PathStartEvent.class, PathStartEvent.class);
    classMapping.put(PathCancelledEvent.class, PathCancelEvent.class);
    classMapping.put(PathStoppedEvent.class, PathStopEvent.class);
    classMapping.put(PathTargetReachedEvent.class, PathTargetFoundEvent.class);

    classMapping.put(PlayerDiscoverLocationEvent.class, PlayerDiscoverEvent.class);
    classMapping.put(PlayerDiscoverProgressEvent.class, de.cubbossa.pathfinder.events.discovering.PlayerDiscoverProgressEvent.class);
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
  public boolean dispatchPlayerFindProgressEvent(PathPlayer<Player> player, NodeGroup found, NodeGroup observer) {
    dispatchEvent(new de.cubbossa.pathfinder.events.discovering.PlayerDiscoverProgressEvent(player, found, observer));
    return true;
  }

  @Override
  public boolean dispatchPlayerFindEvent(PathPlayer<Player> player, NodeGroup group, DiscoverableModifier modifier, LocalDateTime findDate) {
    if (dispatchEvent(new PlayerDiscoverEvent(player, group, modifier, findDate))) {
      PathFinder.get().getStorage().loadGroups(group).thenAccept(uuidCollectionMap -> {
        uuidCollectionMap.values()
            .stream().flatMap(Collection::stream)
            .distinct()
            .filter(g -> g.hasModifier(DiscoverProgressModifier.KEY))
            .forEach(g -> dispatchPlayerFindProgressEvent(player, group, g));
      });
      return true;
    }
    return false;
  }

  @Override
  public boolean dispatchPlayerForgetEvent(PathPlayer<Player> player, NamespacedKey group) {
    NodeGroup g = PathFinder.get().getStorage().loadGroup(group).join().orElseThrow();
    DiscoverableModifier modifier = g.<DiscoverableModifier>getModifier(DiscoverableModifier.KEY).orElseThrow();
    return dispatchEvent(new PlayerForgetEvent(player, g, modifier));
  }

  @Override
  public boolean dispatchVisualizerChangeEvent(PathVisualizer<?, ?> visualizer) {
    return true;
  }

  @Override
  public boolean dispatchPathStart(PathPlayer<Player> player, VisualizerPath<Player> path) {
    return dispatchEvent(new PathStartEvent(player, path));
  }

  @Override
  public boolean dispatchPathTargetReached(PathPlayer<Player> player, VisualizerPath<Player> path) {
    return dispatchEvent(new PathTargetFoundEvent(player, path));
  }

  @Override
  public void dispatchPathStopped(PathPlayer<Player> player, VisualizerPath<Player> path) {

  }

  @Override
  public boolean dispatchPathCancel(PathPlayer<Player> player, VisualizerPath<Player> path) {
    return dispatchEvent(new PathCancelEvent(player, path));
  }

  @SneakyThrows
  @Override
  public <E extends PathFinderEvent> Listener<E> listen(Class<E> eventType, Consumer<? super E> event) {
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
    // Remove listener if exists.
    org.bukkit.event.Listener bukkitListener = listenerMap.remove(listener);
    if (bukkitListener == null) {
      return;
    }

    Class<? extends Event> bukkitEventClass = classMapping.get(listener.eventType());
    Method handlerListMethod;
    try {
      handlerListMethod = bukkitEventClass.getMethod("getHandlerList");
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Bukkit Event class '" + bukkitEventClass.getName() + "' does not implement mandatory method getHandlerList.");
    }
    HandlerList handlerList = (HandlerList) handlerListMethod.invoke(bukkitEventClass);
    handlerList.unregister(bukkitListener);
  }
}
