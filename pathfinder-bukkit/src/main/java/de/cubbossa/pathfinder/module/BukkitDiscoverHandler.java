package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.AbstractDiscoverHandler;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.command.DiscoveriesCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitDiscoverHandler extends AbstractDiscoverHandler<Player> implements Listener {

  private final Collection<UUID> playerLock = ConcurrentHashMap.newKeySet();

  public BukkitDiscoverHandler(CommonPathFinder plugin) {
    super(plugin);
    Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance());

    BukkitPathFinder.getInstance().getCommandRegistry().registerCommand(new DiscoveriesCommand());
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    if (playerLock.contains(uuid)) {
      return;
    }
    playerLock.add(uuid);

    PathPlayer<Player> player = BukkitPathFinder.wrap(event.getPlayer());
    super.getFulfillingGroups(player).thenCompose(groups -> {
      return CompletableFuture.allOf(groups.stream()
          .map(group -> super.discover(player, group, LocalDateTime.now()))
          .toArray(CompletableFuture[]::new));
    }).thenRun(() -> playerLock.remove(uuid));
  }
}
