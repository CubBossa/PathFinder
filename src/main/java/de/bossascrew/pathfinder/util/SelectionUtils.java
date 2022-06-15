package de.bossascrew.pathfinder.util;

import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.data.findable.NavigationTarget;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SelectionUtils {

	private static final Pattern SELECT_PATTERN = Pattern.compile("@n(\\[((.+=.+,)*(.+=.+))?])?");

	private record Context(Player player, String value) {
	}

	public record Selector(String key, Pattern value,
	                       BiFunction<List<NavigationTarget>, Context, List<NavigationTarget>> filter,
	                       String... completions) {
	}

	public static final Selector SELECT_KEY_NAME = new Selector("name", Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1"), (nodes, context) -> {
		boolean regex = context.value().startsWith("regex:");
		String arg = regex ? context.value().substring(6) : context.value();
		Pattern pattern = regex ? Pattern.compile(arg) : null;
		return nodes.stream()
				.filter(n -> regex ? pattern.matcher(n.getNameFormat()).matches() : n.getNameFormat().equalsIgnoreCase(arg))
				.collect(Collectors.toList());
	}, "<name>");

	public static final Selector SELECT_KEY_PERMISSION = new Selector("permission", Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1"), (nodes, context) -> {
		boolean regex = context.value().startsWith("regex:");
		String arg = regex ? context.value().substring(6) : context.value();
		Pattern pattern = regex ? Pattern.compile(arg) : null;
		return nodes.stream()
				.filter(n -> n.getPermission() != null)
				.filter(n -> regex ? pattern.matcher(n.getPermission()).matches() : n.getPermission().equalsIgnoreCase(arg))
				.collect(Collectors.toList());
	}, "<permission>");

	public static final Selector SELECT_KEY_TANGENT_LENGTH = new Selector("tangent_length", Pattern.compile("(..)?[0-9]*(.[0-9]+)?(..)?"), (nodes, context) -> {
		boolean smaller = context.value().startsWith("..");
		boolean larger = context.value().endsWith("..");
		if (smaller && larger) return nodes;
		String arg = context.value();
		if (smaller) {
			arg = arg.substring(2);
		}
		if (larger) {
			arg = arg.substring(0, arg.length() - 2);
		}
		float req = Float.parseFloat(arg);
		return nodes.stream().filter(node -> {
			double dist = node.getPosition().distance(context.player().getLocation().toVector());
			return smaller && dist <= req || larger && dist >= req || dist == req;
		}).collect(Collectors.toList());
	}, "..1", "1.5", "2..");

	public static final Selector SELECT_KEY_DISTANCE = new Selector("distance", Pattern.compile("(..)?[0-9]*(.[0-9]+)?(..)?"), (nodes, context) -> {
		boolean smaller = context.value().startsWith("..");
		boolean larger = context.value().endsWith("..");
		if (smaller && larger) return nodes;
		String arg = context.value();
		if (smaller) {
			arg = arg.substring(2);
		}
		if (larger) {
			arg = arg.substring(0, arg.length() - 2);
		}
		float req = Float.parseFloat(arg);
		return nodes.stream().filter(node -> {
			double dist = node.getPosition().distance(context.player().getLocation().toVector());
			return smaller && dist <= req || larger && dist >= req || dist == req;
		}).collect(Collectors.toList());
	}, "..1", "1.5", "2..");

	public static final Selector SELECT_KEY_LIMIT = new Selector("limit", Pattern.compile("[0-9]+"), (nodes, context) -> nodes.subList(0, Integer.parseInt(context.value())), IntStream.range(1, 10).mapToObj(i -> "" + i).toArray(String[]::new));

	public static final Selector SELECT_KEY_SORT = new Selector("sort", Pattern.compile("(nearest|furthest|random|arbitrary)"), (nodes, context) -> {
		Vector pVec = context.player().getLocation().toVector();
		return switch (context.value()) {
			case "nearest" -> nodes.stream().sorted(Comparator.comparingDouble(o -> o.getPosition().distance(pVec))).collect(Collectors.toList());
			case "furthest" -> nodes.stream()
					.sorted((o1, o2) -> Double.compare(o2.getPosition().distance(pVec), o1.getPosition().distance(pVec)))
					.collect(Collectors.toList());
			case "random" -> nodes.stream().collect(Collectors.collectingAndThen(Collectors.toList(), n -> {
				Collections.shuffle(n);
				return n;
			}));
			case "arbitrary" -> nodes.stream().sorted(Comparator.comparingInt(NavigationTarget::getNodeId)).collect(Collectors.toList());
		};
	}, "nearest", "furthest", "random", "arbitrary");

	public static final String SELECT_KEY_GROUP = "group";

	public static final String SELECT_KEY_EDGE = "has_edge";

	public static final Selector[] SELECTORS = {
			SELECT_KEY_LIMIT, SELECT_KEY_DISTANCE, SELECT_KEY_TANGENT_LENGTH, SELECT_KEY_NAME, SELECT_KEY_PERMISSION,
			SELECT_KEY_SORT
	};

	public static NodeSelection getTargetSelection(Player player, Collection<NavigationTarget> input, String selectString) {
		Matcher matcher = SELECT_PATTERN.matcher(selectString);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Select String must be of format @n[<key>=<value>,...]");
		}
		if (matcher.groupCount() < 2) {
			return new NodeSelection(input);
		}
		String argumentString = matcher.group(2);

		Map<Selector, String> arguments = new HashMap<>();
		while (argumentString.length() > 0) {
			int len = argumentString.length();
			for (Selector selector : SELECTORS) {
				if (!argumentString.startsWith(selector.key())) {
					continue;
				}
				argumentString = argumentString.substring(selector.key().length() + 1);
				Matcher m = selector.value().matcher(argumentString);
				MatchResult matchResult = m.toMatchResult();
				if (matchResult.start() != 0) {
					throw new IllegalArgumentException("Illegal value for key '" + selector.key() + "': " + argumentString);
				}
				String value = argumentString.substring(matcher.end() + 1);
				arguments.put(selector, value);
			}
			if (len <= argumentString.length()) {
				throw new IllegalArgumentException("Illegal selection argument: " + argumentString);
			}
		}

		List<NavigationTarget> nodes = new ArrayList<>(input);
		for (Map.Entry<Selector, String> entry : arguments.entrySet()) {
			nodes = entry.getKey().filter().apply(nodes, new Context(player, entry.getValue()));
		}
		return new NodeSelection(nodes);
	}

	public static List<String> complete(Collection<? extends NavigationTarget> input, String selectString) {
		if (!selectString.startsWith("@n[")) {
			return Lists.newArrayList("@n[");
		}
		selectString = selectString.substring(3);
		String[] args = selectString.split(",");
		selectString = args[args.length - 1];

		String sel = selectString;
		if (selectString.endsWith("=")) return Arrays.stream(SELECTORS)
				.flatMap(s -> sel.substring(0, sel.length() - 1).equalsIgnoreCase(s.key) ? Arrays.stream(s.completions()) : Stream.empty())
				.collect(Collectors.toList());

		return Arrays.stream(SELECTORS)
				.map(s -> s.key() + "=")
				.filter(sel::startsWith)
				.collect(Collectors.toList());
	}
}
