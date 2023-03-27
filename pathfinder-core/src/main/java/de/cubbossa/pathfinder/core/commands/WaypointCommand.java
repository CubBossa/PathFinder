package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.arguments.LocationType;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WaypointCommand extends Command {

  public WaypointCommand(Supplier<NodeType<?>> fallbackWaypointType) {
    super("waypoint");
    withAliases("node");
    withGeneratedHelp();

    withRequirement(sender -> sender.hasPermission(PathPerms.PERM_CMD_WP_INFO)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_LIST)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_CREATE)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_DELETE)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_TPHERE)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_TP)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_CONNECT)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_DISCONNECT)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_SET_CURVE)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_ADD_GROUP)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_REMOVE_GROUP)
        || sender.hasPermission(PathPerms.PERM_CMD_WP_CLEAR_GROUPS)
    );

    then(CustomArgs.literal("info")
        .withPermission(PathPerms.PERM_CMD_WP_INFO)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              onInfo(player, (NodeSelection) objects[0]);
            })
        )
    );
    then(CustomArgs.literal("list")
        .withPermission(PathPerms.PERM_CMD_WP_LIST)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              onList(player, (NodeSelection) objects[0], 1);
            })
            .then(CustomArgs.integer("page", 1)
                .displayAsOptional()
                .executesPlayer((player, objects) -> {
                  onList(player, (NodeSelection) objects[0], (Integer) objects[1]);
                })
            ))
    );
    then(CustomArgs.literal("create")
        .withPermission(PathPerms.PERM_CMD_WP_CREATE)
        .executesPlayer((player, objects) -> {
          PathFinderAPI.builder()
              .withEvents()
              .withMessages(player)
              .build()
              .createNode(fallbackWaypointType.get(), player.getLocation().add(new Vector(0, 1, 0)));
        })
        .then(CustomArgs.location("location")
            .displayAsOptional()
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                  .withEvents()
                  .withMessages(player)
                  .build()
                  .createNode(fallbackWaypointType.get(), (Location) objects[1]);
            })
        )
        .then(CustomArgs.nodeTypeArgument("type")
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                  .withEvents()
                  .withMessages(player)
                  .build()
                  .createNode(
                      (NodeType<? extends Node<?>>) objects[0],
                      player.getLocation().add(new Vector(0, 1, 0))
                  );
            })
            .then(CustomArgs.location("location")
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                      .createNode(
                          (NodeType<? extends Node<?>>) objects[0],
                          (Location) objects[2]
                      );
                })
            )
        )
    );
    then(CustomArgs.literal("delete")
        .withPermission(PathPerms.PERM_CMD_WP_DELETE)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                  .deleteNodes((NodeSelection) objects[0]);
            })
        )
    );
    then(CustomArgs.literal("tphere")
        .withPermission(PathPerms.PERM_CMD_WP_TPHERE)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                  .updateNodes((NodeSelection) objects[0], node -> {
                    node.setLocation(player.getLocation());
                  });
            })
        )
    );
    then(CustomArgs.literal("tp")
        .withPermission(PathPerms.PERM_CMD_WP_TP)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .then(CustomArgs.location("location", LocationType.PRECISE_POSITION)
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                      .updateNodes((NodeSelection) objects[0], node -> {
                        node.setLocation((Location) objects[1]);
                      });
                })
            )
        )
    );
    then(CustomArgs.literal("connect")
        .withPermission(PathPerms.PERM_CMD_WP_CONNECT)
        .then(CustomArgs.nodeSelectionArgument("start")
            .then(CustomArgs.nodeSelectionArgument("end")
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                      .connectNodes((NodeSelection) objects[0], (NodeSelection) objects[1]);
                })
            )
        )
    );
    then(CustomArgs.literal("disconnect")
        .withPermission(PathPerms.PERM_CMD_WP_DISCONNECT)
        .then(CustomArgs.nodeSelectionArgument("start")
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                  .withEvents()
                  .withMessages(player)
                  .build()
                  .disconnectNodes((NodeSelection) objects[0]);
            })
            .then(CustomArgs.nodeSelectionArgument("end")
                .displayAsOptional()
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                      .disconnectNodes((NodeSelection) objects[0], (NodeSelection) objects[1]);
                })
            )
        )

    );
    then(CustomArgs.literal("group")
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .then(CustomArgs.literal("add")
                .withPermission(PathPerms.PERM_CMD_WP_ADD_GROUP)
                .then(CustomArgs.nodeGroupArgument("group")
                    .executesPlayer((player, objects) -> {
                      PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                          .removeNodesFromGroup((NamespacedKey) objects[1], (NodeSelection) objects[0]);
                    })
                )
            )
            .then(CustomArgs.literal("remove")
                .withPermission(PathPerms.PERM_CMD_WP_REMOVE_GROUP)
                .then(CustomArgs.nodeGroupArgument("group")
                    .executesPlayer((player, objects) -> {
                      PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                          .removeNodesFromGroup((NamespacedKey) objects[1], (NodeSelection) objects[0]);
                    })
                )
            )
            .then(CustomArgs.literal("clear")
                .withPermission(PathPerms.PERM_CMD_WP_CLEAR_GROUPS)
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents()
                      .withMessages(player)
                      .build()
                      .clearNodeGroups((NodeSelection) objects[0]);
                })
            )
        )
    );
  }


  private void onInfo(Player player, NodeSelection selection) {

    if (selection.size() == 0) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_N_INFO_NO_SEL, player);
      return;
    }
    if (selection.size() > 1) {
      onList(player, selection, 1);
      return;
    }
    PathFinderAPI.get().getNode(selection.get(0)).thenAccept(node -> {
      FormattedMessage message = Messages.CMD_N_INFO.format(TagResolver.builder()
          .resolver(Placeholder.parsed("id", node.getNodeId().toString()))
          .resolver(
              Placeholder.component("position", Messages.formatVector(node.getLocation().toVector())))
          .resolver(Placeholder.unparsed("world", node.getLocation().getWorld().getName()))
          .resolver(Placeholder.component("edges", Messages.formatNodeSelection(player,
              node.getEdges().stream().map(Edge::getEnd)
                  .collect(Collectors.toCollection(NodeSelection::new)))))
//          .resolver(Placeholder.component("groups", Messages.formatNodeGroups(player,
//              node instanceof Groupable<?> groupable ? groupable.getGroups() : new ArrayList<>())))
          .build());
      TranslationHandler.getInstance().sendMessage(message, player);
    });
  }

  /**
   * Lists all waypoints of a certain selection.
   *
   * @param page first page 1, not 0!
   */
  private void onList(Player player, NodeSelection selection, int page) {

    String selector;
    if (selection.getMeta() != null) {
      selector = selection.getMeta().selector();
    } else {
      selector = "@n";
    }

    TagResolver resolver = Placeholder.parsed("selector", selector);

    CommandUtils.printList(
        player,
        page,
        10,
        new ArrayList<>(selection),
        uuid -> {
          PathFinderAPI.get().getNode(uuid).thenAccept(n -> {
            TagResolver r = TagResolver.builder()
                .tag("id", Tag.preProcessParsed(n.getNodeId() + ""))
                .resolver(Placeholder.component("position",
                    Messages.formatVector(n.getLocation().toVector())))
                .resolver(Placeholder.unparsed("world", n.getLocation().getWorld().getName()))
                .resolver(Placeholder.component("edges", Messages.formatNodeSelection(player,
                    n.getEdges().stream().map(Edge::getEnd)
                        .collect(Collectors.toCollection(NodeSelection::new)))))
//                .resolver(Placeholder.component("groups", Messages.formatNodeGroups(player,
//                    n instanceof Groupable<?> groupable ? groupable.getGroups() : new ArrayList<>())))
                .build();
            TranslationHandler.getInstance()
                .sendMessage(Messages.CMD_N_LIST_ELEMENT.format(r), player);
          });
        },
        Messages.CMD_N_LIST_HEADER.format(resolver),
        Messages.CMD_N_LIST_FOOTER.format(resolver));
  }
}
