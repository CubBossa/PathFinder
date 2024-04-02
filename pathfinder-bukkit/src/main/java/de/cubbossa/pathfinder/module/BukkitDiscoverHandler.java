package de.cubbossa.pathfinder.module;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.command.DiscoveriesCommand;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@AutoService(PathFinderExtension.class)
public class BukkitDiscoverHandler extends AbstractDiscoverHandler<Player> implements Listener {

  private final Collection<UUID> playerLock = ConcurrentHashMap.newKeySet();
  private final DiscoveriesCommand discoveriesCommand;

  public BukkitDiscoverHandler() {
    discoveriesCommand = new DiscoveriesCommand();
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {
    super.onEnable(pathPlugin);
    Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance());
    BukkitPathFinder.getInstance().getCommandRegistry().registerCommand(discoveriesCommand);
  }

  @Override
  public void dispose() {
    BukkitPathFinder.getInstance().getCommandRegistry().unregisterCommand(discoveriesCommand);
    PlayerMoveEvent.getHandlerList().unregister(this);
    super.dispose();
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
