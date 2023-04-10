package de.cubbossa.pathfinder.core.commands;

import static de.cubbossa.pathfinder.core.commands.CommandArgument.arg;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeType;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.modifier.DiscoverableModifier;
import de.cubbossa.pathfinder.core.nodegroup.modifier.NavigableModifier;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.query.FindQueryParser;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.storage.StorageAssistant;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.Pagination;
import de.cubbossa.pathfinder.util.SelectionUtils;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A collection of custom command arguments for the CommandAPI.
 */
@UtilityClass
public class CustomArgs {

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

  public CommandArgument<Location, LocationArgument> location(String node) {
    return new CommandArgument<>(new LocationArgument(node));
  }

  public CommandArgument<Location, LocationArgument> location(String node, LocationType type) {
    return new CommandArgument<>(new LocationArgument(node, type));
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
    return arg(new CustomArgument<>(new StringArgument(nodeName), info -> {
      try {
        return Enum.valueOf(scope, info.input().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new CustomArgument.CustomArgumentException("Invalid input value: " + info.input());
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
    return arg(new CustomArgument<>(new IntegerArgument("page", 1), p -> {
      return Pagination.page(p.currentInput() - 1, size);
    }));
  }

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
  public Argument<String> miniMessageArgument(
      String nodeName,
      Function<SuggestionInfo, Collection<String>> supplier
  ) {
    return arg(new GreedyStringArgument(nodeName)).replaceSuggestions((info, builder) -> {

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
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      Optional<?> vis = PathFinderProvider.get().getStorage().loadVisualizer(customArgumentInfo.currentInput()).join();
      if (vis.isEmpty()) {
        throw new CustomArgument.CustomArgumentException("There is no visualizer with this key.");
      }
      return (PathVisualizer<?, ?>) vis.get();
    })).includeSuggestions(suggestNamespacedKeys(sender ->
        PathFinderProvider.get().getStorage().loadVisualizers().thenApply(pathVisualizers -> pathVisualizers.stream()
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
  public <T extends PathVisualizer<T, ?>> Argument<T> pathVisualizerArgument(String nodeName, VisualizerType<T> type) {
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      Optional<T> vis = (Optional<T>) PathFinderProvider.get().getStorage()
          .loadVisualizer(customArgumentInfo.currentInput()).join();
      if (vis.isEmpty()) {
        throw new CustomArgument.CustomArgumentException("There is no visualizer with this key.");
      }
      return (T) vis.get();
    })).includeSuggestions(suggestNamespacedKeys(sender ->
        PathFinderProvider.get().getStorage().loadVisualizers(type).thenApply(pathVisualizers -> pathVisualizers.values().stream()
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
  public ArgumentSuggestions suggestNamespacedKeys(
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
  public <T extends Node<T>> Argument<NodeType<T>> nodeTypeArgument(String nodeName) {
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      NodeType<T> type =
          PathPlugin.getInstance().getNodeTypeRegistry()
              .getNodeType(customArgumentInfo.currentInput());
      if (type == null) {
        throw new CustomArgument.CustomArgumentException(
            "Node type with key '" + customArgumentInfo.currentInput() + "' does not exist.");
      }
      return type;
    })).includeSuggestions(
        suggestNamespacedKeys(sender -> CompletableFuture.completedFuture(
            PathPlugin.getInstance().getNodeTypeRegistry().getTypeKeys())));
  }

  /**
   * Provides a node selection argument.
   * This comes with a custom syntax that is a copy of the vanilla entity selectors.
   * There are a variety of filters to apply to the search, an example user input could be
   * "@n[distance=..10]", which returns all nodes within a range of 10 blocks.
   * This includes ALL nodes of all roadmaps.
   * All filters can be seen in {@link SelectionUtils#SELECTORS}
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a node selection argument instance
   */
  public CommandArgument<NodeSelection, CustomArgument<NodeSelection, String>> nodeSelectionArgument(
      String nodeName) {
    return (CommandArgument<NodeSelection, CustomArgument<NodeSelection, String>>) arg(
        new CustomArgument<>(new TextArgument(nodeName), info -> {
          if (info.sender() instanceof Player player) {
            try {
              return SelectionUtils.getNodeSelection(player,
                  info.input().substring(1, info.input().length() - 1));
            } catch (CommandSyntaxException | ParseCancellationException e) {
              throw new CustomArgument.CustomArgumentException(e.getMessage());
            }
          }
          return new NodeSelection();
        })).includeSuggestions(SelectionUtils::getNodeSelectionSuggestions);
  }

  /**
   * Provides a node group argument, which suggests the keys of all node groups and resolves the
   * user input into the node group instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a node group argument instance
   */
  public CommandArgument<NamespacedKey, Argument<NamespacedKey>> nodeGroupArgument(
      String nodeName) {
    return (CommandArgument<NamespacedKey, Argument<NamespacedKey>>) arg(
        new NamespacedKeyArgument(nodeName)
    ).replaceSuggestions(suggestNamespacedKeys(
        sender -> PathPlugin.getInstance().getStorage().loadAllGroups().thenApply(nodeGroups ->
            nodeGroups.stream().map(NodeGroup::getKey).toList())
    ));
  }

  /**
   * A command argument that resolves and suggests Discoverables as their namespaced keys.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return The CustomArgument instance
   */
  public CommandArgument<NamespacedKey, Argument<NamespacedKey>> discoverableArgument(
      String nodeName) {
    return arg(new NamespacedKeyArgument(nodeName).includeSuggestions(
        suggestNamespacedKeys(sender -> PathPlugin.getInstance().getStorage().loadAllGroups()
            .thenApply(nodeGroups -> nodeGroups.stream()
                .filter(g -> g.hasModifier(DiscoverableModifier.class))
                .map(NodeGroup::getKey)
                .collect(Collectors.toList())))));
  }

  /**
   * A command argument that resolves a navigation query and suggests the according search terms.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return The CustomArgument instance
   */
  public Argument<NodeSelection> navigateSelectionArgument(String nodeName) {
    return arg(new CustomArgument<>(new GreedyStringArgument(nodeName), context -> {
      if (!(context.sender() instanceof Player player)) {
        throw new CustomArgument.CustomArgumentException("Only for players");
      }
      String search = context.currentInput();
      List<Node<?>> scope = PathPlugin.getInstance().getStorage().loadNodes().join().stream()
          .filter(node -> {
            FindModule.NavigationRequestContext c =
                new FindModule.NavigationRequestContext(player.getUniqueId(), node);
            return FindModule.getInstance().getNavigationFilter().stream()
                .allMatch(predicate -> predicate.test(c));
          })
          .toList();

      try {
        Map<Node<?>, NavigableModifier> map =
            StorageAssistant.loadNodes(NavigableModifier.class).join();
        Collection<Node<?>> target =
            new FindQueryParser().parse(search, scope, n -> map.get(n).getSearchTerms());
        return new NodeSelection(target);
      } catch (Throwable t) {
        throw new CustomArgument.CustomArgumentException(t.getMessage());
      }
    }))
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
          lastIndex = Integer.max(
              suggestionsBuilder.getInput().length() - suggestionsBuilder.getRemaining().length(),
              lastIndex + 1);

          StringRange range = StringRange.between(lastIndex, input.length());
          List<Suggestion> suggestions = new ArrayList<>();

          StringRange finalRange = range;
          String inRange = finalRange.get(input);

          Collection<String> allTerms = new HashSet<>();
          StorageAssistant.loadNodes(NavigableModifier.class).thenAccept(map -> {
            map.forEach((node, navigableModifier) -> {
              allTerms.addAll(navigableModifier.getSearchTermStrings());
            });
          }).join();

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
   * Provides a visualizer type argument, which suggests the keys of all registered visualizer types
   * and resolves the user input into the visualizer type instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a visualizer type argument instance
   */
  public Argument<? extends VisualizerType<?>> visualizerTypeArgument(String nodeName) {
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {

      VisualizerType<?> type =
          VisualizerHandler.getInstance().getVisualizerType(customArgumentInfo.currentInput());
      if (type == null) {
        throw new CustomArgument.CustomArgumentException(
            "Unknown type: '" + customArgumentInfo.currentInput() + "'.");
      }
      return type;
    })).includeSuggestions(suggestNamespacedKeys(
        sender -> CompletableFuture.completedFuture(
            VisualizerHandler.getInstance().getVisualizerTypes().keySet())));
  }
}
