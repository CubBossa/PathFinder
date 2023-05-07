package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.VectorUtils;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.LocationType;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class WaypointCommand extends Command {

  public WaypointCommand(PathFinder pathFinder,
                         Supplier<NodeType<?>> fallbackWaypointType) {
    super(pathFinder, "waypoint");
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
            .executesPlayer((player, args) -> {
              onInfo(player, args.getUnchecked(0));
            })
        )
    );
    then(CustomArgs.literal("list")
        .withPermission(PathPerms.PERM_CMD_WP_LIST)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, args) -> {
              onList(player, args.getUnchecked(0), 1);
            })
            .then(CustomArgs.integer("page", 1)
                .displayAsOptional()
                .executesPlayer((player, args) -> {
                  onList(player, args.getUnchecked(0), args.getUnchecked(1));
                })
            ))
    );
    then(CustomArgs.literal("create")
        .withPermission(PathPerms.PERM_CMD_WP_CREATE)
        .executesPlayer((player, args) -> {
          createNode(player, fallbackWaypointType.get(),
              VectorUtils.toInternal(player.getLocation()));
        })
        .then(CustomArgs.location("location")
            .displayAsOptional()
            .executesPlayer((player, args) -> {
              createNode(player, fallbackWaypointType.get(), args.getUnchecked(0));
            })
        )
        .then(CustomArgs.nodeTypeArgument("type")
            .executesPlayer((player, args) -> {
              createNode(player, args.getUnchecked(0),
                  VectorUtils.toInternal(player.getLocation()));
            })
            .then(CustomArgs.location("location")
                .executesPlayer((player, args) -> {
                  createNode(player, args.getUnchecked(0),
                      args.getUnchecked(1));
                })
            )
        )
    );
    then(CustomArgs.literal("delete")
        .withPermission(PathPerms.PERM_CMD_WP_DELETE)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, args) -> {
              deleteNode(player, args.getUnchecked(0));
            })
        )
    );
    then(CustomArgs.literal("tphere")
        .withPermission(PathPerms.PERM_CMD_WP_TPHERE)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .executesPlayer((player, args) -> {
              teleportNodes(player, args.getUnchecked(0),
                  VectorUtils.toInternal(player.getLocation()));
            })
        )
    );
    then(CustomArgs.literal("tp")
        .withPermission(PathPerms.PERM_CMD_WP_TP)
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .then(CustomArgs.location("location", LocationType.PRECISE_POSITION)
                .executesPlayer((player, args) -> {
                  teleportNodes(player, args.getUnchecked(0), args.getUnchecked(1));
                })
            )
        )
    );
    then(CustomArgs.literal("connect")
        .withPermission(PathPerms.PERM_CMD_WP_CONNECT)
        .then(CustomArgs.nodeSelectionArgument("start")
            .then(CustomArgs.nodeSelectionArgument("end")
                .executesPlayer((player, args) -> {
                  connectNodes(player, args.getUnchecked(0), args.getUnchecked(1));
                })
            )
        )
    );
    then(CustomArgs.literal("disconnect")
        .withPermission(PathPerms.PERM_CMD_WP_DISCONNECT)
        .then(CustomArgs.nodeSelectionArgument("start")
            .then(CustomArgs.nodeSelectionArgument("end")
                .executesPlayer((player, args) -> {
                  disconnectNodes(player, args.getUnchecked(0), args.getUnchecked(1));
                })
            )
        )

    );
    then(CustomArgs.literal("group")
        .then(CustomArgs.nodeSelectionArgument("nodes")
            .then(CustomArgs.literal("add")
                .withPermission(PathPerms.PERM_CMD_WP_ADD_GROUP)
                .then(CustomArgs.nodeGroupArgument("group")
                    .executesPlayer((player, args) -> {
                      addGroup(player, args.getUnchecked(0), args.getUnchecked(1));
                    })
                )
            )
            .then(CustomArgs.literal("remove")
                .withPermission(PathPerms.PERM_CMD_WP_REMOVE_GROUP)
                .then(CustomArgs.nodeGroupArgument("group")
                    .executesPlayer((player, args) -> {
                      removeGroup(player, args.getUnchecked(0), args.getUnchecked(1));
                    })
                )
            )
            .then(CustomArgs.literal("clear")
                .withPermission(PathPerms.PERM_CMD_WP_CLEAR_GROUPS)
                .executesPlayer((player, args) -> {
                  clearGroups(player, args.getUnchecked(0));
                })
            )
        )
    );
  }

  private void addGroup(CommandSender sender, NodeSelection nodes, SimpleNodeGroup group) {
    for (Node node : nodes) {
      if (!(node instanceof Groupable groupable)) {
        continue;
      }
      groupable.addGroup(group);
      getPathfinder().getStorage().saveNode(node);

    }
    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_ADD_GROUP.formatted(TagResolver.builder()
        .resolver(Placeholder.component("nodes", Messages.formatNodeSelection(sender, nodes)))
        .tag("group", Messages.formatKey(group.getKey()))
        .build()));
  }

  private void removeGroup(CommandSender sender, NodeSelection nodes, SimpleNodeGroup group) {
    for (Node node : nodes) {
      if (!(node instanceof Groupable groupable)) {
        continue;
      }
      if (!groupable.getGroups().contains(group)) {
        continue;
      }
      groupable.removeGroup(group.getKey());
      getPathfinder().getStorage().saveNode(node);

      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_REMOVE_GROUP.formatted(TagResolver.builder()
          .resolver(Placeholder.component("nodes", Messages.formatNodeSelection(sender, nodes)))
          .build()));
    }
  }

  private void clearGroups(CommandSender sender, NodeSelection nodes) {
    for (Node node : nodes) {
      if (!(node instanceof Groupable groupable)) {
        continue;
      }
      if (groupable.getGroups().isEmpty()) {
        continue;
      }
      groupable.clearGroups();
      getPathfinder().getStorage().saveNode(node);

      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_CLEAR_GROUPS.formatted(TagResolver.builder().resolver(
              Placeholder.component("nodes", Messages.formatNodeSelection(sender, nodes)))
          .build()));
    }
  }

  private void disconnectNodes(CommandSender sender, NodeSelection start, NodeSelection end) {
    for (Node s : start) {
      for (Node e : end) {
        s.disconnect(e);
        getPathfinder().getStorage().saveNode(s);
      }
    }
  }

  private void connectNodes(CommandSender sender, NodeSelection start, NodeSelection end) {
    for (Node s : start) {
      for (Node e : end) {
        if (s.equals(e)) {
          continue;
        }
        if (s.hasConnection(e)) {
          continue;
        }
        s.connect(e);
        getPathfinder().getStorage().saveNode(s);
      }
    }
  }

  private void createNode(CommandSender sender, NodeType<?> type,
                          Location location) {
    getPathfinder().getStorage().createAndLoadNode(
        type,
        location
    ).thenAccept(n -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_CREATE.formatted(
          Placeholder.parsed("id", n.getNodeId().toString())
      ));
    });
  }

  private void deleteNode(CommandSender sender, NodeSelection nodes) {
    getPathfinder().getStorage().deleteNodes(nodes).thenRun(() -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_DELETE.formatted(
          Placeholder.component("selection", Messages.formatNodeSelection(sender, nodes))
      ));
    });
  }

  private void teleportNodes(CommandSender sender, NodeSelection nodes, Location location) {
    Collection<CompletableFuture<?>> futures = new HashSet<>();
    for (Node node : nodes) {
      node.setLocation(location);
      futures.add(getPathfinder().getStorage().saveNode(node));
    }
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_UPDATED.formatted(
          Placeholder.component("selection", Messages.formatNodeSelection(sender, nodes))
      ));
    });
  }

  private void onInfo(Player player, NodeSelection selection) {

    if (selection.size() == 0) {
      BukkitUtils.wrap(player).sendMessage(Messages.CMD_N_INFO_NO_SEL);
      return;
    }
    if (selection.size() > 1) {
      onList(player, selection, 1);
      return;
    }
    Node node = selection.get(0);

    Collection<UUID> neighbours = node.getEdges().stream().map(Edge::getEnd).toList();
    Collection<Node> resolvedNeighbours =
        getPathfinder().getStorage().loadNodes(neighbours).join();

    Message message = Messages.CMD_N_INFO.formatted(TagResolver.builder()
        .resolver(Placeholder.parsed("id", node.getNodeId().toString()))
        .resolver(
            Placeholder.component("position",
                Messages.formatVector(node.getLocation())))
        .resolver(Placeholder.unparsed("world", node.getLocation().getWorld().getName()))
        .resolver(Placeholder.component("edges",
            Messages.formatNodeSelection(player, resolvedNeighbours)))
//          .resolver(Placeholder.component("groups", Messages.formatNodeGroups(player,
//              node instanceof Groupable groupable ? groupable.getGroups() : new ArrayList<>())))
        .build());
    BukkitUtils.wrap(player).sendMessage(message);
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
          Collection<UUID> neighbours = n.getEdges().stream().map(Edge::getEnd).toList();
          Collection<Node> resolvedNeighbours =
              getPathfinder().getStorage().loadNodes(neighbours).join();

          TagResolver r = TagResolver.builder()
              .tag("id", Tag.preProcessParsed(n.getNodeId() + ""))
              .resolver(Placeholder.component("position",
                  Messages.formatVector(n.getLocation())))
              .resolver(Placeholder.unparsed("world", n.getLocation().getWorld().getName()))
              .resolver(Placeholder.component("edges",
                  Messages.formatNodeSelection(player, resolvedNeighbours)))
//                .resolver(Placeholder.component("groups", Messages.formatNodeGroups(player,
//                    n instanceof Groupable groupable ? groupable.getGroups() : new ArrayList<>())))
              .build();
          BukkitUtils.wrap(player).sendMessage(Messages.CMD_N_LIST_ELEMENT.formatted(r));
        },
        Messages.CMD_N_LIST_HEADER.formatted(resolver),
        Messages.CMD_N_LIST_FOOTER.formatted(resolver));
  }
}
