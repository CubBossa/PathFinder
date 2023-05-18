package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class CreateGroupCmd extends PathFinderSubCommand {

  public CreateGroupCmd(PathFinder pathFinder) {
    super(pathFinder, "creategroup");
    withGeneratedHelp();

    withPermission(PathPerms.PERM_CMD_NG_CREATE);
    then(new StringArgument("name")
        .executes((sender, args) -> {
          createGroup(sender, args.getUnchecked(0));
        })
    );
  }

  private void createGroup(CommandSender sender, String name) {
    String finalName = name.toLowerCase();
    NamespacedKey key = CommonPathFinder.pathfinder(name);
    getPathfinder().getStorage().loadGroup(key).thenAccept(optGroup -> {
      if (optGroup.isPresent()) {
        BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_ALREADY_EXISTS.formatted(
            Placeholder.parsed("name", finalName)
        ));
        return;
      }
      getPathfinder().getStorage()
          .createAndLoadGroup(CommonPathFinder.pathfinder(name))
          .thenAccept(group -> {
            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_CREATE.formatted(
                TagResolver.resolver("name", Messages.formatKey(group.getKey()))
            ));
          })
          .exceptionally(throwable -> {
            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_CREATE_FAIL);
            getPathfinder().getLogger().log(Level.SEVERE, "Could not create nodegroup.", throwable);
            return null;
          });
    });
  }
}
