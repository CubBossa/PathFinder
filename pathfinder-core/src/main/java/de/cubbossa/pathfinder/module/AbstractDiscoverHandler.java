package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.group.PermissionModifier;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.nodegroup.modifier.CommonDiscoverableModifier;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AbstractDiscoverHandler<PlayerT> {

  public static <T> AbstractDiscoverHandler<T> getInstance() {
    return (AbstractDiscoverHandler<T>) instance;
  }

  private static AbstractDiscoverHandler<?> instance;
  final CommonPathFinder plugin;
  final EventDispatcher<PlayerT> eventDispatcher;

  public AbstractDiscoverHandler(CommonPathFinder plugin) {
    instance = this;
    this.plugin = plugin;
    this.eventDispatcher = (EventDispatcher<PlayerT>) plugin.getEventDispatcher();

    if (!plugin.getConfiguration().moduleConfig.discoveryModule) {
      return;
    }

    if (plugin.getConfiguration().navigation.requireDiscovery) {
      AbstractNavigationHandler.getInstance().registerFindPredicate(context -> {
        Map<Node, Collection<NodeGroup>> map = PathFinderProvider.get().getStorage().loadGroupsOfNodes(context.nodes()).join();

        return map.entrySet().stream()
            .filter(e -> e.getValue().stream().allMatch(group -> {
              return !group.hasModifier(CommonDiscoverableModifier.class)
                  || !this.hasDiscovered(context.playerId(), group).join();
            }))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
      });
    }
  }

  public CompletableFuture<Collection<NodeGroup>> getFulfillingGroups(PathPlayer<?> player) {
    Storage storage = PathFinderProvider.get().getStorage();
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
    return plugin.getStorage().loadDiscoverInfo(playerId, group.getKey()).thenCompose(discoverInfo -> {
      if (discoverInfo.isPresent()) {
        return CompletableFuture.completedFuture(null);
      }

      Optional<DiscoverableModifier> discoverable = group.getModifier(DiscoverableModifier.KEY);
      if (!eventDispatcher.dispatchPlayerFindEvent(player, group, discoverable.get(), date)) {
        return CompletableFuture.completedFuture(null);
      }

      return plugin.getStorage().createAndLoadDiscoverinfo(playerId, group.getKey(), date);
    }).thenRun(() -> {
    });
  }

  public CompletableFuture<Void> forget(PathPlayer<PlayerT> player, NodeGroup group) {
    if (!group.hasModifier(CommonDiscoverableModifier.class)) {
      return CompletableFuture.completedFuture(null);
    }

    return plugin.getStorage().loadDiscoverInfo(player.getUniqueId(), group.getKey()).thenAccept(discoverInfo -> {
      if (discoverInfo.isEmpty()) {
        return;
      }
      DiscoverInfo info = discoverInfo.get();
      if (!eventDispatcher.dispatchPlayerForgetEvent(player, info.discoverable())) {
        return;
      }
      plugin.getStorage().deleteDiscoverInfo(discoverInfo.get());
    });
  }

  public CompletableFuture<Boolean> hasDiscovered(UUID playerId, NodeGroup group) {
    return plugin.getStorage().loadDiscoverInfo(playerId, group.getKey())
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
}
