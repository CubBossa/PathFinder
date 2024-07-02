package de.cubbossa.pathfinder.navigation;

import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderExtension;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.command.CancelPathCommand;
import de.cubbossa.pathfinder.command.FindCommand;
import de.cubbossa.pathfinder.command.FindLocationCommand;
import de.cubbossa.pathfinder.command.PathFinderReloadListener;
import de.cubbossa.pathfinder.event.PathStartEvent;
import de.cubbossa.pathfinder.event.PathStoppedEvent;
import de.cubbossa.pathfinder.misc.PathPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.pf4j.Extension;

@Extension(points = {PathFinderExtension.class, PathFinderReloadListener.class})
public class BukkitNavigationModule extends AbstractNavigationModule<Player> implements Listener {

  private FindCommand findCommand;
  private FindLocationCommand findLocationCommand;
  private CancelPathCommand cancelPathCommand;

  public BukkitNavigationModule() {
    super();
  }

  @Override
  public void onLoad(PathFinder pathPlugin) {

    super.onLoad(pathPlugin);

    findCommand = new FindCommand();
    findLocationCommand = new FindLocationCommand();
    cancelPathCommand = new CancelPathCommand();

    if (pathPlugin instanceof BukkitPathFinder bpf) {
      // TODO
      bpf.getCommandRegistry().registerCommand(findCommand);
      bpf.getCommandRegistry().registerCommand(findLocationCommand);
      bpf.getCommandRegistry().registerCommand(cancelPathCommand);
    }
  }

  @Override
  public void onEnable(PathFinder pathPlugin) {
    super.onEnable(pathPlugin);

    Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance());
    eventDispatcher.listen(PathStartEvent.class, e -> cancelPathCommand.refresh(e.getPlayer()));
    eventDispatcher.listen(PathStoppedEvent.class, e -> cancelPathCommand.refresh(e.getPlayer()));
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    Player p = event.getPlayer();
    PathPlayer<Player> pathPlayer = PathPlayer.wrap(p);

    AbstractNavigationModule<Player>.NavigationContext info = activePaths.get(pathPlayer.getUniqueId());
    if (info != null && pathPlayer.getLocation().distanceSquared(info.target().getLocation()) < Math.pow(info.dist(), 2)) {
      reach(info.playerId());
    }
  }
}
