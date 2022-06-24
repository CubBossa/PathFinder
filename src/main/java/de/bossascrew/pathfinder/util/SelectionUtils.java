package de.bossascrew.pathfinder.util;

import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.node.Node;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
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

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_PERMISSION = new SelectionParser.Filter<>("permission", Pattern.compile("([\"'])(?:(?=(\\\\?))\\2\\.)*?\\1"), (nodes, context) -> {
		boolean regex = context.value().startsWith("regex:");
		String arg = regex ? context.value().substring(6) : context.value();
		Pattern pattern = regex ? Pattern.compile(arg) : null;
		return nodes.stream()
				.filter(n -> n.getPermission() != null)
				.filter(n -> regex ? pattern.matcher(n.getPermission()).matches() : n.getPermission().equalsIgnoreCase(arg))
				.collect(Collectors.toList());
	}, "<permission>");

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
			double dist = node.getPosition().distance(context.getPlayer().getLocation().toVector());
			return smaller && dist <= req || larger && dist >= req || dist == req;
		}).collect(Collectors.toList());
	}, "..1", "1.5", "2..");

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
			double dist = node.getPosition().distance(context.getPlayer().getLocation().toVector());
			return smaller && dist <= req || larger && dist >= req || dist == req;
		}).collect(Collectors.toList());
	}, "..1", "1.5", "2..");

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_LIMIT = new SelectionParser.Filter<>("limit", Pattern.compile("[0-9]+"), (nodes, context) -> CommandUtils.subList(new ArrayList<>(nodes), 0, Integer.parseInt(context.value())), IntStream.range(1, 10).mapToObj(i -> "" + i).toArray(String[]::new));

	public static final SelectionParser.Filter<Node, PlayerContext> SELECT_KEY_SORT = new SelectionParser.Filter<>("sort", Pattern.compile("(nearest|furthest|random|arbitrary)"), (nodes, context) -> {
		Vector pVec = context.getPlayer().getLocation().toVector();
		return switch (context.value()) {
			case "nearest" -> nodes.stream().sorted(Comparator.comparingDouble(o -> o.getPosition().distance(pVec))).collect(Collectors.toList());
			case "furthest" -> nodes.stream()
					.sorted((o1, o2) -> Double.compare(o2.getPosition().distance(pVec), o1.getPosition().distance(pVec)))
					.collect(Collectors.toList());
			case "random" -> nodes.stream().collect(Collectors.collectingAndThen(Collectors.toList(), n -> {
				Collections.shuffle(n);
				return n;
			}));
			case "arbitrary" -> nodes.stream().sorted(Comparator.comparingInt(Node::getNodeId)).collect(Collectors.toList());
			default -> nodes;
		};
	}, "nearest", "furthest", "random", "arbitrary");

	public static final String SELECT_KEY_GROUP = "group";

	public static final String SELECT_KEY_EDGE = "has_edge";

	public static final ArrayList<SelectionParser.Filter<Node, PlayerContext>> SELECTORS = Lists.newArrayList(
			SELECT_KEY_LIMIT, SELECT_KEY_DISTANCE, SELECT_KEY_TANGENT_LENGTH, SELECT_KEY_PERMISSION,
			SELECT_KEY_SORT
	);

	public static NodeSelection getNodeSelection(Player player, Collection<Node> input, String selectString) {
		return new SelectionParser<>(SELECTORS, string -> new PlayerContext(string, player), "n", "node")
				.parseSelection(input, selectString, NodeSelection::new);
	}

	public static List<String> completeNodeSelection(String selectString) {
		if (!selectString.startsWith("@n[")) {
			return Lists.newArrayList("@n[");
		}
		String in = selectString;
		selectString = selectString.substring(3);

		String[] args = selectString.split(",");
		selectString = args[args.length - 1];

		String sel = selectString;
		String sub = in.substring(0, Integer.max(in.lastIndexOf(','), in.lastIndexOf('[')) + 1);

		return SELECTORS.stream()
				.map(s -> sel.endsWith("=") ? Arrays.stream(s.completions()).map(c -> s.key() + "=" + c).collect(Collectors.toList()) : Lists.newArrayList(s.key() + "="))
				.flatMap(Collection::stream)
				.map(s -> sub + s)
				.collect(Collectors.toList());
	}
}
