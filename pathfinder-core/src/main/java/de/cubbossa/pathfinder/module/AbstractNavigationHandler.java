package de.cubbossa.pathfinder.module;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtension;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.group.FindDistanceModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.group.PermissionModifier;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.NavigationConstraint;
import de.cubbossa.pathfinder.navigation.NavigationHandler;
import de.cubbossa.pathfinder.navigation.Navigator;
import de.cubbossa.pathfinder.navigation.NavigatorImpl;
import de.cubbossa.pathfinder.navigation.Route;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.NodeType;
import de.cubbossa.pathfinder.util.ExtensionPoint;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Getter;

public class AbstractNavigationHandler<PlayerT>
    extends PathFinderExtensionBase
    implements PathFinderExtension, NavigationHandler<PlayerT> {

  @Getter
  private static AbstractNavigationHandler<?> instance;

  @Getter
  private final NamespacedKey key = AbstractPathFinder.pathfinder("navigation");

  protected final PathFinder pathFinder;
  protected EventDispatcher<PlayerT> eventDispatcher;

  protected Cache<UUID, Navigator> navigators;

  protected final Map<UUID, NavigationContext> activePaths;
  protected final List<NavigationConstraint> navigationConstraints;

  public AbstractNavigationHandler() {
    instance = this;
    this.activePaths = new HashMap<>();
    this.pathFinder = PathFinderProvider.get();
    this.pathFinder.getDisposer().register(this.pathFinder, this);
    this.navigationConstraints = new ArrayList<>();

    navigators = Caffeine.newBuilder()
        .maximumSize(128)
        .build();

    ExtensionPoint<NavigationConstraint> extensionPoint = new ExtensionPoint<>(NavigationConstraint.class);
    extensionPoint.getExtensions().forEach(this::registerNavigationConstraint);
  }

  public void onLoad(PathFinder pathPlugin) {

    if (!pathFinder.getConfiguration().getModuleConfig().isNavigationModule()) {
      disable();
    }
    this.eventDispatcher = (EventDispatcher<PlayerT>) pathFinder.getEventDispatcher();
  }

  public void onEnable(PathFinder pathPlugin) {

    registerNavigationConstraint((playerId, scope) -> {
      PathPlayer<?> player = AbstractPathFinder.getInstance().wrap(playerId);
      Map<Node, Collection<NodeGroup>> groups = PathFinderProvider.get().getStorage().loadGroupsOfNodes(scope).join();

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
    instance = null;
  }

  @Override
  public CompletableFuture<VisualizerPath<PlayerT>> navigate(PathPlayer<PlayerT> viewer, Route route) {

    var current = activePaths.get(viewer.getUniqueId());
    if (current != null) {
      cancel(current.playerId);
    }

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
      // TODO message
      return null;
    }
    boolean result = eventDispatcher.dispatchPathStart(viewer, path);
    if (!result) {
      // TODO message
      return null;
    }

    path.startUpdater(1000);

    activePaths.put(viewer.getUniqueId(), context(viewer.getUniqueId(), path));
    return null;
  }

  private NavigationContext context(UUID playerId, VisualizerPath<PlayerT> path) {

    Node last = path.getPath().get(0);
    if (last == null) {
      throw new IllegalStateException("Path containing no nodes");
    }

    double dist = 1.5;

    if (last instanceof GroupedNode gn) {
      NodeGroup highest = gn.groups().stream()
          .filter(g -> g.hasModifier(FindDistanceModifier.KEY))
          .max(NodeGroup::compareTo).orElse(null);

      if (highest != null) {
        dist = highest.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY)
            .map(FindDistanceModifier::distance)
            .orElse(1.5);
      }
    }
    return new NavigationContext(playerId, path, last, dist);
  }

  private void unset(NavigationContext context) {
    if (activePaths.remove(context.playerId) != null) {
      eventDispatcher.dispatchPathStopped(PathPlayer.wrap(context.playerId), context.path);
    }
  }

  @Override
  public void unset(UUID viewer) {
    var context = activePaths.get(viewer);
    if (context != null) {
      unset(context);
    }
  }

  @Override
  public void cancel(UUID viewer) {
    var context = activePaths.get(viewer);
    if (context == null) {
      return;
    }
    var path = context.path();
    if (!eventDispatcher.dispatchPathCancel(path.getTargetViewer(), path)) {
      return;
    }
    unset(context);
  }

  @Override
  public void reach(UUID viewer) {
    var context = activePaths.get(viewer);
    if (context == null) {
      return;
    }
    var path = context.path();
    if (!eventDispatcher.dispatchPathTargetReached(path.getTargetViewer(), path)) {
      return;
    }
    unset(context);
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
  public VisualizerPath<PlayerT> getActivePath(PathPlayer<PlayerT> player) {
    return activePaths.get(player.getUniqueId()).path;
  }

  public void cancelPathWhenTargetReached(VisualizerPath<PlayerT> path) {

  }

  protected final class NavigationContext {
    private final UUID playerId;
    private final VisualizerPath<PlayerT> path;
    private final Node target;
    private final double dist;

    private NavigationContext(UUID playerId, VisualizerPath<PlayerT> path, Node target, double dist) {
      this.playerId = playerId;
      this.path = path;
      this.target = target;
      this.dist = dist;
    }

    public UUID playerId() {
      return playerId;
    }

    public VisualizerPath<PlayerT> path() {
      return path;
    }

    public Node target() {
      return target;
    }

    public double dist() {
      return dist;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null || obj.getClass() != this.getClass()) {
        return false;
      }
      var that = (NavigationContext) obj;
      return Objects.equals(this.playerId, that.playerId) &&
          Objects.equals(this.path, that.path) &&
          Objects.equals(this.target, that.target) &&
          Double.doubleToLongBits(this.dist) == Double.doubleToLongBits(that.dist);
    }

    @Override
    public int hashCode() {
      return Objects.hash(playerId, path, target, dist);
    }

    @Override
    public String toString() {
      return "NavigationContext[" +
          "playerId=" + playerId + ", " +
          "path=" + path + ", " +
          "target=" + target + ", " +
          "dist=" + dist + ']';
    }
  }
}