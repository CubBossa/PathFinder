package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.Modifier;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.nodegroup.ModifierType;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.Pagination;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

/**
 * A command to manage NodeGroups.
 */
public class NodeGroupCommand extends Command {

  /**
   * A command to manage NodeGroups.
   */
  public NodeGroupCommand(PathFinder pathFinder) {
    super(pathFinder, "nodegroup");
    withGeneratedHelp();

    withRequirement(sender -> sender.hasPermission(PathPerms.PERM_CMD_NG_LIST)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_INFO)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_CREATE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_DELETE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_SET_MOD)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_UNSET_MOD)
    );

    then(CustomArgs.literal("info")
        .withPermission(PathPerms.PERM_CMD_NG_INFO)
        .then(CustomArgs.nodeGroupArgument("group")
            .executes((commandSender, objects) -> {
              showGroup(commandSender, (NodeGroup) objects[0]);
            })
        )
    );
    then(CustomArgs.literal("list")
        .withPermission(PathPerms.PERM_CMD_NG_LIST)
        .executes((sender, objects) -> {
          listGroups(sender, Pagination.page(0, 10));
        })
        .then(CustomArgs.pagination(10)
            .displayAsOptional()
            .executes((sender, args) -> {
              listGroups(sender, (Pagination) args[0]);
            })));

    then(CustomArgs.literal("create")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_NG_CREATE)
        .then(new StringArgument("name")
            .executes((sender, args) -> {
              createGroup(sender, (String) args[0]);
            })));

    then(CustomArgs.literal("delete")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_NG_DELETE)
        .then(CustomArgs.nodeGroupArgument("group")
            .executes((sender, objects) -> {
              deleteGroup(sender, (NodeGroup) objects[0]);
            })));

    ArgumentTree set = CustomArgs.literal("set").withPermission(PathPerms.PERM_CMD_NG_SET_MOD);
    ArgumentTree unset = CustomArgs.literal("unset").withPermission(PathPerms.PERM_CMD_NG_UNSET_MOD);
    for (ModifierType<?> modifier : getPathfinder().getModifierRegistry().getModifiers()) {
      ArgumentTree lit = CustomArgs.literal(modifier.getSubCommandLiteral());
      lit = modifier.registerAddCommand(lit, mod -> (commandSender, objects) -> {
        addModifier(commandSender, (NodeGroup) objects[0], mod);
      });
      set = set.then(lit);
      unset = unset.then(CustomArgs.literal(modifier.getSubCommandLiteral())
          .executes((commandSender, objects) -> {
            removeModifier(commandSender, (NodeGroup) objects[0], modifier.getModifierClass());
          })
      );
    }
  }

  private void listGroups(CommandSender sender, Pagination pagination) {
    getPathfinder().getStorage().loadGroups(pagination).thenApply(nodeGroups -> {
      CommandUtils.printList(
          sender,
          pagination,
          p -> getPathfinder().getStorage().loadGroups(p).join().stream().toList(),
          group -> {
            TagResolver r = TagResolver.builder()
                .resolver(Placeholder.component("key", Component.text(group.getKey().toString())))
                .resolver(Placeholder.component("size", Component.text(group.size())))
                .build();
            TranslationHandler.getInstance()
                .sendMessage(Messages.CMD_NG_LIST_LINE.format(r), sender);
          },
          Messages.CMD_NG_LIST_HEADER,
          Messages.CMD_NG_LIST_FOOTER
      );
      return nodeGroups;
    });
  }

  private void showGroup(CommandSender sender, NodeGroup group) {
  }

  private void createGroup(CommandSender sender, String name) {
    //TODO precods
    TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS.format(
        Placeholder.parsed("name", name)
    ), sender);
    TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE_FAIL, sender);

    getPathfinder().getStorage().createAndLoadGroup(new NamespacedKey(PathPlugin.getInstance(), ""))
        .thenAccept(group -> {
          TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(
              Placeholder.parsed("key", group.getKey().toString())
          ), sender);
        });
  }

  private void deleteGroup(CommandSender sender, NodeGroup group) {
    getPathfinder().getStorage().deleteGroup(group).thenRun(() -> {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(
          Placeholder.parsed("name", group.getKey().toString())
      ), sender);
    });
  }

  private void addModifier(CommandSender sender, NodeGroup group, Modifier modifier) {
    group.addModifier(modifier);
    getPathfinder().getStorage().saveGroup(group).thenRun(() -> {
      // TODO message
    });
  }

  private void removeModifier(CommandSender sender, NodeGroup group, Class<? extends Modifier> mod) {
    group.removeModifier(mod);
    getPathfinder().getStorage().saveGroup(group).thenRun(() -> {
      // TODO message
    });
  }
}
