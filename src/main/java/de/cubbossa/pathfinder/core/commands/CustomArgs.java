package de.cubbossa.pathfinder.core.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathfinder.core.node.*;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.SelectionUtils;
import de.cubbossa.pathfinder.util.SetArithmeticParser;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class CustomArgs {

	private static final Collection<String> TAGS = Lists.newArrayList("<rainbow>", "<gradient>", "<click>", "<hover>",
			"<rainbow:", "<gradient:", "<click:", "<hover:");

	static {
		TAGS.addAll(NamedTextColor.NAMES.keys().stream()
				.map(s -> "<" + s + ">").collect(Collectors.toList()));
		TAGS.addAll(TextDecoration.NAMES.keys().stream()
				.map(s -> "<" + s + ">").collect(Collectors.toList()));
	}

	private static final Pattern MINI_FINISH = Pattern.compile(".*(</?[^<>]*)");
	private static final Pattern MINI_CLOSE = Pattern.compile(".*<([^/<>:]+)(:[^/<>]+)?>[^/<>]*");

	/**
	 * Provides a MiniMessage Argument, which autocompletes xml tags and contains all default tags
	 * that come along with MiniMessage.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @return a MiniMessage argument instance
	 */
	public Argument<String> miniMessageArgument(String nodeName) {
		return miniMessageArgument(nodeName, suggestionInfo -> new ArrayList<>());
	}

	/**
	 * Provides a MiniMessage Argument, which autocompletes xml tags and contains all default tags
	 * that come along with MiniMessage.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @param supplier Used to insert custom tags into the suggestions
	 * @return a MiniMessage argument instance
	 */
	public Argument<String> miniMessageArgument(String nodeName, Function<SuggestionInfo, Collection<String>> supplier) {
		return new GreedyStringArgument(nodeName).replaceSuggestions((info, builder) -> {

			int offset = builder.getInput().length();
			String[] splits = info.currentInput().split(" ", -1);
			String in = splits[splits.length - 1];
			StringRange range = StringRange.between(offset - in.length(), offset);

			List<Suggestion> suggestions = new ArrayList<>();
			StringRange fRange = range;
			supplier.apply(info).stream()
					.filter(string -> string.startsWith(in))
					.map(string -> new Suggestion(fRange, string))
					.forEach(suggestions::add);

			Matcher m = MINI_FINISH.matcher(info.currentInput());
			if (m.matches()) {
				MatchResult result = m.toMatchResult();
				range = StringRange.between(result.start(1), result.end(1));
				String filter = result.group(1);
				StringRange finalRange = range;
				TAGS.stream()
						.filter(s -> s.startsWith(filter))
						.map(s -> new Suggestion(finalRange, s))
						.forEach(suggestions::add);

				suggestions.add(new Suggestion(range, m.group(1) + ">"));
			} else {
				Matcher matcher = MINI_CLOSE.matcher(info.currentArg());
				if (matcher.matches()) {
					suggestions.add(new Suggestion(StringRange.at(offset), "</" + matcher.group(1) + ">"));
				}
			}
			return CompletableFuture.completedFuture(Suggestions.create(builder.getInput(), suggestions));
		});
	}

	/**
	 * Provides a roadmap argument, which parses the namespaced key of a roadmap and resolves it into
	 * the actual roadmap instance.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @return a roadmap argument instance
	 */
	public Argument<RoadMap> roadMapArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
			return RoadMapHandler.getInstance().getRoadMap(customArgumentInfo.currentInput());
		}).includeSuggestions(suggestNamespacedKeys(sender -> RoadMapHandler.getInstance().getRoadMapsStream()
				.map(RoadMap::getKey).collect(Collectors.toList())));
	}

	/**
	 * Provides a world argument, which suggests all loaded world names and parses the user input
	 * into the world instance by resolving it via name.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @return a roadmap argument instance
	 */
	public Argument<World> worldArgument(String nodeName) {
		return new CustomArgument<>(new StringArgument(nodeName), customArgumentInfo -> {
			World world = Bukkit.getWorld(customArgumentInfo.currentInput());
			if (world == null) {
				throw new CustomArgument.CustomArgumentException("There is no world with this name.");
			}
			return world;
		}).includeSuggestions((suggestionInfo, suggestionsBuilder) -> {
			Bukkit.getWorlds().stream().map(World::getName).forEach(suggestionsBuilder::suggest);
			return suggestionsBuilder.buildFuture();
		});
	}

	/**
	 * Provides a path visualizer argument, which suggests the namespaced keys for all path visualizers and
	 * resolves the user input into the actual visualizer instance.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @return a path visualizer argument instance
	 */
	public Argument<? extends PathVisualizer<?, ?>> pathVisualizerArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
			PathVisualizer<?, ?> vis = VisualizerHandler.getInstance().getPathVisualizerMap().get(customArgumentInfo.currentInput());
			if (vis == null) {
				throw new CustomArgument.CustomArgumentException("There is no visualizer with this key.");
			}
			return vis;
		}).includeSuggestions(suggestNamespacedKeys(sender ->
				new ArrayList<>(VisualizerHandler.getInstance().getPathVisualizerMap().keySet())
		));
	}

	/**
	 * Provides a path visualizer argument, which suggests the namespaced keys for all path visualizers of the provided
	 * visualizer type and resolves the user input into the actual visualizer instance.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @param type     The type that all suggested and parsed visualizers are required to have
	 * @return a path visualizer argument instance
	 */
	public Argument<? extends PathVisualizer<?, ?>> pathVisualizerArgument(String nodeName, VisualizerType<?> type) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
			PathVisualizer<?, ?> vis = VisualizerHandler.getInstance().getPathVisualizerMap().get(customArgumentInfo.currentInput());
			if (vis == null) {
				throw new CustomArgument.CustomArgumentException("There is no visualizer with this key.");
			}
			if (!vis.getType().equals(type)) {
				throw new CustomArgument.CustomArgumentException("Visualizer '" + customArgumentInfo.currentInput() + "' is not of type " + type.getCommandName());
			}
			return vis;
		}).includeSuggestions(suggestNamespacedKeys(sender ->
				new ArrayList<>(VisualizerHandler.getInstance().getPathVisualizerMap().entrySet().stream()
						.filter(entry -> entry.getValue().getType().equals(type))
						.map(Map.Entry::getKey)
						.collect(Collectors.toList()))
		));
	}

	private interface NamespacedSuggestions {
		Collection<NamespacedKey> apply(CommandSender sender) throws CommandSyntaxException;
	}

	/**
	 * @param keysSupplier Converts a command sender into a collection of namespaced keys
	 * @return the argument suggestions object to insert
	 */
	public ArgumentSuggestions suggestNamespacedKeys(NamespacedSuggestions keysSupplier) {
		return (suggestionInfo, suggestionsBuilder) -> {

			Collection<NamespacedKey> keys = keysSupplier.apply(suggestionInfo.sender());
			String[] splits = suggestionInfo.currentInput().split(" ", -1);
			String in = splits[splits.length - 1];
			int sLen = suggestionInfo.currentInput().length();
			StringRange range = StringRange.between(sLen - in.length(), sLen);

			List<Suggestion> suggestions = keys.stream()
					.filter(key -> key.getKey().startsWith(in) || key.getNamespace().startsWith(in))
					.map(NamespacedKey::toString)
					.map(s -> new Suggestion(range, s))
					.collect(Collectors.toList());

			return CompletableFuture.completedFuture(Suggestions.create(suggestionsBuilder.getInput(), suggestions));
		};
	}

	public Argument<String> suggestCommaSeparatedList(String node) {
		return new GreedyStringArgument(node).replaceSuggestions((suggestionInfo, suggestionsBuilder) -> {
			return suggestionsBuilder.buildFuture();
			//TODO :S
			/*StringRange range = StringRange.at(suggestionInfo.currentInput().length());
			List<Suggestion> suggestions = Lists.newArrayList("abc", "def", "ghi").stream()
					.map(s -> new Suggestion(range, s))
					.collect(Collectors.toList());

			return CompletableFuture.completedFuture(Suggestions.create(suggestionsBuilder.getInput(), suggestions));*/
		});
		/*return (suggestionInfo, suggestionsBuilder) -> {
			if (!suggestionInfo.currentArg().matches("[0-9\\w,_]")) {
				throw new SimpleCommandExceptionType(() -> "Comma separated lists may only contain alphanumeric values and commas.").create();
			}
			StringRange range = StringRange.at(suggestionsBuilder.getInput().length());
			List<Suggestion> suggestions = new ArrayList<>();
			if (!suggestionInfo.currentArg().endsWith(",")) {
				suggestions.add(new Suggestion(range, ","));
			}
			return CompletableFuture.completedFuture(Suggestions.create(suggestionsBuilder.getInput(), suggestions));
		};*/
	}

	/**
	 * Provides a node type argument, which suggests the keys of all registered node types and resolves the user input
	 * into the node type instance.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @return a node type argument instance
	 */
	public <T extends Node> Argument<NodeType<T>> nodeTypeArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
			NodeType<T> type = NodeTypeHandler.getInstance().getNodeType(customArgumentInfo.currentInput());
			if (type == null) {
				throw new CustomArgument.CustomArgumentException("Node type with key '" + customArgumentInfo.currentInput() + "' does not exist.");
			}
			return type;
		}).includeSuggestions(suggestNamespacedKeys(sender -> NodeTypeHandler.getInstance().getTypes().keySet()));
	}

	/**
	 * Provides a node selection argument.
	 * This comes with a custom syntax that is a copy of the vanilla entity selectors.
	 * There are a variety of filters to apply to the search, an example user input could be "@n[distance=..10]", which
	 * returns all nodes within a range of 10 blocks.
	 * This includes ALL nodes of all roadmaps.
	 * All filters can be seen in {@link SelectionUtils#SELECTORS}
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @return a node selection argument instance
	 */
	public Argument<NodeSelection> nodeSelectionArgument(String nodeName) {
		return new CustomArgument<>(new TextArgument(nodeName), customArgumentInfo -> {
			if (customArgumentInfo.sender() instanceof Player player) {
				return SelectionUtils.getNodeSelection(player, customArgumentInfo.input().substring(1, customArgumentInfo.input().length() - 1));
			}
			return new NodeSelection();
		}).includeSuggestions(SelectionUtils::getNodeSelectionSuggestions);
	}

	/**
	 * Provides a node group argument, which suggests the keys of all node groups and resolves the user input
	 * into the node group instance.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @return a node group argument instance
	 */
	public Argument<NodeGroup> nodeGroupArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), info -> {
			NodeGroup group = NodeGroupHandler.getInstance().getNodeGroup(info.currentInput());
			if (group == null) {
				throw new CustomArgument.CustomArgumentException("There is no nodegroup with this name.");
			}
			return group;
		}).replaceSuggestions(suggestNamespacedKeys((sender -> {
			return NodeGroupHandler.getInstance().getNodeGroups().stream()
					.map(NodeGroup::getKey)
					.collect(Collectors.toList());
		})));
	}

	public Argument<? extends Discoverable> discoverableArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
			NodeGroup group = NodeGroupHandler.getInstance().getNodeGroup(customArgumentInfo.currentInput());
			if (group == null) {
				throw new CustomArgument.CustomArgumentException("There is no discoverable object with this name.");
			}
			return group;
		}).includeSuggestions(suggestNamespacedKeys(sender -> NodeGroupHandler.getInstance().getNodeGroups().stream()
				.map(NodeGroup::getKey).collect(Collectors.toList())));
	}

	private static final List<Character> LIST_SYMBOLS = Lists.newArrayList('!', '&', '|', ')', '(');
	private static final List<String> LIST_SYMBOLS_STRING = Lists.newArrayList("!", "&", "|", ")", "(");

	public Argument<NodeSelection> navigateSelectionArgument(String nodeName) {
		return new CustomArgument<>(new GreedyStringArgument(nodeName), context -> {
			if (!(context.sender() instanceof Player player)) {
				throw new CustomArgument.CustomArgumentException("Only for players");
			}
			String search = context.currentInput();
			SetArithmeticParser<Groupable> parser = new SetArithmeticParser<>(RoadMapHandler.getInstance().getRoadMaps().values().stream()
					.flatMap(roadMap -> roadMap.getNodes().stream()
							.filter(node -> node instanceof Groupable)
							.map(node -> (Groupable) node)
							.filter(node -> {
								FindModule.NavigationRequestContext c = new FindModule.NavigationRequestContext(player.getUniqueId(), node);
								return FindModule.getInstance().getNavigationFilter().stream().allMatch(predicate -> predicate.test(c));
							}))
					.collect(Collectors.toSet()), Navigable::getSearchTerms);
			return new NodeSelection(new HashSet<>(parser.parse(search)));
		})
				.includeSuggestions((suggestionInfo, suggestionsBuilder) -> {
					if (!(suggestionInfo.sender() instanceof Player player)) {
						return suggestionsBuilder.buildFuture();
					}
					UUID playerId = player.getUniqueId();
					String input = suggestionsBuilder.getInput();

					int lastIndex = LIST_SYMBOLS.stream()
							.map(input::lastIndexOf)
							.mapToInt(value -> value)
							.max()
							.orElse(0);
					lastIndex = Integer.max(suggestionsBuilder.getInput().length() - suggestionsBuilder.getRemaining().length(), lastIndex + 1);

					StringRange range = StringRange.between(lastIndex, input.length());
					List<Suggestion> suggestions = new ArrayList<>();

					StringRange finalRange = range;
					String inRange = finalRange.get(input);
					Collection<String> allTerms = NodeGroupHandler.getInstance().getNodeGroups().stream()
							.filter(NodeGroup::isNavigable)
							.map(navigable -> new FindModule.NavigationRequestContext(playerId, navigable))
							.filter(navigable -> FindModule.getInstance().getNavigationFilter().stream().allMatch(navigablePredicate -> navigablePredicate.test(navigable)))
							.map(FindModule.NavigationRequestContext::navigable)
							.map(Navigable::getSearchTerms)
							.flatMap(Collection::stream)
							.collect(Collectors.toSet());

					/*TODO if (!Arrays.stream(suggestionInfo.currentInput().substring(0, lastIndex).split("[!&|()]")).allMatch(allTerms::contains)) {
						throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), () -> "At least one of the used search terms is incorrect");
					}*/


					allTerms.stream().filter(s -> s.startsWith(inRange))
							.map(s -> new Suggestion(finalRange, s))
							.forEach(suggestions::add);


					if (suggestions.isEmpty()) {
						range = StringRange.at(suggestionInfo.currentInput().length() - 1);
						for (String s : LIST_SYMBOLS_STRING) {
							suggestions.add(new Suggestion(range, s));
						}
					}

					return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
				});
	}

	/**
	 * Provides a visualizer type argument, which suggests the keys of all registered visualizer types and resolves the user input
	 * into the visualizer type instance.
	 *
	 * @param nodeName The name of the command argument in the command structure
	 * @return a visualizer type argument instance
	 */
	public Argument<? extends VisualizerType<?>> visualizerTypeArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {

			VisualizerType<?> type = VisualizerHandler.getInstance().getVisualizerType(customArgumentInfo.currentInput());
			if (type == null) {
				throw new CustomArgument.CustomArgumentException("Unknown type: '" + customArgumentInfo.currentInput() + "'.");
			}
			return type;
		}).includeSuggestions(suggestNamespacedKeys(sender -> VisualizerHandler.getInstance().getVisualizerTypes().keySet()));
	}
}
