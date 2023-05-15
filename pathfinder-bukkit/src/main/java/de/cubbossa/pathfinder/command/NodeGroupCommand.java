package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;
import java.util.stream.Collectors;

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
            .executes((commandSender, args) -> {
              showGroup(commandSender, args.getUnchecked(0));
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
              listGroups(sender, args.getUnchecked(0));
            })));

    then(CustomArgs.literal("create")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_NG_CREATE)
        .then(new StringArgument("name")
            .executes((sender, args) -> {
              createGroup(sender, args.getUnchecked(0));
            })));

    then(CustomArgs.literal("delete")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_NG_DELETE)
        .then(CustomArgs.nodeGroupArgument("group")
            .executes((sender, args) -> {
              deleteGroup(sender, args.getUnchecked(0));
            })));

    Argument<?> set = CustomArgs.literal("set").withPermission(PathPerms.PERM_CMD_NG_SET_MOD);
    Argument<?> unset =
        CustomArgs.literal("unset").withPermission(PathPerms.PERM_CMD_NG_UNSET_MOD);
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
            removeModifier(commandSender, args.getUnchecked(0),
                modifier.getModifierClass());
          })
      );
    }
    then(CustomArgs.literal("modify")
        .then(CustomArgs.nodeGroupArgument("group")
            .then(set)
            .then(unset)
        )
    );
  }

  private void listGroups(CommandSender sender, Pagination pagination) {
    getPathfinder().getStorage().loadAllGroups().thenAccept(nodeGroups -> {
      sender.sendMessage(nodeGroups.stream().map(NodeGroup::getKey).map(NamespacedKey::getKey)
          .collect(Collectors.joining(", ")));
    });
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
            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_LIST_LINE.formatted(r));
          },
          Messages.CMD_NG_LIST_HEADER,
          Messages.CMD_NG_LIST_FOOTER
      );
      return nodeGroups;
    });
  }

  private void showGroup(CommandSender sender, SimpleNodeGroup group) {
    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_INFO.formatted(TagResolver.builder()
        .tag("key", Messages.formatKey(group.getKey()))
        .resolver(Placeholder.component("nodes", Messages.formatNodeSelection(sender, group.resolve().join())))
        .resolver(Formatter.number("weight", group.getWeight()))
        .resolver(Placeholder.component("modifiers", Messages.formatModifiers(sender, group.getModifiers())))
        .build()));
  }

  private void createGroup(CommandSender sender, String name) {
    name = name.toString().toLowerCase();
    NamespacedKey key = CommonPathFinder.pathfinder(name);
    if (getPathfinder().getStorage().loadGroup(key).join().isPresent()) {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_ALREADY_EXISTS.formatted(
          Placeholder.parsed("name", name)
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
  }

  private void deleteGroup(CommandSender sender, SimpleNodeGroup group) {
    PathPlayer<?> p = BukkitUtils.wrap(sender);
    if (group.getKey().equals(CommonPathFinder.globalGroupKey())) {
      p.sendMessage(Messages.CMD_NG_DELETE_GLOBAL);
      return;
    }
    getPathfinder().getStorage().deleteGroup(group).thenRun(() -> {
      p.sendMessage(Messages.CMD_NG_DELETE.formatted(
          Placeholder.parsed("name", group.getKey().toString())
      ));
    });
  }

  private void addModifier(CommandSender sender, SimpleNodeGroup group, Modifier modifier) {
    group.addModifier(modifier);
    getPathfinder().getStorage().saveGroup(group).thenRun(() -> {
      // TODO message
    });
  }

  private void removeModifier(CommandSender sender, SimpleNodeGroup group,
                              Class<? extends Modifier> mod) {
    group.removeModifier(mod);
    getPathfinder().getStorage().saveGroup(group).thenRun(() -> {
      // TODO message
    });
  }
}
