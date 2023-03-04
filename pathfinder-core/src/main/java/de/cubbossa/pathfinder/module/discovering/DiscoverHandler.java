package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.data.DiscoverInfo;
import de.cubbossa.pathfinder.module.discovering.event.PlayerDiscoverEvent;
import de.cubbossa.pathfinder.module.discovering.event.PlayerForgetEvent;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.serializedeffects.EffectHandler;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class DiscoverHandler {

  @Getter
  private static DiscoverHandler instance;

  private final Map<UUID, Map<NamespacedKey, DiscoverInfo>> discovered;

  public DiscoverHandler() {
    instance = this;

    discovered = new HashMap<>();
    if (!PathPlugin.getInstance().getConfiguration().isDiscoveryEnabled()) {
      return;
    }
    Bukkit.getPluginManager().registerEvents(new DiscoverListener(), PathPlugin.getInstance());

    if (PathPlugin.getInstance().getConfiguration().isFindLocationRequiresDiscovery()) {
      FindModule.getInstance().registerFindPredicate(context -> {
        if (context.navigable() instanceof Discoverable discoverable) {
          try {
            return hasDiscovered(context.playerId(), discoverable).get();
          } catch (InterruptedException | ExecutionException e) {
            return true;
          }
        }
        return true;
      });
    }
  }

  public void cachePlayer(UUID player) {
    CompletableFuture.runAsync(() -> {
      discovered.put(player, PathPlugin.getInstance().getDatabase().loadDiscoverInfo(player));
    });
  }

  public void invalidatePlayerCache(UUID player) {
    discovered.remove(player);
  }

  public void playDiscovery(UUID playerId, Discoverable discoverable) {
    Player player = Bukkit.getPlayer(playerId);
    if (player == null) {
      throw new IllegalStateException("Player is null");
    }
    EffectHandler.getInstance().playEffect(
        PathPlugin.getInstance().getEffectsFile(),
        "discover",
        player,
        player.getLocation(),
        Placeholder.component("name", discoverable.getDisplayName()));
  }

  public CompletableFuture<Map<NamespacedKey, DiscoverInfo>> getPlayerData(UUID player) {
    return discovered.containsKey(player) ?
        CompletableFuture.completedFuture(discovered.get(player)) :
        CompletableFuture.supplyAsync(() -> {
          Map<NamespacedKey, DiscoverInfo> data =
              PathPlugin.getInstance().getDatabase().loadDiscoverInfo(player);
          discovered.put(player, data);
          return data;
        });
  }

  public void discover(UUID playerId, Discoverable discoverable, LocalDateTime date) {
    getPlayerData(playerId).thenAccept(map -> {
      if (map.containsKey(discoverable.getKey())) {
        return;
      }

      Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
        if (map.containsKey(discoverable.getKey())) {
          return;
        }

        PlayerDiscoverEvent event = new PlayerDiscoverEvent(playerId, discoverable, date);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
          return;
        }
        discovered.computeIfAbsent(playerId, uuid -> new HashMap<>())
            .put(discoverable.getKey(), new DiscoverInfo(playerId, discoverable.getKey(), date));
        playDiscovery(playerId, discoverable);
      });
    });
  }

  public void forget(UUID playerId, Discoverable discoverable) {

    getPlayerData(playerId).thenAccept(map -> {

      if (!map.containsKey(discoverable.getKey())) {
        return;
      }
      Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
        PlayerForgetEvent event = new PlayerForgetEvent(playerId, discoverable);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
          return;
        }
        discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).remove(discoverable.getKey());
      });
    });
  }

  public CompletableFuture<Boolean> hasDiscovered(UUID playerId, Discoverable discoverable) {
    return getPlayerData(playerId).thenApply(map -> map.containsKey(discoverable.getKey()));
  }

  public float getDiscoveryDistance(UUID playerId, Node node) {
    return node instanceof Groupable groupable ?
        NodeGroupHandler.getInstance().getFindDistance(groupable) :
        3f;
  }
}
