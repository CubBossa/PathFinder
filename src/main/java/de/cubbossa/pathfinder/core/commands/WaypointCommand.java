package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

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
				.then(CustomArgs.roadMapArgument("roadmap")
						.executesPlayer((player, objects) -> {
							onList(player, (RoadMap) objects[0], 1);
						})
						.then(new IntegerArgument("page", 1)
								.executesPlayer((player, objects) -> {
									onList(player, (RoadMap) objects[0], (Integer) objects[1]);
								})
						)
				)
		);
		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_WP_CREATE)
				.then(CustomArgs.roadMapArgument("roadmap")
						.executesPlayer((player, objects) -> {
							onCreate(player, (RoadMap) objects[0], RoadMapHandler.WAYPOINT_TYPE, player.getLocation().add(new Vector(0, 1, 0)));
						})
						.then(new LocationArgument("location")
								.executesPlayer((player, objects) -> {
									onCreate(player, (RoadMap) objects[0], RoadMapHandler.WAYPOINT_TYPE, (Location) objects[1]);
								})
						)
						.then(CustomArgs.nodeTypeArgument("type")
								.executesPlayer((player, objects) -> {
									onCreate(player, (RoadMap) objects[0], (NodeType<? extends Node>) objects[1], player.getLocation().add(new Vector(0, 1, 0)));
								})
								.then(new LocationArgument("location")
										.executesPlayer((player, objects) -> {
											onCreate(player, (RoadMap) objects[0], (NodeType<? extends Node>) objects[1], (Location) objects[2]);
										})
								)
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
							onTp(player, (NodeSelection) objects[0], player.getLocation());
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
						.then(CustomArgs.nodeSelectionArgument("end")
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
						.then(CustomArgs.nodeSelectionArgument("end")
								.executesPlayer((player, objects) -> {
									onDisconnect(player, (NodeSelection) objects[0], (NodeSelection) objects[1]);
								})
						)
				)

		);
		then(new LiteralArgument("edit")
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.then(new LiteralArgument("curve-length")
								.withPermission(PathPlugin.PERM_CMD_WP_SET_CURVE)
								.then(new DoubleArgument("length", 0.001)
										.executesPlayer((player, objects) -> {
											onSetTangent(player, (NodeSelection) objects[0], (Double) objects[1]);
										})
								)
						)
						.then(new LiteralArgument("addgroup")
								.then(CustomArgs.nodeGroupArgument("group")
										.executesPlayer((player, objects) -> {
											onAddGroup(player, (NodeSelection) objects[0], (NodeGroup) objects[1]);
										})
								)
						)
						.then(new LiteralArgument("removegroup")
								.then(CustomArgs.nodeGroupArgument("group")
										.executesPlayer((player, objects) -> {
											onRemoveGroup(player, (NodeSelection) objects[0], (NodeGroup) objects[1]);
										})
								)
						)
						.then(new LiteralArgument("cleargroups")
								.executesPlayer((player, objects) -> {
									onClearGroups(player, (NodeSelection) objects[0]);
								})
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

	public void onCreate(Player player, RoadMap roadMap, NodeType<? extends Node> type, Location location) {
		Node node = roadMap.createNode(type, location);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CREATE
				.format(TagResolver.resolver("id", Tag.inserting(Component.text(node.getNodeId())))), player);
	}

	public void onDelete(Player player, NodeSelection selection) {
		for (RoadMap roadMap : RoadMapHandler.getInstance().getRoadMaps()) {
			roadMap.removeNodes(selection);
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DELETE
				.format(TagResolver.resolver("selection", Tag.inserting(Messages.formatNodeSelection(player, selection)))), player);
	}

	public void onTp(Player player, NodeSelection selection, Location location) {

		if (selection.size() == 0) {
			return;
		}
		RoadMapHandler.getInstance().setNodeLocation(selection, location);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_MOVED.format(TagResolver.builder()
				.resolver(Placeholder.component("selection", Messages.formatNodeSelection(player, selection)))
				.resolver(Placeholder.component("location", Messages.formatVector(location.toVector())))
				.build()), player);
	}

	/**
	 * @param pageInput starts with 1, not 0!
	 */
	public void onList(Player player, RoadMap roadMap, int pageInput) {

		TagResolver resolver = TagResolver.builder()
				.resolver(Placeholder.parsed("roadmap-key", roadMap.getKey().toString()))
				.resolver(Placeholder.component("roadmap-name", roadMap.getDisplayName()))
				.build();

		CommandUtils.printList(
				player,
				pageInput,
				10,
				new ArrayList<>(roadMap.getNodes()),
				n -> {
					TagResolver r = TagResolver.builder()
							.tag("id", Tag.preProcessParsed(n.getNodeId() + ""))
							.resolver(Placeholder.component("position", Messages.formatVector(n.getLocation().toVector())))
							.resolver(Placeholder.unparsed("world", n.getLocation().getWorld().getName()))
							.resolver(Formatter.number("curve-length", n.getCurveLength() == null ?
									roadMap.getDefaultBezierTangentLength() : n.getCurveLength()))
							.resolver(Placeholder.component("edges", Messages.formatNodeSelection(player, n.getEdges().stream().map(Edge::getEnd).collect(Collectors.toCollection(NodeSelection::new)))))
							.resolver(Placeholder.component("groups", Messages.formatNodeGroups(player, n instanceof Groupable groupable ? groupable.getGroups() : new ArrayList<>())))
							.build();
					TranslationHandler.getInstance().sendMessage(Messages.CMD_N_LIST_ELEMENT.format(r), player);
				},
				Messages.CMD_N_LIST_HEADER.format(resolver),
				Messages.CMD_N_LIST_FOOTER.format(resolver));
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
		RoadMapHandler.getInstance().setNodeCurveLength(selection, strength);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_TANGENT.format(TagResolver.builder()
				.resolver(Placeholder.component("selection", Messages.formatNodeSelection(player, selection)))
				.resolver(Placeholder.component("length", Component.text(strength)))
				.build()), player);
	}

	public void onAddGroup(Player player, NodeSelection selection, NodeGroup group) {
		NodeGroupHandler.getInstance().addNodes(group, selection.stream()
				.filter(node -> node instanceof Groupable)
				.map(n -> (Groupable) n)
				.collect(Collectors.toSet()));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_ADD_GROUP.format(TagResolver.builder()
				.resolver(Placeholder.component("nodes", Messages.formatNodeSelection(player, selection)))
				.resolver(Placeholder.component("group", group.getDisplayName()))
				.build()), player);
	}

	public void onRemoveGroup(Player player, NodeSelection selection, NodeGroup group) {
		NodeGroupHandler.getInstance().removeNodes(group, selection.stream()
				.filter(node -> node instanceof Groupable)
				.map(n -> (Groupable) n)
				.collect(Collectors.toSet()));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_REMOVE_GROUP.format(TagResolver.builder()
				.resolver(Placeholder.component("nodes", Messages.formatNodeSelection(player, selection)))
				.resolver(Placeholder.component("group", group.getDisplayName()))
				.build()), player);
	}

	public void onClearGroups(Player player, NodeSelection selection) {
		Collection<Groupable> groupables = selection.stream()
				.filter(node -> node instanceof Groupable)
				.map(n -> (Groupable) n)
				.collect(Collectors.toSet());
		NodeGroupHandler.getInstance().removeNodes(groupables.stream().flatMap(groupable -> groupable.getGroups().stream()).collect(Collectors.toSet()), groupables);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CLEAR_GROUPS.format(TagResolver.builder()
				.resolver(Placeholder.component("nodes", Messages.formatNodeSelection(player, selection)))
				.build()), player);
	}


}
