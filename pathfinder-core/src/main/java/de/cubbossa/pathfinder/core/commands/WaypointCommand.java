package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeHandler;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.arguments.LocationType;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WaypointCommand extends Command {

  public WaypointCommand() {
    super("waypoint");
    withAliases("node");
    withGeneratedHelp();

    withRequirement(sender -> sender.hasPermission(PathPlugin.PERM_CMD_WP_INFO)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_LIST)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_CREATE)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_DELETE)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_TPHERE)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_TP)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_CONNECT)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_DISCONNECT)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_SET_CURVE)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_ADD_GROUP)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_REMOVE_GROUP)
        || sender.hasPermission(PathPlugin.PERM_CMD_WP_CLEAR_GROUPS)
    );

    then(CustomArgs.literal("info")
        .withPermission(PathPlugin.PERM_CMD_WP_INFO)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              onInfo(player, (NodeSelection) objects[0]);
            })
        )
    );
    then(CustomArgs.literal("list")
        .withPermission(PathPlugin.PERM_CMD_WP_LIST)
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
        .withPermission(PathPlugin.PERM_CMD_WP_CREATE)
        .executesPlayer((player, objects) -> {
          PathFinderAPI.builder()
              .withEvents().build()
              .messageLayer(player)
              .createNode(NodeHandler.WAYPOINT_TYPE, player.getLocation().add(new Vector(0, 1, 0)));
        })
        .then(CustomArgs.location("location")
            .displayAsOptional()
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                  .withEvents().build()
                  .messageLayer(player)
                  .createNode(NodeHandler.WAYPOINT_TYPE, (Location) objects[1]);
            })
        )
        .then(CustomArgs.nodeTypeArgument("type")
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                  .withEvents().build()
                  .messageLayer(player)
                  .createNode(
                      (NodeType<? extends Node<?>>) objects[0],
                      player.getLocation().add(new Vector(0, 1, 0))
                  );
            })
            .then(CustomArgs.location("location")
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents().build()
                      .messageLayer(player)
                      .createNode(
                          (NodeType<? extends Node<?>>) objects[0],
                          (Location) objects[2]
                      );
                })
            )
        )
    );
    then(CustomArgs.literal("delete")
        .withPermission(PathPlugin.PERM_CMD_WP_DELETE)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                  .withEvents().build()
                  .messageLayer(player)
                  .deleteNodes((NodeSelection) objects[0]);
            })
        )
    );
    then(CustomArgs.literal("tphere")
        .withPermission(PathPlugin.PERM_CMD_WP_TPHERE)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              PathFinderAPI.builder()
                  .withEvents().build()
                  .messageLayer(player)
                  .updateNodes((NodeSelection) objects[0], node -> {
                    node.setLocation(player.getLocation());
                  });
            })
        )
    );
    then(CustomArgs.literal("tp")
        .withPermission(PathPlugin.PERM_CMD_WP_TP)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .then(CustomArgs.location("location", LocationType.PRECISE_POSITION)
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents().build()
                      .messageLayer(player)
                      .updateNodes((NodeSelection) objects[0], node -> {
                        node.setLocation((Location) objects[1]);
                      });
                })
            )
        )
    );
    then(CustomArgs.literal("connect")
        .withPermission(PathPlugin.PERM_CMD_WP_CONNECT)
        .then(CustomArgs.nodeSelectionArgument("start")
            .then(CustomArgs.nodeSelectionArgument("end")
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents().build()
                      .messageLayer(player)
                      .connectNodes((NodeSelection) objects[0], (NodeSelection) objects[1]);
                })
            )
        )
    );
    then(CustomArgs.literal("disconnect")
        .withPermission(PathPlugin.PERM_CMD_WP_DISCONNECT)
        .then(CustomArgs.nodeSelectionArgument("start")
            .executesPlayer((player, objects) -> {
              onDisconnect(player, (NodeSelection) objects[0], null);
            })
            .then(CustomArgs.nodeSelectionArgument("end")
                .displayAsOptional()
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents().build()
                      .messageLayer(player)
                      .disconnectNodes((NodeSelection) objects[0], (NodeSelection) objects[1]);
                })
            )
        )

    );
    then(CustomArgs.literal("group")
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .then(CustomArgs.literal("add")
                .withPermission(PathPlugin.PERM_CMD_WP_ADD_GROUP)
                .then(CustomArgs.nodeGroupArgument("group")
                    .executesPlayer((player, objects) -> {
                      PathFinderAPI.builder()
                          .withEvents().build()
                          .messageLayer(player)
                          .removeNodesFromGroup((NamespacedKey) objects[1], (NodeSelection) objects[0]);
                    })
                )
            )
            .then(CustomArgs.literal("remove")
                .withPermission(PathPlugin.PERM_CMD_WP_REMOVE_GROUP)
                .then(CustomArgs.nodeGroupArgument("group")
                    .executesPlayer((player, objects) -> {
                      PathFinderAPI.builder()
                          .withEvents().build()
                          .messageLayer(player)
                          .removeNodesFromGroup((NamespacedKey) objects[1], (NodeSelection) objects[0]);
                    })
                )
            )
            .then(CustomArgs.literal("clear")
                .withPermission(PathPlugin.PERM_CMD_WP_CLEAR_GROUPS)
                .executesPlayer((player, objects) -> {
                  PathFinderAPI.builder()
                      .withEvents().build()
                      .messageLayer(player)
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
    Node<?> node = selection.get(0);
    FormattedMessage message = Messages.CMD_N_INFO.format(TagResolver.builder()
        .resolver(Placeholder.parsed("id", node.getNodeId().toString()))
        .resolver(
            Placeholder.component("position", Messages.formatVector(node.getLocation().toVector())))
        .resolver(Placeholder.unparsed("world", node.getLocation().getWorld().getName()))
        .resolver(Placeholder.component("edges", Messages.formatNodeSelection(player,
            node.getEdges().stream().map(Edge::getEnd)
                .collect(Collectors.toCollection(NodeSelection::new)))))
        .resolver(Placeholder.component("groups", Messages.formatNodeGroups(player,
            node instanceof Groupable<?> groupable ? groupable.getGroups() : new ArrayList<>())))
        .build());

    TranslationHandler.getInstance().sendMessage(message, player);
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
        n -> {
          TagResolver r = TagResolver.builder()
              .tag("id", Tag.preProcessParsed(n.getNodeId() + ""))
              .resolver(Placeholder.component("position",
                  Messages.formatVector(n.getLocation().toVector())))
              .resolver(Placeholder.unparsed("world", n.getLocation().getWorld().getName()))
              .resolver(Placeholder.component("edges", Messages.formatNodeSelection(player,
                  n.getEdges().stream().map(Edge::getEnd)
                      .collect(Collectors.toCollection(NodeSelection::new)))))
              .resolver(Placeholder.component("groups", Messages.formatNodeGroups(player,
                  n instanceof Groupable<?> groupable ? groupable.getGroups() : new ArrayList<>())))
              .build();
          TranslationHandler.getInstance()
              .sendMessage(Messages.CMD_N_LIST_ELEMENT.format(r), player);
        },
        Messages.CMD_N_LIST_HEADER.format(resolver),
        Messages.CMD_N_LIST_FOOTER.format(resolver));
  }

  private void onConnect(Player player, NodeSelection startSelection, NodeSelection endSelection) {

    for (Node<?> start : startSelection) {
      for (Node<?> end : endSelection) {
        TagResolver resolver = TagResolver.builder()
            .resolver(Placeholder.component("start", Component.text(start.getNodeId().toString())))
            .resolver(Placeholder.component("end", Component.text(end.getNodeId().toString())))
            .build();

        if (start.equals(end)) {
          TranslationHandler.getInstance()
              .sendMessage(Messages.CMD_N_CONNECT_IDENTICAL.format(resolver), player);
          continue;
        }
        if (start.getEdges().stream().anyMatch(edge -> edge.getEnd().equals(end))) {
          TranslationHandler.getInstance()
              .sendMessage(Messages.CMD_N_CONNECT_ALREADY_CONNECTED.format(resolver), player);
          continue;
        }
        TranslationHandler.getInstance()
            .sendMessage(Messages.CMD_N_CONNECT.format(resolver), player);
      }
    }
  }

  private void onDisconnect(Player player, NodeSelection startSelection,
                            @Nullable NodeSelection endSelection) {

    for (Node<?> start : startSelection) {
      if (endSelection == null) {
        continue;
      }
      for (Node<?> end : endSelection) {
        TagResolver resolver = TagResolver.builder()
            .resolver(Placeholder.component("start", Component.text(start.getNodeId().toString())))
            .resolver(Placeholder.component("end", Component.text(end.getNodeId().toString())))
            .build();

        TranslationHandler.getInstance()
            .sendMessage(Messages.CMD_N_DISCONNECT.format(resolver), player);
      }
    }
  }
}
