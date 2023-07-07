package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.FindDistanceModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.group.PermissionModifier;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.nodegroup.modifier.CommonDiscoverableModifier;
import de.cubbossa.pathfinder.storage.StorageUtil;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

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
        Collection<NodeGroup> groups = StorageUtil.getGroups(context.node());

        for (NodeGroup group : groups) {
          if (!group.hasModifier(CommonDiscoverableModifier.class)) {
            continue;
          }
          if (!hasDiscovered(context.playerId(), group).join()) {
            return false;
          }
        }
        return true;
      });
    }
  }

  public boolean fulfillsDiscoveringRequirements(NodeGroup group, PathPlayer<?> player) {
    if (!group.hasModifier(DiscoverableModifier.class)) {
      return false;
    }
    Optional<PermissionModifier> perm = group.getModifier(PermissionModifier.KEY);
    if (perm.isPresent() && !player.hasPermission(perm.get().permission())) {
      return false;
    }
    // TODO join performance issues
    for (Node node : group.resolve().join()) {
      if (node == null) {
        plugin.getLogger().log(Level.SEVERE, "Node is null"); // TODO
        continue;
      }
      if (!Objects.equals(node.getLocation().getWorld(), player.getLocation().getWorld())) {
        continue;
      }
      float dist = getDiscoveryDistance(player.getUniqueId(), node);
      if (node.getLocation().getX() - player.getLocation().getX() > dist
          || node.getLocation().getY() - player.getLocation().getY() > dist) {
        continue;
      }
      if (node.getLocation().distanceSquared(player.getLocation()) > Math.pow(dist, 2)) {
        continue;
      }
      return true;
    }
    return false;
  }

  public void discover(PathPlayer<PlayerT> player, NodeGroup group, LocalDateTime date) {
    if (!group.hasModifier(DiscoverableModifier.KEY)) {
      return;
    }
    UUID playerId = player.getUniqueId();
    plugin.getStorage().loadDiscoverInfo(playerId, group.getKey()).thenAccept(discoverInfo -> {
      if (discoverInfo.isPresent()) {
        return;
      }

      Optional<DiscoverableModifier> discoverable = group.getModifier(DiscoverableModifier.KEY);
      if (!eventDispatcher.dispatchPlayerFindEvent(player, group, discoverable.get(), date)) {
        return;
      }

      plugin.getStorage().createAndLoadDiscoverinfo(playerId, group.getKey(), date);
    });
  }

  public void forget(PathPlayer<PlayerT> player, NodeGroup group) {
    if (!group.hasModifier(CommonDiscoverableModifier.class)) {
      return;
    }

    plugin.getStorage().loadDiscoverInfo(player.getUniqueId(), group.getKey()).thenAccept(discoverInfo -> {
      if (discoverInfo.isEmpty()) {
        return;
      }
      DiscoverInfo info = discoverInfo.get();
      if (!eventDispatcher.dispatchPlayerForgetEvent(player, info.discoverable(), info.foundDate())) {
        return;
      }
      plugin.getStorage().deleteDiscoverInfo(discoverInfo.get());
    });
  }

  public CompletableFuture<Boolean> hasDiscovered(UUID playerId, NodeGroup group) {
    return plugin.getStorage().loadDiscoverInfo(playerId, group.getKey())
        .thenApply(Optional::isPresent);
  }

  public float getDiscoveryDistance(UUID playerId, Node node) {
    FindDistanceModifier mod = StorageUtil.getGroups(node).stream()
        .filter(group -> group.hasModifier(FindDistanceModifier.KEY))
        .sorted()
        .findFirst()
        .map(group -> group.<FindDistanceModifier>getModifier(FindDistanceModifier.KEY).get())
        .orElse(null);
    return mod == null ? 1.5f : (float) mod.distance();
  }
}
