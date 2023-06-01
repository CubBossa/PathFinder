package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.NodeUtils;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.LocationType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NodesCmd extends PathFinderSubCommand {

  public NodesCmd(PathFinder pathFinder) {
    super(pathFinder, "nodes");
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

    then(CustomArgs.nodeSelectionArgument("nodes")
        .then(CustomArgs.literal("info")
            .withPermission(PathPerms.PERM_CMD_WP_INFO)
            .executesPlayer((player, args) -> {
              onInfo(player, args.getUnchecked(0));
            })
        )
        .then(CustomArgs.literal("tphere")
            .withPermission(PathPerms.PERM_CMD_WP_TPHERE)
            .executesPlayer((player, args) -> {
              teleportNodes(player, args.getUnchecked(0), BukkitVectorUtils.toInternal(player.getLocation()));
            })
        )
        .then(CustomArgs.literal("tp")
            .withPermission(PathPerms.PERM_CMD_WP_TP)
            .then(CustomArgs.location("location", LocationType.PRECISE_POSITION)
                .executesPlayer((player, args) -> {
                  teleportNodes(player, args.getUnchecked(0), args.getUnchecked(1));
                })
            )
        )
        .then(CustomArgs.literal("connect")
            .withPermission(PathPerms.PERM_CMD_WP_CONNECT)
            .then(CustomArgs.nodeSelectionArgument("end")
                .executesPlayer((player, args) -> {
                  connectNodes(player, args.getUnchecked(0), args.getUnchecked(1));
                })
            )
        )
        .then(CustomArgs.literal("disconnect")
            .withPermission(PathPerms.PERM_CMD_WP_DISCONNECT)
            .then(CustomArgs.nodeSelectionArgument("end")
                .executesPlayer((player, args) -> {
                  disconnectNodes(player, args.getUnchecked(0), args.getUnchecked(1));
                })
            )

        )
        .then(CustomArgs.literal("group")
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
    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_ADD_GROUP.formatted(
        Placeholder.component("nodes", Messages.formatNodeSelection(sender, nodes)),
        Messages.formatter().namespacedKey("group", group.getKey())
    ));
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

      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_REMOVE_GROUP.formatted(
          Placeholder.component("nodes", Messages.formatNodeSelection(sender, nodes))
      ));
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

      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_CLEAR_GROUPS.formatted(
          Placeholder.component("nodes", Messages.formatNodeSelection(sender, nodes))
      ));
    }
  }

  private void disconnectNodes(CommandSender sender, NodeSelection start, NodeSelection end) {
    for (Node s : start) {
      for (Node e : end) {
        s.disconnect(e);
        getPathfinder().getStorage().saveNode(s);
      }
    }
    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_DISCONNECT.formatted(
        Placeholder.component("start", Messages.formatNodeSelection(sender, start)),
        Placeholder.component("end", Messages.formatNodeSelection(sender, end))
    ));
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
    BukkitUtils.wrap(sender).sendMessage(Messages.CMD_N_CONNECT.formatted(
        Placeholder.component("start", Messages.formatNodeSelection(sender, start)),
        Placeholder.component("end", Messages.formatNodeSelection(sender, end))
    ));
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
      NodeUtils.onList(player, selection, Pagination.page(0, 10));
      return;
    }
    Node node = selection.get(0);

    Collection<UUID> neighbours = node.getEdges().stream().map(Edge::getEnd).toList();
    Collection<Node> resolvedNeighbours =
        getPathfinder().getStorage().loadNodes(neighbours).join();

    Message message = Messages.CMD_N_INFO.formatted(
        Messages.formatter().uuid("id", node.getNodeId()),
        Messages.formatter().vector("position", node.getLocation()),
        Placeholder.unparsed("world", node.getLocation().getWorld().getName()),
        Placeholder.component("edges", Messages.formatNodeSelection(player, resolvedNeighbours))
    );
    BukkitUtils.wrap(player).sendMessage(message);
  }
}
