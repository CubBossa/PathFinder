package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.arguments.Argument;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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

    Argument<?> set = CustomArgs.literal("set").withPermission(PathPerms.PERM_CMD_NG_SET_MOD);
    Argument<?> unset = CustomArgs.literal("unset").withPermission(PathPerms.PERM_CMD_NG_UNSET_MOD);

    for (ModifierType<?> modifier : getPathfinder().getModifierRegistry().getTypes()) {
      if (!(modifier instanceof ModifierCommandExtension<?> cmdExt)) {
        continue;
      }
      Argument<?> lit = CustomArgs.literal(modifier.getSubCommandLiteral());
      lit = cmdExt.registerAddCommand(lit, mod -> (commandSender, args) -> {
        addModifier(commandSender, args.getUnchecked(0), mod);
      });
      set = set.then(lit);
      unset = unset.then(CustomArgs.literal(modifier.getSubCommandLiteral())
          .executes((commandSender, args) -> {
            removeModifier(commandSender, args.getUnchecked(0), modifier.getKey());
          })
      );
    }

    then(CustomArgs.nodeGroupArgument("group")
        .then(CustomArgs.literal("info")
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
    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_INFO.formatted(TagResolver.builder()
        .tag("key", Messages.formatKey(group.getKey()))
        .resolver(Placeholder.component("nodes", Messages.formatNodeSelection(sender, group.resolve().join())))
        .resolver(Formatter.number("weight", group.getWeight()))
        .resolver(Placeholder.component("modifiers", Messages.formatModifiers(sender, group.getModifiers())))
        .build()));
  }

  private void addModifier(CommandSender sender, SimpleNodeGroup group, Modifier modifier) {
    group.addModifier(modifier);
    getPathfinder().getStorage().saveGroup(group).thenRun(() -> {
      CommonPathFinder.getInstance().wrap(sender).sendMessage(Messages.CMD_NG_MODIFY_SET.formatted(
          TagResolver.resolver("group", Messages.formatKey(group.getKey())),
          TagResolver.resolver("type", Messages.formatKey(modifier.getKey()))
      ));
    });
  }

  private void removeModifier(CommandSender sender, SimpleNodeGroup group, NamespacedKey mod) {
    group.removeModifier(mod);
    getPathfinder().getStorage().saveGroup(group).thenRun(() -> {
      CommonPathFinder.getInstance().wrap(sender).sendMessage(Messages.CMD_NG_MODIFY_REMOVE.formatted(
          TagResolver.resolver("group", Messages.formatKey(group.getKey())),
          TagResolver.resolver("type", Messages.formatKey(mod))
      ));
    });
  }
}
