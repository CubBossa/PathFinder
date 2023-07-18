package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitDiscoverHandler extends AbstractDiscoverHandler<Player> implements Listener {

  private final PathFinder pathFinder;
  private final Collection<UUID> playerLock = ConcurrentHashMap.newKeySet();

  public BukkitDiscoverHandler(CommonPathFinder plugin) {
    super(plugin);
    pathFinder = PathFinderProvider.get();
    Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance());
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    if (playerLock.contains(uuid)) {
      return;
    }
    playerLock.add(uuid);
    pathFinder.getStorage().loadGroups(DiscoverableModifier.KEY).thenAccept(nodeGroups -> {
      PathPlayer<Player> player = BukkitPathFinder.wrap(event.getPlayer());
      Collection<CompletableFuture<?>> futures = new HashSet<>();
      for (NodeGroup group : nodeGroups) {
        futures.add(super.fulfillsDiscoveringRequirements(group, player).thenCompose(b -> {
          if (b) {
            return super.discover(player, group, LocalDateTime.now());
          }
          return CompletableFuture.completedFuture(null);
        }));
      }
      CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> playerLock.remove(uuid));
    });
  }
}
