package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class CreateGroupCmd extends PathFinderSubCommand {

  public CreateGroupCmd(PathFinder pathFinder) {
    super(pathFinder, "creategroup");
    withGeneratedHelp();

    withPermission(PathPerms.PERM_CMD_NG_CREATE);
    then(new StringArgument("name")
        .executes((sender, args) -> {
          createGroup(sender, args.getUnchecked(0).toString().toLowerCase());
        })
    );
  }

  private void createGroup(CommandSender sender, String name) {
    NamespacedKey key = AbstractPathFinder.pathfinder(name);
    getPathfinder().getStorage().loadGroup(key).thenAccept(optGroup -> {
      if (optGroup.isPresent()) {
        BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_ALREADY_EXISTS.formatted(
            Placeholder.parsed("key", name)
        ));
        return;
      }
      getPathfinder().getStorage()
          .createAndLoadGroup(AbstractPathFinder.pathfinder(name))
          .thenAccept(group -> {
            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_CREATE.formatted(
                Messages.formatter().namespacedKey("key", group.getKey())
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
