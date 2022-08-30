package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class WaypointCommand extends CommandTree {

	public WaypointCommand() {
		super("waypoint");
		withPermission(PathPlugin.PERM_CMD_WP);

		withAliases("node");

		then(new LiteralArgument("info")
				.withPermission(PathPlugin.PERM_CMD_WP_INFO)
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.executesPlayer((player, objects) -> {
							onInfo(player, (NodeSelection) objects[0]);
						})
				)
		);
		then(new LiteralArgument("list")
				.withPermission(PathPlugin.PERM_CMD_WP_LIST)
				.executesPlayer((player, objects) -> {
					onList(player, 1);
				})
				.then(new IntegerArgument("page", 1)
						.executesPlayer((player, objects) -> {
							onList(player, (Integer) objects[0]);
						})
				)
		);
		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_WP_CREATE)
				.executesPlayer((player, objects) -> {
					onCreate(player, RoadMapHandler.WAYPOINT_TYPE, player.getLocation().add(new Vector(0, 1, 0)));
				})
				.then(new LocationArgument("location")
						.executesPlayer((player, objects) -> {
							onCreate(player, RoadMapHandler.WAYPOINT_TYPE, (Location) objects[0]);
						})
				)
				.then(CustomArgs.nodeTypeArgument("type")
						.executesPlayer((player, objects) -> {
							onCreate(player, (NodeType<? extends Node>) objects[0], player.getLocation().add(new Vector(0, 1, 0)));
						})
						.then(new LocationArgument("location")
								.executesPlayer((player, objects) -> {
									onCreate(player, (NodeType<? extends Node>) objects[0], (Location) objects[1]);
								})
						)
				)
		);
		then(new LiteralArgument("delete")
				.withPermission(PathPlugin.PERM_CMD_WP_DELETE)
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.executesPlayer((player, objects) -> {
							onDelete(player, (NodeSelection) objects[0]);
						})
				)
		);
		then(new LiteralArgument("tphere")
				.withPermission(PathPlugin.PERM_CMD_WP_TPHERE)
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.executesPlayer((player, objects) -> {
							onTphere(player, (NodeSelection) objects[0]);
						})
				)
		);
		then(new LiteralArgument("tp")
				.withPermission(PathPlugin.PERM_CMD_WP_TP)
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.then(new LocationArgument("location", LocationType.PRECISE_POSITION)
								.executesPlayer((player, objects) -> {
									onTp(player, (NodeSelection) objects[0], (Location) objects[1]);
								})
						)
				)
		);
		then(new LiteralArgument("connect")
				.withPermission(PathPlugin.PERM_CMD_WP_CONNECT)
				.then(CustomArgs.nodeSelectionArgument("start")
						.then(CustomArgs.nodeGroupArgument("end")
								.executesPlayer((player, objects) -> {
									onConnect(player, (NodeSelection) objects[0], (NodeSelection) objects[1]);
								})
						)
				)
		);
		then(new LiteralArgument("disconnect")
				.withPermission(PathPlugin.PERM_CMD_WP_DISCONNECT)
				.then(CustomArgs.nodeSelectionArgument("start")
						.executesPlayer((player, objects) -> {
							onDisconnect(player, (NodeSelection) objects[0], null);
						})
						.then(CustomArgs.nodeGroupArgument("end")
								.executesPlayer((player, objects) -> {
									onDisconnect(player, (NodeSelection) objects[0], (NodeSelection) objects[1]);
								})
						)
				)

		);
		then(new LiteralArgument("set")
				.then(new LiteralArgument("curve-length")
						.withPermission(PathPlugin.PERM_CMD_WP_SET_CURVE)
						.then(CustomArgs.nodeSelectionArgument("nodes")
								.then(new DoubleArgument("length", 0.001)
										.executesPlayer((player, objects) -> {
											onSetTangent(player, (NodeSelection) objects[0], (Double) objects[1]);
										})
								)
						)
				)
		);
	}


	public void onInfo(Player player, NodeSelection selection) {

		if (selection.size() > 1) {
			//TODO selection choice
			return;
		}
		Node node = selection.get(0);
		FormattedMessage message = Messages.CMD_N_INFO.format(TagResolver.builder()
				.tag("id", Tag.preProcessParsed(node.getNodeId() + ""))
				.tag("roadmap", Messages.formatKey(node.getRoadMapKey()))
				.tag("groups", node instanceof Groupable groupable ?
						Tag.inserting(Messages.formatNodeGroups(player, groupable.getGroups())) :
						Tag.inserting(Component.text("none"))) //TODO as message
				.resolver(Placeholder.component("position", Messages.formatVector(node.getLocation().toVector())))
				.tag("curve-length", Tag.preProcessParsed(node.getCurveLength() + ""))
				.tag("edge-count", Tag.preProcessParsed(node.getEdges().size() + ""))
				.build());

		TranslationHandler.getInstance().sendMessage(message, player);
	}

	public void onCreate(Player player, NodeType<? extends Node> type, Location location) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
		Node node = roadMap.createNode(type, location);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CREATE
				.format(TagResolver.resolver("id", Tag.inserting(Component.text(node.getNodeId())))), player);
	}

	public void onDelete(Player player, NodeSelection selection) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
		roadMap.removeNodes(selection);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DELETE
				.format(TagResolver.resolver("selection", Tag.inserting(Messages.formatNodeSelection(player, selection)))), player);
	}

	public void onTphere(Player player, NodeSelection selection) {
		if (selection.size() == 0) {
			return;
		}
		RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(selection.get(0).getRoadMapKey());
		if (roadMap == null) {
			return;
		}
		selection.forEach(node -> roadMap.setNodeLocation(node, player.getLocation()));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_MOVED.format(TagResolver.builder()
				.resolver(Placeholder.component("selection", Messages.formatNodeSelection(player, selection)))
				.resolver(Placeholder.component("location", Messages.formatVector(player.getLocation().toVector())))
				.build()), player);
	}

	public void onTp(Player player, NodeSelection selection, Location location) {

		selection.forEach(node -> node.setLocation(location));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_MOVED.format(TagResolver.builder()
				.resolver(Placeholder.component("selection", Messages.formatNodeSelection(player, selection)))
				.resolver(Placeholder.component("location", Messages.formatVector(location.toVector())))
				.build()), player);
	}

	public void onList(Player player, int pageInput) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

		TagResolver resolver = TagResolver.builder()
				.resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
				.tag("page", Tag.preProcessParsed(pageInput + ""))
				.build();

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_LIST_HEADER.format(resolver), player);

		PathPlugin.getInstance().getAudiences().player(player).sendMessage(Component.join(
				JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
				CommandUtils.subList(new ArrayList<>(roadMap.getNodes()), pageInput, 40).stream()
						.map(n -> {

							TagResolver r = TagResolver.builder()
									.tag("id", Tag.preProcessParsed(n.getNodeId() + ""))
									.resolver(Placeholder.component("position", Messages.formatVector(n.getLocation().toVector())))
									.tag("groups", n instanceof Groupable groupable ?
											Tag.inserting(Messages.formatNodeGroups(player, groupable.getGroups())) :
											Tag.inserting(Component.text("none"))) //TODO as message
									.build();

							return TranslationHandler.getInstance().translateLine(Messages.CMD_N_LIST_ELEMENT.format(resolver, r), player);
						})
						.toList()));
		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_LIST_FOOTER.format(resolver), player);
	}

	public void onConnect(Player player, NodeSelection startSelection, NodeSelection endSelection) {

		for (Node start : startSelection) {
			for (Node end : endSelection) {
				TagResolver resolver = TagResolver.builder()
						.resolver(Placeholder.component("start", Component.text(start.getNodeId())))
						.resolver(Placeholder.component("end", Component.text(end.getNodeId())))
						.build();

				if (start.equals(end)) {
					TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT_IDENTICAL.format(resolver), player);
					continue;
				}
				if (start.getEdges().stream().anyMatch(edge -> edge.getEnd().equals(end))) {
					TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT_ALREADY_CONNECTED.format(resolver), player);
					continue;
				}
				start.connect(end);
				TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT.format(resolver), player);
			}
		}
	}

	public void onDisconnect(Player player, NodeSelection startSelection, @Nullable NodeSelection endSelection) {

		for (Node start : startSelection) {
			if (endSelection == null) {
				RoadMapHandler.getInstance().getRoadMap(start.getRoadMapKey()).disconnectNode(start);
				continue;
			}
			for (Node end : endSelection) {
				TagResolver resolver = TagResolver.builder()
						.resolver(Placeholder.component("start", Component.text(start.getNodeId())))
						.resolver(Placeholder.component("end", Component.text(end.getNodeId())))
						.build();

				start.disconnect(end);
				TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DISCONNECT.format(resolver), player);
			}
		}
	}

	public void onSetTangent(Player player, NodeSelection selection, Double strength) {
		selection.forEach(node -> node.setCurveLength(strength));
		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_TANGENT.format(TagResolver.builder()
				.resolver(Placeholder.component("selection", Messages.formatNodeSelection(player, selection)))
				.resolver(Placeholder.component("length", Component.text(strength)))
				.build()), player);
	}

		/*TODO @Subcommand("group")
		@Syntax("<nodes> <group>")
		@CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION + " null|" + PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
		public void onSetGroup(CommandSender sender, NodeSelection selection, @Single NamespacedKey key) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);

			TagResolver resolver = TagResolver.builder()
					.resolver(Placeholder.component("selection", Messages.formatNodeSelection(selection)))
					.resolver(Placeholder.component("group", Messages.formatKey(key)))
					.build();

			NodeGroup group = roadMap.getNodeGroup(key);
			if (group == null) {
				TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_GROUP_UNKNOWN.format(resolver), sender);
				return;
			}
			selection.forEach(node -> node.setGroupKey(key));
			TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_GROUP.format(resolver), sender);
		}*/
}
