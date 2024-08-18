package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.tinytranslations.Message;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public class ReloadCmd extends PathFinderSubCommand {

  public ReloadCmd(PathFinder pathFinder) {
    super(pathFinder, "reload");

    withPermission(PathPerms.PERM_CMD_PF_RELOAD);

    executes((sender, objects) -> {
      measuredReload(() -> {
        pathFinder.reloadConfigs();
        pathFinder.reloadLocale();
        pathFinder.getEventDispatcher().dispatchReloadEvent(true, true);
      }, sender, Messages.RELOAD_SUCCESS);
    });

    then(Arguments.literal("language")
        .executes((sender, objects) -> {
          measuredReload(() -> {
            pathFinder.reloadLocale();
            pathFinder.getEventDispatcher().dispatchReloadEvent(false, true);
          }, sender, Messages.RELOAD_SUCCESS_LANG);
        })
    );

    then(Arguments.literal("config")
        .executes((sender, objects) -> {
          measuredReload(() -> {
            pathFinder.reloadConfigs();
            pathFinder.getEventDispatcher().dispatchReloadEvent(true, false);
          }, sender, Messages.RELOAD_SUCCESS_CFG);
        })
    );
  }

  private void measuredReload(Runnable action, CommandSender sender, Message success) {
    long now = System.currentTimeMillis();

    CompletableFuture.runAsync(action).whenComplete((unused, throwable) -> {
      if (throwable != null) {
        BukkitUtils.wrap(sender).sendMessage(Messages.RELOAD_ERROR.formatted(TagResolver.builder()
            .resolver(Placeholder.component("error", Component.text(
                throwable.getMessage()
                    .replaceFirst("java\\.lang\\.RuntimeException: [^:]*: ", ""))))
            .build()));
        PathFinder.get().getLogger()
            .log(Level.SEVERE, "Error occured while reloading: ",
                throwable);
      } else {
        BukkitUtils.wrap(sender).sendMessage(success
            .insertNumber("ms", System.currentTimeMillis() - now));
      }
    });
  }
}
