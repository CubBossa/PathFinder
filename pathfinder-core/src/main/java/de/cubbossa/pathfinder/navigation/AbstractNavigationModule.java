package de.cubbossa.pathfinder.navigation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtension;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.event.EventCancelledException;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.event.PathFinderReloadEvent;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.group.FindDistanceModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.group.PermissionModifier;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.util.EdgeBasedGraphEntrySolver;
import de.cubbossa.pathfinder.util.ExtensionPoint;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbstractNavigationModule<PlayerT>
    extends PathFinderExtensionBase
    implements PathFinderExtension, NavigationModule<PlayerT> {

  @Getter
  private final NamespacedKey key = AbstractPathFinder.pathfinder("navigation");

  protected final PathFinder pathFinder;
  protected EventDispatcher<PlayerT> eventDispatcher;

  protected Cache<UUID, Navigator> navigators;

  protected final Map<UUID, Navigation<PlayerT>> activeFindCommandPaths;
  protected final List<NavigationConstraint> navigationConstraints;

  public AbstractNavigationModule() {
    NavigationModuleProvider.set(this);

    this.activeFindCommandPaths = new HashMap<>();
    this.pathFinder = PathFinder.get();
    this.pathFinder.getDisposer().register(this.pathFinder, this);
    this.navigationConstraints = new ArrayList<>();

    navigators = Caffeine.newBuilder().build();

    ExtensionPoint<NavigationConstraint> extensionPoint = new ExtensionPoint<>(NavigationConstraint.class);
    extensionPoint.getExtensions().forEach(this::registerNavigationConstraint);

    setEdgeEntrySolver();
  }

  private void setEdgeEntrySolver() {
    RouteImpl.DEFAULT_GRAPH_ENTRY_SOLVER = new EdgeBasedGraphEntrySolver(
        pathFinder.getConfiguration().getNavigation().getFindLocation().getMaxDistance(),
        pathFinder.getConfiguration().getNavigation().getExternalNodeEdgeConnectionWeight()
    );
    RouteImpl.DEFAULT_CONNECTION_DISTANCE = (double) pathFinder.getConfiguration().getNavigation()
        .getFindLocation().getMaxDistance();
  }

  public void onLoad(PathFinder pathPlugin) {

    if (!pathFinder.getConfiguration().getModuleConfig().isNavigationModule()) {
      disable();
    }
    this.eventDispatcher = (EventDispatcher<PlayerT>) pathFinder.getEventDispatcher();
  }

  public void onEnable(PathFinder pathPlugin) {

    this.eventDispatcher.listen(this, PathFinderReloadEvent.class, e -> {
      if (e.reloadsConfig()) {
        setEdgeEntrySolver();
      }
    });

    registerNavigationConstraint((playerId, scope) -> {
      PathPlayer<?> player = PathPlayer.wrap(playerId);
      Map<Node, Collection<NodeGroup>> groups = PathFinder.get().getStorage().loadGroupsOfNodes(scope).join();

      if (player.unwrap() == null) {
        return new HashSet<>();
      }

      return groups.entrySet().stream()
          .filter(e -> {
            return e.getValue().stream().allMatch(group -> {
              Optional<PermissionModifier> mod = group.getModifier(PermissionModifier.KEY);
              return mod.isEmpty() || player.hasPermission(mod.get().permission());
            });
          })
          .map(Map.Entry::getKey)
          .collect(Collectors.toList());
    });
  }

  @Override
  public void dispose() {
    NavigationModuleProvider.set(null);
  }

  @Override
  public CompletableFuture<Navigation<PlayerT>> navigate(PathPlayer<PlayerT> viewer, Route route) {
    return CompletableFuture.supplyAsync(() -> {

      VisualizerPath<PlayerT> path;
      try {
        path = navigators.get(viewer.getUniqueId(), uuid -> new NavigatorImpl(c -> {
          Collection<Node> nodes = c;
          for (NavigationConstraint navigationConstraint : navigationConstraints) {
            nodes = navigationConstraint.filterTargetNodes(uuid, nodes);
          }
          return nodes;
        })).createRenderer(viewer, route);
      } catch (NoPathFoundException noPathFoundException) {
        throw new CompletionException(noPathFoundException);
      }
      NavigationLocation start = route.getStart();
      NavigationLocation end = route.getEnd().stream().filter(l -> l.getNode().equals(path.getPath().get(path.getPath().size() - 1))).findAny().orElseThrow();

      if (!eventDispatcher.dispatchPathStart(viewer, path)) {
        throw new EventCancelledException();
      }
      return new NavigationImpl(start, end, path);
    });
  }

  @Override
  public void registerNavigationConstraint(NavigationConstraint filter) {
    navigationConstraints.add(filter);
  }

  @Override
  public boolean canNavigateTo(UUID uuid, Node node, Collection<Node> scope) {
    return applyNavigationConstraints(uuid, scope).contains(node);
  }

  @Override
  public Collection<Node> applyNavigationConstraints(UUID player, Collection<Node> nodes) {
    Collection<Node> nodeSet = new HashSet<>(nodes);
    for (NavigationConstraint f : navigationConstraints) {
      nodeSet = f.filterTargetNodes(player, nodeSet);
    }
    return nodeSet;
  }

  @Override
  public @Nullable Navigation<PlayerT> getActiveFindCommandPath(@NotNull PathPlayer<PlayerT> player) {
    return activeFindCommandPaths.get(player.getUniqueId());
  }

  @Override
  public CompletableFuture<Navigation<PlayerT>> setFindCommandPath(PathPlayer<PlayerT> viewer, Route route) {
    var current = activeFindCommandPaths.get(viewer.getUniqueId());
    if (current != null) {
      current.cancel();
    }
    return navigate(viewer, route).thenApply(n -> {
      activeFindCommandPaths.put(viewer.getUniqueId(), n);
      return n;
    });
  }

  @Getter
  @Accessors(fluent = true)
  class NavigationImpl implements Navigation<PlayerT>, Listener {

    private final NavigationLocation startLocation;
    private final NavigationLocation endLocation;
    private final VisualizerPath<PlayerT> renderer;
    private Double rangeSquared = null;

    private HashSet<Runnable> onEnd = null;
    private HashSet<Runnable> onCancel = null;
    private HashSet<Runnable> onComplete = null;

    public NavigationImpl(
        NavigationLocation start,
        NavigationLocation end,
        VisualizerPath<PlayerT> path
    ) {
      this.startLocation = start;
      this.endLocation = end;
      this.renderer = path;

      PathFinder.get().getDisposer().register(this, renderer);
    }

    @Override
    public void dispose() {
      if (rangeSquared != null) {
        PlayerMoveEvent.getHandlerList().unregister(this);
      }
    }

    @EventHandler
    void onMove(PlayerMoveEvent e) {
      if (e.getPlayer().getUniqueId().equals(viewer().getUniqueId())
          && viewer().getLocation().distanceSquared(endLocation.getNode().getLocation()) < rangeSquared) {
        complete();
      }
    }

    @Override
    public PathPlayer<PlayerT> viewer() {
      return renderer.getTargetViewer();
    }

    @Override
    public List<Location> pathControlPoints() {
      return renderer.getPath().stream().map(Node::getLocation).toList();
    }

    private void stop() {
      eventDispatcher.dispatchPathStopped(viewer(), renderer());
      renderer.stopUpdater();
      renderer.removeAllViewers();
      PathFinder.get().getDisposer().dispose(this);
    }

    @Override
    public void complete() {
      if (!eventDispatcher.dispatchPathTargetReached(viewer(), renderer())) {
        return;
      }
      stop();
      if (onComplete != null) {
        onComplete.forEach(Runnable::run);
      }
      if (onEnd != null) {
        onEnd.forEach(Runnable::run);
      }
    }

    @Override
    public void cancel() {
      if (!eventDispatcher.dispatchPathCancel(viewer(), renderer())) {
        return;
      }
      stop();
      if (onCancel != null) {
        onCancel.forEach(Runnable::run);
      }
      if (onEnd != null) {
        onEnd.forEach(Runnable::run);
      }
    }

    @Override
    public Navigation<PlayerT> persist() {
      return this;
    }

    public Navigation<PlayerT> cancelWhenTargetInRange() {
      double dist = 1.5;

      if (endLocation.getNode() instanceof GroupedNode gn) {
        NodeGroup highest = gn.groups().stream()
            .filter(g -> g.hasModifier(FindDistanceModifier.KEY))
            .max(NodeGroup::compareTo).orElse(null);

        if (highest != null) {
          dist = highest.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY)
              .map(FindDistanceModifier::distance)
              .orElse(1.5);
        }
      }
      return cancelWhenTargetInRange(dist);
    }

    @Override
    public Navigation<PlayerT> cancelWhenTargetInRange(double range) {
      if (rangeSquared == null) {
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("PathFinder"));
      }
      rangeSquared = range * range;
      return this;
    }

    @Override
    public void onEnd(Runnable runnable) {
      if (onEnd == null) {
        onEnd = new HashSet<>();
      }
      onEnd.add(runnable);
    }

    @Override
    public void onComplete(Runnable runnable) {
      if (onComplete == null) {
        onComplete = new HashSet<>();
      }
      onComplete.add(runnable);
    }

    @Override
    public void onCancel(Runnable runnable) {
      if (onCancel == null) {
        onCancel = new HashSet<>();
      }
      onCancel.add(runnable);
    }

    @Override
    public String toString() {
      return "NavigationImpl{" +
          "startLocation=" + startLocation +
          ", endLocation=" + endLocation +
          ", renderer=" + renderer +
          '}';
    }
  }
}