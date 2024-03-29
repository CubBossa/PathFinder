package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.arguments.Argument;
import org.bukkit.command.CommandSender;

/**
 * A command to manage NodeGroups.
 */
public class GroupCmd extends PathFinderSubCommand {

  /**
   * A command to manage NodeGroups.
   */
  public GroupCmd(PathFinder pathFinder) {
    super(pathFinder, "group");
    withGeneratedHelp();

    withRequirement(sender -> sender.hasPermission(PathPerms.PERM_CMD_NG_LIST)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_INFO)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_CREATE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_DELETE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_SET_MOD)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_UNSET_MOD)
    );

    Argument<?> set = Arguments.literal("set").withPermission(PathPerms.PERM_CMD_NG_SET_MOD);
    Argument<?> unset = Arguments.literal("unset").withPermission(PathPerms.PERM_CMD_NG_UNSET_MOD);

    for (ModifierType<?> modifier : getPathfinder().getModifierRegistry().getTypes()) {
      if (!(modifier instanceof ModifierCommandExtension<?> cmdExt)) {
        continue;
      }
      Argument<?> lit = Arguments.literal(modifier.getSubCommandLiteral());
      lit = cmdExt.registerAddCommand(lit, mod -> (commandSender, args) -> {
        addModifier(commandSender, args.getUnchecked(0), mod);
      });
      set = set.then(lit);
      unset = unset.then(Arguments.literal(modifier.getSubCommandLiteral())
          .executes((commandSender, args) -> {
            removeModifier(commandSender, args.getUnchecked(0), modifier.getKey());
          })
      );
    }

    then(Arguments.nodeGroupArgument("group")
        .then(Arguments.literal("info")
            .withPermission(PathPerms.PERM_CMD_NG_INFO)
            .executes((commandSender, args) -> {
              showGroup(commandSender, args.getUnchecked(0));
            })
        )

        .then(set)
        .then(unset)
    );
  }

  private void showGroup(CommandSender sender, SimpleNodeGroup group) {
    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_INFO.insertObject("group", group));
  }

  private void addModifier(CommandSender sender, SimpleNodeGroup group, Modifier modifier) {
    group.addModifier(modifier);
    getPathfinder().getStorage().saveGroup(group).thenRun(() -> {
      CommonPathFinder.getInstance().wrap(sender).sendMessage(Messages.CMD_NG_MODIFY_SET
          .insertObject("group", group)
          .insertObject("mod", modifier));
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  private void removeModifier(CommandSender sender, SimpleNodeGroup group, NamespacedKey mod) {
    group.removeModifier(mod);
    getPathfinder().getStorage().saveGroup(group).thenRun(() -> {
      CommonPathFinder.getInstance().wrap(sender).sendMessage(Messages.CMD_NG_MODIFY_REMOVE
          .insertObject("group", group)
          .insertObject("mod", mod));
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }
}
