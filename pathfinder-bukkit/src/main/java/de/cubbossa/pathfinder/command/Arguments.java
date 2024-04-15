package de.cubbossa.pathfinder.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.group.NavigableModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.Keyed;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.Pagination;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.module.AbstractNavigationHandler;
import de.cubbossa.pathfinder.navigation.query.FindQueryParser;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.NodeSelection;
import de.cubbossa.pathfinder.node.NodeSelectionImpl;
import de.cubbossa.pathfinder.node.NodeSelectionProviderImpl;
import de.cubbossa.pathfinder.node.NodeType;
import de.cubbossa.pathfinder.storage.StorageAdapter;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import de.cubbossa.pathfinder.visualizer.query.SearchTerm;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A collection of custom command arguments for the CommandAPI.
 */
@UtilityClass
public class Arguments {

  private static final Collection<String> TAGS =
      Lists.newArrayList("<rainbow>", "<gradient>", "<click>", "<hover>",
          "<rainbow:", "<gradient:", "<click:", "<hover:");
  private static final Pattern MINI_FINISH = Pattern.compile(".*(</?[^<>]*)");
  private static final Pattern MINI_CLOSE = Pattern.compile(".*<([^/<>:]+)(:[^/<>]+)?>[^/<>]*");
  private static final List<Character> LIST_SYMBOLS = Lists.newArrayList('!', '&', '|', ')', '(');
  private static final List<String> LIST_SYMBOLS_STRING =
      Lists.newArrayList("!", "&", "|", ")", "(");

  static {
    TAGS.addAll(NamedTextColor.NAMES.keys().stream()
        .map(s -> "<" + s + ">").toList());
    TAGS.addAll(TextDecoration.NAMES.keys().stream()
        .map(s -> "<" + s + ">").toList());
  }

  public CommandArgument<Player, PlayerArgument> player(String node) {
    return new CommandArgument<>(new PlayerArgument(node));
  }

  public CommandArgument<PathPlayer<Player>, CustomArgument<PathPlayer<Player>, Player>> pathPlayer(String node) {
    return new CommandArgument<>(new CustomArgument<>(new PlayerArgument(node), info -> {
      return BukkitUtils.wrap(info.currentInput());
    }));
  }

  public CommandArgument<Collection<PathPlayer<Player>>, CustomArgument<Collection<PathPlayer<Player>>, Collection>> pathPlayers(String node) {
    return new CommandArgument<>(new CustomArgument<>(new EntitySelectorArgument.ManyPlayers(node), info -> {
      return ((Collection<Object>) info.currentInput()).stream()
          .filter(e -> e instanceof Player)
          .map(e -> (Player) e)
          .map(BukkitUtils::<Player>wrap)
          .collect(Collectors.toList());
    }));
  }

  public CommandArgument<de.cubbossa.pathfinder.misc.Location, CustomArgument<de.cubbossa.pathfinder.misc.Location, Location>> location(
      String node, LocationType type) {
    return CommandArgument.arg(new CustomArgument<>(new LocationArgument(node, type), customArgumentInfo -> {
      return BukkitVectorUtils.toInternal(customArgumentInfo.currentInput());
    }));
  }

  public CommandArgument<de.cubbossa.pathfinder.misc.Location, CustomArgument<de.cubbossa.pathfinder.misc.Location, Location>> location(
      String node) {
    return location(node, LocationType.PRECISE_POSITION);
  }


  public CustomLiteralArgument literal(String literal) {
    return new CustomLiteralArgument(literal);
  }

  public CommandArgument<Integer, IntegerArgument> integer(String node) {
    return new CommandArgument<>(new IntegerArgument(node));
  }

  public CommandArgument<Integer, IntegerArgument> integer(String node, int min) {
    return new CommandArgument<>(new IntegerArgument(node, min));
  }

  public CommandArgument<Integer, IntegerArgument> integer(String node, int min, int max) {
    return new CommandArgument<>(new IntegerArgument(node, min, max));
  }

  /**
   * An argument that suggests and resolves enum fields in lower case syntax.
   *
   * @param nodeName The name of the command argument in the command structure
   * @param scope    The enum class instance
   * @param <E>      The enum type
   * @return The argument
   */
  public <E extends Enum<E>> Argument<E> enumArgument(String nodeName, Class<E> scope) {
    return CommandArgument.arg(new CustomArgument<>(new StringArgument(nodeName), info -> {
      try {
        return Enum.valueOf(scope, info.input().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw CustomArgument.CustomArgumentException.fromString("Invalid input value: " + info.input());
      }
    })).includeSuggestions((suggestionInfo, suggestionsBuilder) -> {
      Arrays.stream(scope.getEnumConstants())
          .map(Enum::toString)
          .map(String::toLowerCase)
          .forEach(suggestionsBuilder::suggest);
      return suggestionsBuilder.buildFuture();
    });
  }

  public CommandArgument<Pagination, CustomArgument<Pagination, Integer>> pagination(int size) {
    return CommandArgument.arg(new CustomArgument<>(new IntegerArgument("page", 1), p -> {
      return Pagination.page(p.currentInput() - 1, size);
    }));
  }

  /**
   * Provides a MiniMessage BukkitNodeSelectionArgument, which autocompletes xml tags and contains all default tags
   * that come along with MiniMessage.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a MiniMessage argument instance
   */
  public Argument<String> miniMessageArgument(String nodeName) {
    return miniMessageArgument(nodeName, suggestionInfo -> new ArrayList<>());
  }

  /**
   * Provides a MiniMessage BukkitNodeSelectionArgument, which autocompletes xml tags and contains all default tags
   * that come along with MiniMessage.
   *
   * @param nodeName The name of the command argument in the command structure
   * @param supplier Used to insert custom tags into the suggestions
   * @return a MiniMessage argument instance
   */
  public Argument<String> miniMessageArgument(
      String nodeName,
      Function<SuggestionInfo, Collection<String>> supplier
  ) {
    return CommandArgument.arg(new GreedyStringArgument(nodeName)).includeSuggestions((info, builder) -> {

      if (info.currentArg().length() == 0) {
        TAGS.forEach(builder::suggest);
        return builder.buildFuture();
      }

      int offset = builder.getInput().length();
      String[] splits = info.currentInput().split(" ", -1);
      String in = splits[splits.length - 1];
      StringRange range = StringRange.between(offset - in.length(), offset);

      List<Suggestion> suggestions = new ArrayList<>();
      StringRange r = range;
      supplier.apply(info).stream()
          .filter(string -> string.startsWith(in))
          .map(string -> new Suggestion(r, string))
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
   * Provides a path visualizer argument, which suggests the namespaced keys for all path
   * visualizers and resolves the user input into the actual visualizer instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a path visualizer argument instance
   */
  public Argument<? extends PathVisualizer<?, ?>> pathVisualizerArgument(String nodeName) {
    return CommandArgument.arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      Optional<?> vis =
          PathFinderProvider.get().getStorage()
              .loadVisualizer(BukkitPathFinder.convert(customArgumentInfo.currentInput()))
              .join();
      if (vis.isEmpty()) {
        throw CustomArgument.CustomArgumentException.fromString("There is no visualizer with this key.");
      }
      return (PathVisualizer<?, ?>) vis.get();
    })).includeSuggestions(suggestNamespacedKeys(sender ->
        PathFinderProvider.get().getStorage().loadVisualizers().thenApply(v -> v.stream()
            .map(Keyed::getKey)
            .toList()))
    );
  }

  /**
   * Provides a path visualizer argument, which suggests the namespaced keys for all path
   * visualizers of the provided visualizer type and resolves the user input into the actual
   * visualizer instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @param type     The type that all suggested and parsed visualizers are required to have
   * @return a path visualizer argument instance
   */
  public <T extends PathVisualizer<?, ?>> Argument<T> pathVisualizerArgument(String nodeName,
                                                                             VisualizerType<T> type) {
    return CommandArgument.arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      Optional<T> vis = (Optional<T>) PathFinderProvider.get().getStorage()
          .loadVisualizer(BukkitPathFinder.convert(customArgumentInfo.currentInput())).join();
      if (vis.isEmpty()) {
        throw CustomArgument.CustomArgumentException.fromString("There is no visualizer with this key.");
      }
      return (T) vis.get();
    })).includeSuggestions(suggestNamespacedKeys(sender ->
        PathFinderProvider.get().getStorage().loadVisualizers(type)
            .thenApply(pathVisualizers -> pathVisualizers.stream()
                .map(Keyed::getKey)
                .toList()))
    );
  }

  /**
   * Suggests a set of NamespacedKeys where the completion also includes matches for only the
   * key.
   *
   * @return the argument suggestions object to insert
   */
  public ArgumentSuggestions<CommandSender> suggestNamespacedKeys(
      Function<CommandSender, CompletableFuture<Collection<NamespacedKey>>> keysSupplierFuture) {
    return (suggestionInfo, suggestionsBuilder) -> {
      return keysSupplierFuture.apply(suggestionInfo.sender()).thenApply(keys -> {
        String[] splits = suggestionInfo.currentInput().split(" ", -1);
        String in = splits[splits.length - 1];
        int len = suggestionInfo.currentInput().length();
        StringRange range = StringRange.between(len - in.length(), len);

        List<Suggestion> suggestions = keys.stream()
            .filter(key -> key.getKey().startsWith(in) || key.getNamespace().startsWith(in))
            .map(NamespacedKey::toString)
            .map(s -> new Suggestion(range, s))
            .collect(Collectors.toList());

        return Suggestions.create(suggestionsBuilder.getInput(), suggestions);
      });
    };
  }

  /**
   * Provides a node type argument, which suggests the keys of all registered node types and
   * resolves the user input into the node type instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a node type argument instance
   */
  public <N extends Node> Argument<NodeType<N>> nodeTypeArgument(
      String nodeName) {
    return CommandArgument.arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      NodeType<N> type =
          PathFinderProvider.get().getNodeTypeRegistry()
              .getType(BukkitPathFinder.convert(customArgumentInfo.currentInput()));
      if (type == null) {
        throw CustomArgument.CustomArgumentException.fromString(
            "Node type with key '" + customArgumentInfo.currentInput() + "' does not exist.");
      }
      return type;
    })).includeSuggestions(
        suggestNamespacedKeys(sender -> CompletableFuture.completedFuture(
            PathFinderProvider.get().getNodeTypeRegistry().getTypeKeys())));
  }

  /**
   * Provides a node selection argument.
   * This comes with a custom syntax that is a copy of the vanilla entity selectors.
   * There are a variety of filters to apply to the search, an example user input could be
   * "@n[distance=..10]", which returns all nodes within a range of 10 blocks.
   * This includes ALL nodes of all roadmaps.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a node selection argument instance
   */
  public CommandArgument<NodeSelection, CustomArgument<NodeSelection, String>> nodeSelectionArgument(
      String nodeName) {
    return (CommandArgument<NodeSelection, CustomArgument<NodeSelection, String>>) CommandArgument.arg(
            new CustomArgument<>(new TextArgument(nodeName), info -> {
              if (info.sender() instanceof Player player) {
                try {
                  return NodeSelection.ofSender(info.input().substring(1, info.input().length() - 1), player);
                } catch (ParseCancellationException e) {
                  throw CustomArgument.CustomArgumentException.fromString(e.getMessage());
                }
              }
              return new NodeSelectionImpl();
            }))
        .includeSuggestions((suggestionInfo, suggestionsBuilder) -> {
          int offset = suggestionInfo.currentInput().length() - suggestionInfo.currentArg().length();

          return NodeSelectionProviderImpl.getNodeSelectionSuggestions(suggestionInfo)
              //  add quotations to suggestions
              .thenApply(s -> CommandUtils.wrapWithQuotation(suggestionInfo.currentArg(), s,
                  suggestionInfo.currentArg(), offset))
              // shift suggestions toward actual command argument offset
              .thenApply(s -> CommandUtils.offsetSuggestions(suggestionInfo.currentArg(), s, offset));
        });
  }

  /**
   * Provides a node group argument, which suggests the keys of all node groups and resolves the
   * user input into the node group instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a node group argument instance
   */
  public CommandArgument<NodeGroup, CustomArgument<NodeGroup, NamespacedKey>> nodeGroupArgument(
      String nodeName) {
    return (CommandArgument<NodeGroup, CustomArgument<NodeGroup, NamespacedKey>>) CommandArgument.arg(
        new CustomArgument<>(new NamespacedKeyArgument(nodeName), info -> {
          return PathFinderProvider.get().getStorage()
              .loadGroup(BukkitPathFinder.convert(info.currentInput())).join()
              .orElseThrow();
        })
    ).replaceSuggestions(suggestNamespacedKeys(
        sender -> PathFinderProvider.get().getStorage().loadAllGroups().thenApply(nodeGroups ->
            nodeGroups.stream().map(NodeGroup::getKey).toList())
    ));
  }

  /**
   * A command argument that resolves and suggests Discoverables as their namespaced keys.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return The CustomArgument instance
   */
  public CommandArgument<NamespacedKey, Argument<NamespacedKey>> discoverableArgument(String nodeName) {
    return (CommandArgument<NamespacedKey, Argument<NamespacedKey>>) CommandArgument.arg(
            new CustomArgument<>(new NamespacedKeyArgument(nodeName), i -> BukkitPathFinder.convert(i.currentInput())))
        .includeSuggestions(suggestNamespacedKeys(sender -> PathFinderProvider.get().getStorage().loadAllGroups()
                .thenApply(nodeGroups -> nodeGroups.stream()
                    .filter(g -> g.hasModifier(DiscoverableModifier.KEY))
                    .map(NodeGroup::getKey)
                    .collect(Collectors.toList()))
            )
        );
  }

  /**
   * A command argument that resolves a navigation query and suggests the according search terms.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return The CustomArgument instance
   */
  public Argument<NodeSelectionImpl> navigateSelectionArgument(String nodeName) {
    return CommandArgument.arg(new CustomArgument<>(new GreedyStringArgument(nodeName),
            context -> {
              if (!(context.sender() instanceof Player player)) {
                throw CustomArgument.CustomArgumentException.fromString("Only for players");
              }
              String search = context.currentInput().replace(" ", "");
              StorageAdapter storage = PathFinderProvider.get().getStorage();
              Map<Node, Collection<NavigableModifier>> scope = storage.<NavigableModifier>loadNodes(NavigableModifier.KEY).join();
              Collection<Node> valids = AbstractNavigationHandler.getInstance().applyNavigationConstraints(player.getUniqueId(), scope.keySet());

              Map<Node, Collection<NavigableModifier>> result = new HashMap<>();
              valids.forEach(node -> result.put(node, scope.get(node)));

              try {
                Function<Node, Collection<SearchTerm>> searchTermFunction = n -> result.get(n).stream()
                    .map(NavigableModifier::getSearchTerms)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

                Collection<Node> target = new FindQueryParser().parse(search, new ArrayList<>(valids), searchTermFunction);
                return new NodeSelectionImpl(target);
              } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException(t);
              }
            }))
        .includeSuggestions((suggestionInfo, suggestionsBuilder) -> {
          if (!(suggestionInfo.sender() instanceof Player)) {
            return suggestionsBuilder.buildFuture();
          }
          String input = suggestionsBuilder.getInput();

          int lastIndex = LIST_SYMBOLS.stream()
              .map(input::lastIndexOf)
              .mapToInt(value -> value)
              .max()
              .orElse(0);
          lastIndex = Integer.max(
              suggestionsBuilder.getInput().length() - suggestionsBuilder.getRemaining().length(),
              lastIndex + 1);

          StringRange range = StringRange.between(lastIndex, input.length());
          String inRange = range.get(input);

          return PathFinderProvider.get().getStorage().<NavigableModifier>loadNodes(NavigableModifier.KEY).thenApply(map -> {
            map.values().stream()
                .flatMap(Collection::stream)
                .map(NavigableModifier::getSearchTermStrings)
                .flatMap(Collection::stream)
                .filter(s -> s.startsWith(inRange))
                .forEach(suggestionsBuilder::suggest);
            return suggestionsBuilder.build();
          });
        });
  }

  /**
   * Provides a visualizer type argument, which suggests the keys of all registered visualizer types
   * and resolves the user input into the visualizer type instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a visualizer type argument instance
   */
  public Argument<VisualizerType<PathVisualizer<?, ?>>> visualizerTypeArgument(
      String nodeName) {
    return CommandArgument.arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {

      Optional<VisualizerType<PathVisualizer<?, ?>>> type =
          VisualizerTypeRegistryImpl.getInstance()
              .getType(BukkitPathFinder.convert(customArgumentInfo.currentInput()));
      if (type.isEmpty()) {
        throw CustomArgument.CustomArgumentException.fromString(
            "Unknown type: '" + customArgumentInfo.currentInput() + "'.");
      }
      return type.get();
    })).includeSuggestions(suggestNamespacedKeys(sender -> {
      return CompletableFuture.completedFuture(PathFinderProvider.get().getVisualizerTypeRegistry().getTypes().keySet());
    }));
  }
}
