package de.cubbossa.pathfinder.navigation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtension;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.event.EventCancelledException;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.group.FindDistanceModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.group.PermissionModifier;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class AbstractNavigationModule<PlayerT>
    extends PathFinderExtensionBase
    implements PathFinderExtension, NavigationModule<PlayerT> {

  @Getter
  private final NamespacedKey key = AbstractPathFinder.pathfinder("navigation");

  protected final PathFinder pathFinder;
  protected EventDispatcher<PlayerT> eventDispatcher;

  protected Cache<UUID, Navigator> navigators;

  protected final Map<UUID, NavigationContext> activePaths;
  protected final List<NavigationConstraint> navigationConstraints;

  public AbstractNavigationModule() {
    NavigationModuleProvider.set(this);

    this.activePaths = new HashMap<>();
    this.pathFinder = PathFinder.get();
    this.pathFinder.getDisposer().register(this.pathFinder, this);
    this.navigationConstraints = new ArrayList<>();

    navigators = Caffeine.newBuilder()
        .maximumSize(128)
        .build();

    ExtensionPoint<NavigationConstraint> extensionPoint = new ExtensionPoint<>(NavigationConstraint.class);
    extensionPoint.getExtensions().forEach(this::registerNavigationConstraint);

    NavigationLocationImpl.GRAPH_ENTRY_SOLVER = new EdgeBasedGraphEntrySolver();
  }

  public void onLoad(PathFinder pathPlugin) {

    if (!pathFinder.getConfiguration().getModuleConfig().isNavigationModule()) {
      disable();
    }
    this.eventDispatcher = (EventDispatcher<PlayerT>) pathFinder.getEventDispatcher();
  }

  public void onEnable(PathFinder pathPlugin) {

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
  public CompletableFuture<VisualizerPath<PlayerT>> navigate(PathPlayer<PlayerT> viewer, Route route) {
    return CompletableFuture.supplyAsync(() -> {

      var current = activePaths.get(viewer.getUniqueId());
      if (current != null) {
        unset(current);
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
        throw new CompletionException(noPathFoundException);
      }

      NavigationContext ctx;
      try {
        ctx = context(viewer.getUniqueId(), path);
      } catch (IllegalStateException t) {
        throw new CompletionException(new NoPathFoundException());
      }

      boolean result = eventDispatcher.dispatchPathStart(viewer, path);
      if (!result) {
        throw new EventCancelledException();
      }

      activePaths.put(viewer.getUniqueId(), ctx);
      return path;
    });
  }

  private NavigationContext context(UUID playerId, VisualizerPath<PlayerT> path) {

    if (path.getPath().isEmpty()) {
      throw new IllegalStateException("Path containing no nodes");
    }
    Node last = path.getPath().get(path.getPath().size() - 1);
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
      PathFinder.get().getDisposer().dispose(context.path);
    }
    context.path.removeViewer(PathPlayer.wrap(context.playerId));
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
  public VisualizerPath<PlayerT> getActivePath(final @NotNull PathPlayer<PlayerT> player) {
    var ctx = activePaths.get(player.getUniqueId());
    return ctx == null ? null : ctx.path;
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