package de.cubbossa.pathfinder.module;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderExtension;
import de.cubbossa.pathapi.event.PathStartEvent;
import de.cubbossa.pathapi.event.PathStoppedEvent;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.command.CancelPathCommand;
import de.cubbossa.pathfinder.command.FindCommand;
import de.cubbossa.pathfinder.command.FindLocationCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

@AutoService(PathFinderExtension.class)
public class BukkitNavigationHandler extends AbstractNavigationHandler<Player> {

  @Getter
  private static BukkitNavigationHandler instance;

  private FindCommand findCommand;
  private FindLocationCommand findLocationCommand;
  private CancelPathCommand cancelPathCommand;

  public BukkitNavigationHandler() {
    super();
    instance = this;
    Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance());
  }

  @Override
  public void onLoad(PathFinder pathPlugin) {
    super.onLoad(pathPlugin);
    findCommand = new FindCommand(pathPlugin);
    findLocationCommand = new FindLocationCommand(pathPlugin);
    cancelPathCommand = new CancelPathCommand(pathPlugin);

    if (pathPlugin instanceof BukkitPathFinder bpf) {
      // TODO
      bpf.getCommandRegistry().registerCommand(findCommand);
      bpf.getCommandRegistry().registerCommand(findLocationCommand);
      bpf.getCommandRegistry().registerCommand(cancelPathCommand);
    }

    eventDispatcher.listen(PathStartEvent.class, e -> cancelPathCommand.refresh(e.getPlayer()));
    eventDispatcher.listen(PathStoppedEvent.class, e -> cancelPathCommand.refresh(e.getPlayer()));
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    Player p = event.getPlayer();
    PathPlayer<Player> pathPlayer = CommonPathFinder.getInstance().wrap(p);

    AbstractNavigationHandler.SearchInfo<Player> info = BukkitNavigationHandler.getInstance().getActivePath(pathPlayer);
    if (info != null && pathPlayer.getLocation().distance(info.target()) < info.distance()) {
      BukkitNavigationHandler.getInstance().reachTarget(info);
    }
  }
}
