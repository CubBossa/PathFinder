package de.cubbossa.pathfinder.module;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.event.PathStartEvent;
import de.cubbossa.pathapi.event.PathStoppedEvent;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.command.CancelPathCommand;
import de.cubbossa.pathfinder.command.FindCommand;
import de.cubbossa.pathfinder.command.FindLocationCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@AutoService(PathFinderExtension.class)
public class BukkitNavigationHandler extends AbstractNavigationHandler<Player> implements Listener {

  @Getter
  private static BukkitNavigationHandler instance;

  private FindCommand findCommand;
  private FindLocationCommand findLocationCommand;
  private CancelPathCommand cancelPathCommand;

  @Override
  public void onLoad(PathFinder pathPlugin) {
    instance = this;

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
    PathPlayer<Player> pathPlayer = AbstractPathFinder.getInstance().wrap(p);

    AbstractNavigationHandler.SearchInfo<Player> info = BukkitNavigationHandler.getInstance().getActivePath(pathPlayer);
    if (info != null && pathPlayer.getLocation().distanceSquared(info.target()) < Math.pow(info.distance(), 2)) {
      BukkitNavigationHandler.getInstance().reachTarget(info);
    }
  }
}
