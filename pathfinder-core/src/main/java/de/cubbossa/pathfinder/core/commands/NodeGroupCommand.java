package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.data.ApplicationLayer;
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
  public NodeGroupCommand(int offset) {
    super("nodegroup");
    withGeneratedHelp();

    withRequirement(sender -> sender.hasPermission(PathPlugin.PERM_CMD_NG_LIST)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_CREATE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_DELETE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_ADD)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_REMOVE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_ST_LIST)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_NAME)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_PERM)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_NAVIGABLE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVERABLE)
        || sender.hasPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVER_DIST)
    );

    then(CustomArgs.literal("list")
        .withPermission(PathPlugin.PERM_CMD_NG_LIST)
        .executes((sender, objects) -> {
          PathFinderAPI.getInstance()
              .eventLayer()
              .messageLayer(sender)
              .getNodeGroups(ApplicationLayer.Pagination.page(1, 10));
        })
        .then(CustomArgs.integer("page", 1)
            .displayAsOptional()
            .executes((sender, args) -> {
              PathFinderAPI.getInstance()
                  .eventLayer()
                  .messageLayer(sender)
                  .getNodeGroups(ApplicationLayer.Pagination.page((Integer) args[offset], 10));
            })));

    then(CustomArgs.literal("create")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_NG_CREATE)
        .then(new StringArgument("name")
            .executes((sender, args) -> {
              PathFinderAPI.getInstance()
                  .eventLayer()
                  .messageLayer(sender)
                  .createNodeGroup(new NamespacedKey(PathPlugin.getInstance(), args[offset].toString()));
            })));

    then(CustomArgs.literal("delete")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_NG_DELETE)
        .then(CustomArgs.nodeGroupArgument("group")
            .executes((sender, objects) -> {
              PathFinderAPI.getInstance()
                  .eventLayer()
                  .messageLayer(sender)
                  .deleteNodeGroup((NamespacedKey) objects[offset]);
            })));

    CustomLiteralArgument unset = CustomArgs.literal("unset");


    then(CustomArgs.literal("unset")
            .withGeneratedHelp()
            .then(CustomArgs.literal("permission")

            )
    );
    then(CustomArgs.literal("set")
            .withGeneratedHelp()
            .then(CustomArgs.literal("permission")
                .then(new StringArgument("permission-node")
                    .executes((commandSender, objects) -> {

                    })
                )
            )
        .then(CustomArgs.literal("curve-length"))
    );
  }
}
