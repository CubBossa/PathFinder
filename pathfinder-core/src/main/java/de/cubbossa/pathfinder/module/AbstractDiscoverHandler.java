package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.storage.DiscoverInfo;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.nodegroup.modifier.FindDistanceModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.PermissionModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.SimpleDiscoverableModifier;

import java.time.LocalDateTime;
import java.util.Collection;
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
        if (!(context.node() instanceof Groupable groupable)) {
          return true;
        }
        Collection<NodeGroup> groups = groupable.getGroups();

        for (NodeGroup group : groups) {
          if (!group.hasModifier(SimpleDiscoverableModifier.class)) {
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
    PermissionModifier perm = group.getModifier(PermissionModifier.class);
    if (perm != null && !player.hasPermission(perm.permission())) {
      return false;
    }
    for (Node node : group.resolve().join()) {
      if (node == null) {
        plugin.getLogger().log(Level.SEVERE, "Node is null"); // TODO
        continue;
      }
      float dist = getDiscoveryDistance(player.getUniqueId(), node);
      if (node.getLocation().getX() - player.getLocation().getX() > dist) {
        continue;
      }
      if (node.getLocation().distance(player.getLocation()) > dist) {
        continue;
      }
      return true;
    }
    return false;
  }

  public void discover(PathPlayer<PlayerT> player, NodeGroup group, LocalDateTime date) {
    if (!group.hasModifier(SimpleDiscoverableModifier.class)) {
      return;
    }
    UUID playerId = player.getUniqueId();
    plugin.getStorage().loadDiscoverInfo(playerId, group.getKey()).thenAccept(discoverInfo -> {
      if (discoverInfo.isPresent()) {
        return;
      }

      DiscoverableModifier discoverable = group.getModifier(SimpleDiscoverableModifier.class);

      if (!eventDispatcher.dispatchPlayerFindEvent(player, group, discoverable, date)) {
        return;
      }

      plugin.getStorage().createAndLoadDiscoverinfo(playerId, group.getKey(), date);
    });
  }

  public void forget(PathPlayer<PlayerT> player, NodeGroup group) {
    if (!group.hasModifier(SimpleDiscoverableModifier.class)) {
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
    if (!(node instanceof Groupable groupable)) {
      return 1.5f;
    }
    FindDistanceModifier mod = groupable.getGroups().stream()
        .filter(group -> group.hasModifier(FindDistanceModifier.class))
        .sorted()
        .findFirst()
        .map(group -> group.getModifier(FindDistanceModifier.class))
        .orElse(null);
    return mod == null ? 1.5f : (float) mod.distance();
  }
}
