package de.cubbossa.pathfinder.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.CustomArgument;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SelectionUtils {

	@Getter
	public static class PlayerContext extends SelectionParser.Context {

		private final Player player;

		public PlayerContext(String value, Player player) {
			super(value);
			this.player = player;
		}
	}

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_ID = new SelectionParser.Filter<>("id", Pattern.compile("[0-9]+"), (elements, context) -> {
		try {
			Integer id = Integer.parseInt(context.value());
			return elements.stream().filter(node -> node.getNodeId() == id).collect(Collectors.toSet());
		} catch (NumberFormatException e) {
			throw new SelectionParser.FilterException("ID must be a number.");
		}
	}, context -> RoadMapHandler.getInstance().getRoadMaps().values().stream()
			.map(RoadMap::getNodes)
			.flatMap(Collection::stream)
			.map(Node::getNodeId)
			.map(integer -> integer + "")
			.collect(Collectors.toList()));

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_TANGENT_LENGTH = new SelectionParser.Filter<>("tangent_length", Pattern.compile("(\\.\\.)?[0-9]*(\\.[0-9]+)?(\\.\\.)?"), (nodes, context) -> {
		boolean smaller = context.value().startsWith("..");
		boolean larger = context.value().endsWith("..");
		if (smaller && larger) {
			return nodes;
		}
		String arg = context.value();
		if (smaller) {
			arg = arg.substring(2);
		}
		if (larger) {
			arg = arg.substring(0, arg.length() - 2);
		}
		float req = Float.parseFloat(arg);
		return nodes.stream().filter(node -> {
			double dist = node.getLocation().distance(context.getPlayer().getLocation());
			return smaller && dist <= req || larger && dist >= req || dist == req;
		}).collect(Collectors.toList());
	}, context -> Lists.newArrayList("..1", "1.5", "2.."));

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_DISTANCE = new SelectionParser.Filter<>("distance", Pattern.compile("(\\.\\.)?[0-9]*(\\.[0-9]+)?(\\.\\.)?"), (nodes, context) -> {
		boolean smaller = context.value().startsWith("..");
		boolean larger = context.value().endsWith("..");
		if (smaller && larger) {
			return nodes;
		}
		String arg = context.value();
		if (smaller) {
			arg = arg.substring(2);
		}
		if (larger) {
			arg = arg.substring(0, arg.length() - 2);
		}
		float req = Float.parseFloat(arg);
		return nodes.stream().filter(node -> {
			double dist = node.getLocation().distance(context.getPlayer().getLocation());
			return smaller && dist <= req || larger && dist >= req || dist == req;
		}).collect(Collectors.toList());
	}, context -> Lists.newArrayList("..1", "1.5", "2.."));

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_ROADMAP = new SelectionParser.Filter<>("roadmap", Pattern.compile("[a-z0-9_]+:[a-z0-9_]+"), (nodes, context) -> {
		NamespacedKey key = NamespacedKey.fromString(context.value());
		if (key == null) {
			throw new SelectionParser.FilterException("Invalid namespaced key: '" + key + "'.");
		}
		return nodes.stream().filter(node -> node.getRoadMapKey().equals(key)).collect(Collectors.toSet());
	}, c -> RoadMapHandler.getInstance().getRoadMaps().values().stream().map(RoadMap::getKey).map(Object::toString).toList());

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_WORLD = new SelectionParser.Filter<>("world", Pattern.compile("[a-zA-Z0-9_]+"), (nodes, playerContext) -> {
		World world = Bukkit.getWorld(playerContext.value());
		if (world == null) {
			throw new SelectionParser.FilterException("'" + playerContext.value() + "' is not a valid world.");
		}
		return nodes.stream().filter(node -> node.getLocation().getWorld().equals(world)).collect(Collectors.toSet());
	}, c -> Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toSet()));

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_LIMIT = new SelectionParser.Filter<>("limit", Pattern.compile("[0-9]+"),
			(nodes, context) -> {
				try {
					int input = Integer.parseInt(context.value());
					return CommandUtils.subListPaginated(new ArrayList<>(nodes), 0, input);
				} catch (Exception e) {
					throw new SelectionParser.FilterException("Invalid number input: '" + context.value() + "'");
				}
			},
			context -> IntStream.range(1, 10).mapToObj(i -> "" + i).toList());

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_OFFSET = new SelectionParser.Filter<>("offset", Pattern.compile("[0-9]+"),
			(nodes, context) -> {
				try {
					int input = Integer.parseInt(context.value());
					return CommandUtils.subList(new ArrayList<>(nodes), input);
				} catch (Exception e) {
					throw new SelectionParser.FilterException("Invalid number input: '" + context.value() + "'");
				}
			},
			context -> IntStream.range(1, 10).mapToObj(i -> "" + i).toList());

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_SORT = new SelectionParser.Filter<>("sort", Pattern.compile("(nearest|furthest|random|arbitrary)"), (nodes, context) -> {
		Location pLoc = context.getPlayer().getLocation();
		return switch (context.value()) {
			case "nearest" ->
					nodes.stream().sorted(Comparator.comparingDouble(o -> o.getLocation().distance(pLoc))).collect(Collectors.toList());
			case "furthest" -> nodes.stream()
					.sorted((o1, o2) -> Double.compare(o2.getLocation().distance(pLoc), o1.getLocation().distance(pLoc)))
					.collect(Collectors.toList());
			case "random" -> nodes.stream().collect(Collectors.collectingAndThen(Collectors.toList(), n -> {
				Collections.shuffle(n);
				return n;
			}));
			case "arbitrary" ->
					nodes.stream().sorted(Comparator.comparingInt(Node::getNodeId)).collect(Collectors.toList());
			default ->
					throw new SelectionParser.FilterException("Invalid sorting parameter: '" + context.value() + "'.");
		};
	}, c -> Lists.newArrayList("nearest", "furthest", "random", "arbitrary"));

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_GROUP = new SelectionParser.Filter<>("group", Pattern.compile("[a-z0-9_]+:[a-z0-9_]+"), (nodes, playerContext) -> {
		NamespacedKey key = NamespacedKey.fromString(playerContext.value());
		if (key == null) {
			throw new SelectionParser.FilterException("Invalid namespaced key: '" + key + "'.");
		}
		NodeGroup group = NodeGroupHandler.getInstance().getNodeGroup(key);
		if (group == null) {
			throw new SelectionParser.FilterException("There is no group with the key '" + key + "'");
		}
		return nodes.stream().filter(node -> node instanceof Groupable groupable && group.contains(groupable)).collect(Collectors.toSet());
	}, c -> NodeGroupHandler.getInstance().getNodeGroups().stream().map(NodeGroup::getKey).map(NamespacedKey::toString).collect(Collectors.toSet()));

	public static final String SELECT_KEY_EDGE = "has_edge";

	public static final ArrayList<SelectionParser.Filter<Node, PlayerContext>> SELECTORS = Lists.newArrayList(
			SELECT_KEY_ID, SELECT_KEY_OFFSET, SELECT_KEY_LIMIT, SELECT_KEY_DISTANCE, SELECT_KEY_TANGENT_LENGTH,
			SELECT_KEY_SORT, SELECT_KEY_WORLD, SELECT_KEY_ROADMAP, SELECT_KEY_GROUP
	);

	public static NodeSelection getNodeSelection(Player player, String selectString) throws CustomArgument.CustomArgumentException {
		return new SelectionParser<>(SELECTORS, string -> new PlayerContext(string, player), "n", "node")
				.parseSelection(RoadMapHandler.getInstance().getRoadMaps().values().stream()
						.flatMap(roadMap -> roadMap.getNodes().stream()).collect(Collectors.toSet()), selectString, NodeSelection::new);
	}

	public static CompletableFuture<Suggestions> getNodeSelectionSuggestions(SuggestionInfo suggestionInfo, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
		return suggestionInfo.sender() instanceof Player player ?
				new SelectionParser<>(SELECTORS, s -> new PlayerContext(s, player), "n", "node").applySuggestions(suggestionInfo, suggestionsBuilder) :
				suggestionsBuilder.buildFuture();
	}
}
