package de.cubbossa.pathfinder.discovery;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtension;
import de.cubbossa.pathfinder.PathFinderExtensionBase;
import de.cubbossa.pathfinder.event.EventDispatcher;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.group.FindDistanceModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.group.PermissionModifier;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.NavigationModule;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.nodegroup.modifier.DiscoverableModifierImpl;
import de.cubbossa.pathfinder.storage.DiscoverInfo;
import de.cubbossa.pathfinder.storage.StorageAdapter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AbstractDiscoveryModule<PlayerT>
    extends PathFinderExtensionBase implements PathFinderExtension, Disposable {

  public static <T> AbstractDiscoveryModule<T> getInstance() {
    return (AbstractDiscoveryModule<T>) instance;
  }

  private static AbstractDiscoveryModule<?> instance;

  private final NamespacedKey key = AbstractPathFinder.pathfinder("discovery");

  final PathFinder pathFinder;
  final EventDispatcher<PlayerT> eventDispatcher;

  public AbstractDiscoveryModule() {
    instance = this;
    this.pathFinder = PathFinder.get();
    this.pathFinder.getDisposer().register(this.pathFinder, this);
    this.eventDispatcher = (EventDispatcher<PlayerT>) pathFinder.getEventDispatcher();

    if (!pathFinder.getConfiguration().getModuleConfig().isDiscoveryModule()) {
      return;
    }

    if (pathFinder.getConfiguration().getNavigation().isRequireDiscovery()) {
      NavigationModule.get().registerNavigationConstraint((playerId, scope) -> {
        Map<Node, Collection<NodeGroup>> map = PathFinder.get().getStorage().loadGroupsOfNodes(scope).join();

        return map.entrySet().stream()
            .filter(e -> e.getValue().stream().allMatch(group -> {
              return !group.hasModifier(DiscoverableModifierImpl.class)
                  || !this.hasDiscovered(playerId, group).join();
            }))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
      });
    }
  }

  @Override
  public void dispose() {
    instance = null;
  }

  public CompletableFuture<Collection<NodeGroup>> getFulfillingGroups(PathPlayer<?> player) {
    StorageAdapter storage = PathFinder.get().getStorage();
    return storage.loadGroups(DiscoverableModifier.KEY).thenCompose(groups -> {
      Collection<UUID> allNodes = groups.stream().flatMap(Collection::stream).toList();
      if (allNodes.isEmpty()) {
        return CompletableFuture.completedFuture(new HashSet<>());
      }
      return storage.loadNodes(allNodes).thenCompose(nodes -> {
        return storage.loadGroups(allNodes).thenApply(nodeGroupMap -> {
          Map<UUID, Node> nodeMap = new HashMap<>();
          nodes.forEach(node -> nodeMap.put(node.getNodeId(), node));

          return groups.stream()
              .filter(group -> {
                Optional<PermissionModifier> perm = group.getModifier(PermissionModifier.KEY);
                return perm.isEmpty() || player.hasPermission(perm.get().permission());
              })
              .filter(group -> {
                return group.stream().anyMatch(uuid -> {
                  Location location = nodeMap.get(uuid).getLocation();
                  if (!Objects.equals(player.getLocation().getWorld(), location.getWorld())) {
                    return false;
                  }
                  float dist = getDiscoveryDistance(nodeGroupMap.get(uuid));
                  if (location.getX() - player.getLocation().getX() > dist
                      || location.getY() - player.getLocation().getY() > dist) {
                    return false;
                  }
                  return !(location.distanceSquared(player.getLocation()) > Math.pow(dist, 2));
                });
              })
              .collect(Collectors.toSet());
        });
      });
    });
  }

  public CompletableFuture<Void> discover(PathPlayer<PlayerT> player, NodeGroup group, LocalDateTime date) {
    if (!group.hasModifier(DiscoverableModifier.KEY)) {
      return CompletableFuture.completedFuture(null);
    }
    UUID playerId = player.getUniqueId();
    return pathFinder.getStorage().loadDiscoverInfo(playerId, group.getKey()).thenCompose(discoverInfo -> {
      if (discoverInfo.isPresent()) {
        return CompletableFuture.completedFuture(null);
      }

      Optional<DiscoverableModifier> discoverable = group.getModifier(DiscoverableModifier.KEY);
      if (!eventDispatcher.dispatchPlayerFindEvent(player, group, discoverable.get(), date)) {
        return CompletableFuture.completedFuture(null);
      }

      return pathFinder.getStorage().createAndLoadDiscoverinfo(playerId, group.getKey(), date);
    }).thenRun(() -> {
    });
  }

  public CompletableFuture<Void> forget(PathPlayer<PlayerT> player, NodeGroup group) {
    if (!group.hasModifier(DiscoverableModifierImpl.class)) {
      return CompletableFuture.completedFuture(null);
    }

    return pathFinder.getStorage().loadDiscoverInfo(player.getUniqueId(), group.getKey()).thenAccept(discoverInfo -> {
      if (discoverInfo.isEmpty()) {
        return;
      }
      DiscoverInfo info = discoverInfo.get();
      if (!eventDispatcher.dispatchPlayerForgetEvent(player, info.discoverable())) {
        return;
      }
      pathFinder.getStorage().deleteDiscoverInfo(discoverInfo.get());
    });
  }

  public CompletableFuture<Boolean> hasDiscovered(UUID playerId, NodeGroup group) {
    return pathFinder.getStorage().loadDiscoverInfo(playerId, group.getKey())
        .thenApply(Optional::isPresent);
  }

  public float getDiscoveryDistance(Collection<NodeGroup> groups) {
    FindDistanceModifier mod = groups.stream()
        .filter(group -> group.hasModifier(FindDistanceModifier.KEY))
        .sorted()
        .findFirst()
        .map(group -> group.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY).get())
        .orElse(null);
    return mod == null ? 1.5f : (float) mod.distance();
  }

  @Override
  public NamespacedKey getKey() {
    return key;
  }
}
