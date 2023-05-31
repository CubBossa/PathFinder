package de.cubbossa.pathfinder.listener;

import de.cubbossa.pathapi.event.*;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.PathFinderConf;
import de.cubbossa.pathfinder.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class BukkitEffects {

  public BukkitEffects(EventDispatcher<Player> dispatcher, PathFinderConf.EffectsConf config) {

    dispatcher.listen(PathTargetReachedEvent.class, e -> {
      e.getPath().getTargetViewer().sendMessage(Messages.TARGET_FOUND);
      runCommands(e.getPath().getTargetViewer(), config.onPathTargetReach);
    });

    dispatcher.listen(PathCancelledEvent.class, e -> {
      e.getPath().getTargetViewer().sendMessage(Messages.CMD_CANCEL);
      runCommands(e.getPath().getTargetViewer(), config.onPathCancel);
    });

    dispatcher.listen(PathStoppedEvent.class, e -> {
      runCommands(e.getPath().getTargetViewer(), config.onPathStop);
    });

    dispatcher.listen(PlayerDiscoverLocationEvent.class, e -> {
      e.getPlayer().sendMessage(e.getModifier().getDisplayName());
    });

    dispatcher.listen(PlayerForgetLocationEvent.class, e -> {
      e.getPlayer().sendMessage(Component.text("Forget: ").append(e.getModifier().getDisplayName()));
    });
  }

  private String prepareCmd(String cmd, PathPlayer<Player> player) {
    return cmd
        .replace("<player>", player.getName());
  }

  private void runCommands(PathPlayer<Player> player, List<String> commands) {
    if (commands == null) {
      return;
    }
    commands.stream()
        .map(s -> this.prepareCmd(s, player))
        .forEach(r -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), r));
  }

}
