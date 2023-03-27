package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.modifier.DiscoverableModifier;
import de.cubbossa.pathfinder.core.nodegroup.modifier.FindDistanceModifier;
import de.cubbossa.pathfinder.core.nodegroup.modifier.PermissionModifier;
import de.cubbossa.pathfinder.data.DiscoverInfo;
import de.cubbossa.pathfinder.module.discovering.event.PlayerDiscoverEvent;
import de.cubbossa.pathfinder.module.discovering.event.PlayerForgetEvent;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.serializedeffects.EffectHandler;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class DiscoverHandler {

  @Getter
  private static DiscoverHandler instance;
  private PathPlugin plugin;
  private final Map<UUID, Map<NamespacedKey, DiscoverInfo>> discovered;

  public DiscoverHandler(PathPlugin plugin) {
    instance = this;
    this.plugin = plugin;
    
    discovered = new HashMap<>();
    if (!plugin.getConfiguration().moduleConfig.discoveryModule) {
      return;
    }
    Bukkit.getPluginManager().registerEvents(new DiscoverListener(), plugin);

    if (plugin.getConfiguration().navigation.requireDiscovery) {
      FindModule.getInstance().registerFindPredicate(context -> {
        if (!(context.node() instanceof Groupable<?> groupable)) {
          return true;
        }
        Collection<NodeGroup> groups = groupable.getGroups().stream()
            .map(key -> PathFinderAPI.get().getNodeGroup(key))
            .parallel()
            .map(CompletableFuture::join)
            .toList();

        for (NodeGroup group : groups) {
          if (!group.hasModifier(DiscoverableModifier.class)) {
            continue;
          }
          try {
            if (!hasDiscovered(context.playerId(), group).get()) {
              return false;
            }
          } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
        }
        return true;
      });
    }
  }

  public void cachePlayer(UUID player) {
    CompletableFuture.runAsync(() -> {
      discovered.put(player, plugin.getDatabase().loadDiscoverInfo(player));
    });
  }

  public void invalidatePlayerCache(UUID player) {
    discovered.remove(player);
  }

  public void playDiscovery(UUID playerId, DiscoverableModifier discoverable) {
    Player player = Bukkit.getPlayer(playerId);
    if (player == null) {
      throw new IllegalStateException("Player is null");
    }
    EffectHandler.getInstance().playEffect(
        plugin.getEffectsFile(),
        "discover",
        player,
        player.getLocation(),
        Placeholder.component("name", discoverable.getDisplayName()));
  }

  public boolean fulfillsDiscoveringRequirements(NodeGroup group, Player player) {
    if (!group.hasModifier(DiscoverableModifier.class)) {
      return false;
    }
    PermissionModifier perm = group.getModifier(PermissionModifier.class);
    if (perm != null && !player.hasPermission(perm.permission())) {
      return false;
    }
    return PathFinderAPI.get().getNodes(new NodeSelection(group)).thenApply(nodes -> {
      for (Node<?> node : nodes) {
        if (node == null) {
          plugin.getLogger().log(Level.SEVERE, "Node is null");
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
    }).join();
  }

  public CompletableFuture<Map<NamespacedKey, DiscoverInfo>> getPlayerData(UUID player) {
    return discovered.containsKey(player) ?
        CompletableFuture.completedFuture(discovered.get(player)) :
        CompletableFuture.supplyAsync(() -> {
          Map<NamespacedKey, DiscoverInfo> data =
              plugin.getDatabase().loadDiscoverInfo(player);
          discovered.put(player, data);
          return data;
        });
  }

  public void discover(UUID playerId, NodeGroup group, LocalDateTime date) {
    if (!group.hasModifier(DiscoverableModifier.class)) {
      return;
    }

    getPlayerData(playerId).thenAccept(map -> {
      if (map.containsKey(group.getKey())) {
        return;
      }

      Bukkit.getScheduler().runTask(plugin, () -> {
        if (map.containsKey(group.getKey())) {
          return;
        }

        PlayerDiscoverEvent event = new PlayerDiscoverEvent(playerId, group, date);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
          return;
        }
        DiscoverableModifier discoverable = group.getModifier(DiscoverableModifier.class);

        discovered.computeIfAbsent(playerId, uuid -> new HashMap<>())
            .put(group.getKey(), new DiscoverInfo(playerId, group.getKey(), date));
        playDiscovery(playerId, discoverable);
      });
    });
  }

  public void forget(UUID playerId, NodeGroup group) {
    if (!group.hasModifier(DiscoverableModifier.class)) {
      return;
    }

    getPlayerData(playerId).thenAccept(map -> {
      if (!map.containsKey(group.getKey())) {
        return;
      }
      Bukkit.getScheduler().runTask(plugin, () -> {
        PlayerForgetEvent event = new PlayerForgetEvent(playerId, group);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
          return;
        }
        discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).remove(group.getKey());
      });
    });
  }

  public CompletableFuture<Boolean> hasDiscovered(UUID playerId, NodeGroup group) {
    return getPlayerData(playerId).thenApply(map -> map.containsKey(group.getKey()));
  }

  public float getDiscoveryDistance(UUID playerId, Node<?> node) {
    if (!(node instanceof Groupable<?> groupable)) {
      return 1.5f;
    }
    FindDistanceModifier mod = groupable.getGroups().stream()
        .map(key -> PathFinderAPI.get().getNodeGroup(key))
        .parallel()
        .map(CompletableFuture::join)
        .filter(group -> group.hasModifier(FindDistanceModifier.class))
        .sorted()
        .findFirst()
        .map(group -> group.getModifier(FindDistanceModifier.class))
        .orElse(null);
    return mod == null ? 1.5f : (float) mod.distance();
  }
}
