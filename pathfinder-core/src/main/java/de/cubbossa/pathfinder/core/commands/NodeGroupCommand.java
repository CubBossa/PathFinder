package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.nodegroup.modifier.PermissionModifier;
import de.cubbossa.pathfinder.storage.ApplicationLayer;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.NamespacedKey;

/**
 * A command to manage NodeGroups.
 */
public class NodeGroupCommand extends Command {

  /**
   * A command to manage NodeGroups.
   *
   * @param offset The argument index offset. Increase if this command is also a child of another
   *               command with non-static arguments.
   */
  public NodeGroupCommand(PathFinder pathFinder, int offset) {
    super(pathFinder, "nodegroup");
    withGeneratedHelp();

    withRequirement(sender -> sender.hasPermission(PathPerms.PERM_CMD_NG_LIST)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_CREATE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_DELETE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_ST_ADD)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_ST_REMOVE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_ST_LIST)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_SET_NAME)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_SET_PERM)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_SET_NAVIGABLE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_SET_DISCOVERABLE)
        || sender.hasPermission(PathPerms.PERM_CMD_NG_SET_DISCOVER_DIST)
    );

    then(CustomArgs.literal("list")
        .withPermission(PathPerms.PERM_CMD_NG_LIST)
        .executes((sender, objects) -> {
          PathFinderAPI.builder()
              .withEvents()
              .withMessages(sender)
              .build()
              .getNodeGroups(ApplicationLayer.Pagination.page(1, 10));
        })
        .then(CustomArgs.integer("page", 1)
            .displayAsOptional()
            .executes((sender, args) -> {
              PathFinderAPI.builder()
                  .withEvents()
                  .withMessages(sender)
                  .build()
                  .getNodeGroups(ApplicationLayer.Pagination.page((Integer) args[offset], 10));
            })));

    then(CustomArgs.literal("create")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_NG_CREATE)
        .then(new StringArgument("name")
            .executes((sender, args) -> {
              PathFinderAPI.builder()
                  .withEvents()
                  .withMessages(sender)
                  .build()
                  .createNodeGroup(
                      new NamespacedKey(PathPlugin.getInstance(), args[offset].toString()));
            })));

    then(CustomArgs.literal("delete")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_NG_DELETE)
        .then(CustomArgs.nodeGroupArgument("group")
            .executes((sender, objects) -> {
              PathFinderAPI.builder()
                  .withEvents()
                  .withMessages(sender)
                  .build()
                  .deleteNodeGroup((NamespacedKey) objects[offset]);
            })));

    CustomLiteralArgument unset = CustomArgs.literal("unset");

    then(CustomArgs.literal("edit")
        .then(CustomArgs.nodeGroupArgument("nodegroup")
            .then(CustomArgs.literal("unset")
                .withGeneratedHelp()
                .then(CustomArgs.literal("permission")
                    .executes((commandSender, args) -> {
                      PathFinderAPI.builder()
                          .withEvents()
                          .withMessages(commandSender)
                          .build()
                          .unassignNodeGroupModifier((NamespacedKey) args[0], PermissionModifier.class);
                    })
                )
            )
            .then(CustomArgs.literal("set")
                .withGeneratedHelp()
                .then(CustomArgs.literal("permission")
                    .then(new StringArgument("permission-node")
                        .executes((commandSender, args) -> {
                          PathFinderAPI.builder()
                              .withEvents()
                              .withMessages(commandSender)
                              .build()
                              .assignNodeGroupModifier((NamespacedKey) args[0], new PermissionModifier((String) args[1]));
                        })
                    )
                )
                .then(CustomArgs.literal("curve-length"))
            )
        )
    );


  }
}
