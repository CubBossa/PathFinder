package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.LocationType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
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
          onCreate(player, NodeHandler.WAYPOINT_TYPE,
              player.getLocation().add(new Vector(0, 1, 0)));
        })
        .then(CustomArgs.location("location")
            .displayAsOptional()
            .executesPlayer((player, objects) -> {
              onCreate(player, NodeHandler.WAYPOINT_TYPE,
                  (Location) objects[1]);
            })
        )
        .then(CustomArgs.nodeTypeArgument("type")
            .executesPlayer((player, objects) -> {
              onCreate(player, (NodeType<? extends Node<?>>) objects[0],
                  player.getLocation().add(new Vector(0, 1, 0)));
            })
            .then(CustomArgs.location("location")
                .executesPlayer((player, objects) -> {
                  onCreate(player, (NodeType<? extends Node<?>>) objects[0],
                      (Location) objects[2]);
                })
            )
        )
    );
    then(CustomArgs.literal("delete")
        .withPermission(PathPlugin.PERM_CMD_WP_DELETE)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              onDelete(player, (NodeSelection) objects[0]);
            })
        )
    );
    then(CustomArgs.literal("tphere")
        .withPermission(PathPlugin.PERM_CMD_WP_TPHERE)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, objects) -> {
              onTp(player, (NodeSelection) objects[0], player.getLocation());
            })
        )
    );
    then(CustomArgs.literal("tp")
        .withPermission(PathPlugin.PERM_CMD_WP_TP)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .then(CustomArgs.location("location", LocationType.PRECISE_POSITION)
                .executesPlayer((player, objects) -> {
                  onTp(player, (NodeSelection) objects[0], (Location) objects[1]);
                })
            )
        )
    );
    then(CustomArgs.literal("connect")
        .withPermission(PathPlugin.PERM_CMD_WP_CONNECT)
        .then(CustomArgs.nodeSelectionArgument("start")
            .then(CustomArgs.nodeSelectionArgument("end")
                .executesPlayer((player, objects) -> {
                  onConnect(player, (NodeSelection) objects[0], (NodeSelection) objects[1]);
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
                  onDisconnect(player, (NodeSelection) objects[0], (NodeSelection) objects[1]);
                })
            )
        )

    );
    then(CustomArgs.literal("edit")
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .then(CustomArgs.literal("curve-length")
                .withPermission(PathPlugin.PERM_CMD_WP_SET_CURVE)
                .then(new DoubleArgument("length", 0.001)
                    .executesPlayer((player, objects) -> {
                      onSetTangent(player, (NodeSelection) objects[0], (Double) objects[1]);
                    })
                )
            )
            .then(CustomArgs.literal("reset-curve-length")
                .withPermission(PathPlugin.PERM_CMD_WP_SET_CURVE)
                .executesPlayer((player, objects) -> {
                  onSetTangent(player, (NodeSelection) objects[0], null);
                }))
            .then(CustomArgs.literal("addgroup")
                .withPermission(PathPlugin.PERM_CMD_WP_ADD_GROUP)
                .then(CustomArgs.nodeGroupArgument("group")
                    .executesPlayer((player, objects) -> {
                      onAddGroup(player, (NodeSelection) objects[0], (NodeGroup) objects[1]);
                    })
                )
            )
            .then(CustomArgs.literal("removegroup")
                .withPermission(PathPlugin.PERM_CMD_WP_REMOVE_GROUP)
                .then(CustomArgs.nodeGroupArgument("group")
                    .executesPlayer((player, objects) -> {
                      onRemoveGroup(player, (NodeSelection) objects[0], (NodeGroup) objects[1]);
                    })
                )
            )
            .then(CustomArgs.literal("cleargroups")
                .withPermission(PathPlugin.PERM_CMD_WP_CLEAR_GROUPS)
                .executesPlayer((player, objects) -> {
                  onClearGroups(player, (NodeSelection) objects[0]);
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

  private void onCreate(Player player, NodeType<? extends Node<?>> type, Location location) {
    Node<?> node = NodeHandler.getInstance().createNode(type, location, true);

    TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CREATE
            .format(TagResolver.resolver("id", Tag.inserting(Component.text(node.getNodeId().toString())))),
        player);
  }

  private void onDelete(Player player, NodeSelection selection) {
  NodeHandler.getInstance().removeNodes(selection);
    TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DELETE
        .format(TagResolver.resolver("selection",
            Tag.inserting(Messages.formatNodeSelection(player, selection)))), player);
  }

  private void onTp(Player player, NodeSelection selection, Location location) {

    if (selection.size() == 0) {
      return;
    }
    NodeHandler.getInstance().setNodeLocation(selection, location);

    TranslationHandler.getInstance().sendMessage(Messages.CMD_N_MOVED.format(TagResolver.builder()
        .resolver(
            Placeholder.component("selection", Messages.formatNodeSelection(player, selection)))
        .resolver(Placeholder.component("location", Messages.formatVector(location.toVector())))
        .build()), player);
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
        NodeHandler.getInstance().connectNodes(start, end);
        TranslationHandler.getInstance()
            .sendMessage(Messages.CMD_N_CONNECT.format(resolver), player);
      }
    }
  }

  private void onDisconnect(Player player, NodeSelection startSelection,
                            @Nullable NodeSelection endSelection) {

    for (Node<?> start : startSelection) {
      if (endSelection == null) {
        NodeHandler.getInstance().disconnectNode(start);
        continue;
      }
      for (Node<?> end : endSelection) {
        TagResolver resolver = TagResolver.builder()
            .resolver(Placeholder.component("start", Component.text(start.getNodeId().toString())))
            .resolver(Placeholder.component("end", Component.text(end.getNodeId().toString())))
            .build();

        NodeHandler.getInstance().disconnectNodes(start, end);
        TranslationHandler.getInstance()
            .sendMessage(Messages.CMD_N_DISCONNECT.format(resolver), player);
      }
    }
  }

  private void onSetTangent(Player player, NodeSelection selection, Double strength) {
    NodeHandler.getInstance().setNodeCurveLength(selection, strength);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_N_SET_TANGENT.format(TagResolver.builder()
            .resolver(
                Placeholder.component("selection", Messages.formatNodeSelection(player, selection)))
            .resolver(strength == null
                ? Placeholder.component("length", Component.text("inherited"))
                : // TODO message instead
                    Formatter.number("length", strength))
            .build()), player);
  }

  private void onAddGroup(Player player, NodeSelection selection, NodeGroup group) {
    PathPlugin.
    NodeGroupHandler.getInstance().addNodes(group, selection.stream()
        .filter(node -> node instanceof Groupable<?>)
        .map(n -> (Groupable<?>) n)
        .collect(Collectors.toSet()));

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_N_ADD_GROUP.format(TagResolver.builder()
            .resolver(Placeholder.component("nodes", Messages.formatNodeSelection(player, selection)))
            .build()), player);
  }

  private void onRemoveGroup(Player player, NodeSelection selection, NodeGroup group) {


    NodeGroupHandler.getInstance().removeNodes(group, selection.stream()
        .filter(node -> node instanceof Groupable<?>)
        .map(n -> (Groupable<?>) n)
        .collect(Collectors.toSet()));

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_N_REMOVE_GROUP.format(TagResolver.builder()
            .resolver(Placeholder.component("nodes", Messages.formatNodeSelection(player, selection)))
            .build()), player);
  }

  private void onClearGroups(Player player, NodeSelection selection) {
    Collection<Groupable<?>> groupables = selection.stream()
        .filter(node -> node instanceof Groupable)
        .map(n -> (Groupable<?>) n)
        .collect(Collectors.toSet());
    NodeGroupHandler.getInstance().removeNodes(
        groupables.stream().flatMap(groupable -> groupable.getGroups().stream())
            .collect(Collectors.toSet()), groupables);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_N_CLEAR_GROUPS.format(TagResolver.builder()
            .resolver(
                Placeholder.component("nodes", Messages.formatNodeSelection(player, selection)))
            .build()), player);
  }


}
