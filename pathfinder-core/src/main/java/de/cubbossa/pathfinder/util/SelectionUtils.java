package de.cubbossa.pathfinder.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.nodeselection.NodeSelectionParser;
import de.cubbossa.pathfinder.nodeselection.NumberRange;
import dev.jorel.commandapi.SuggestionInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SelectionUtils {

  public static final NodeSelectionParser.Argument<UUID> ID =
      new NodeSelectionParser.Argument<>(r -> UUID.fromString(r.getRemaining()))
          .execute(c -> c.getScope().stream()
              .filter(n -> c.getValue().equals(n.getNodeId()))
              .collect(Collectors.toList()))
          .suggestStrings(c -> PathPlugin.getInstance().getStorage().loadNodes().join().stream()
              .map(Node::getNodeId)
              .map(integer -> integer + "")
              .collect(Collectors.toList()));

  public static final NodeSelectionParser.Argument<NumberRange> DISTANCE =
      new NodeSelectionParser.Argument<>(r -> NumberRange.fromString(r.getRemaining()))
          .execute(c -> {
            if (c.getSender() instanceof Player player) {
              PathPlayer<Player> p = PathPlugin.wrap(player);
              return c.getScope().stream()
                  .filter(
                      n -> c.getValue().contains(n.getLocation().distance(p.getLocation())))
                  .collect(Collectors.toList());
            }
            return Lists.newArrayList();
          });

  public static final NodeSelectionParser.Argument<World> WORLD =
      new NodeSelectionParser.Argument<>(r -> {
        World world = Bukkit.getWorld(r.getRemaining());
        if (world == null) {
          throw new RuntimeException("'" + r.getRemaining() + "' is not a valid world.");
        }
        return world;
      }).execute(c -> c.getScope().stream()
              .filter(node -> Objects.equals(node.getLocation().getWorld(), c.getValue().getUID()))
              .collect(Collectors.toList()))
          .suggestStrings(c -> Bukkit.getWorlds().stream()
              .map(World::getName)
              .collect(Collectors.toList()));

  public static final NodeSelectionParser.Argument<Integer> LIMIT =
      new NodeSelectionParser.Argument<>(IntegerArgumentType.integer())
          .execute(c -> CommandUtils.subList(c.getScope(), 0, c.getValue()));

  public static final NodeSelectionParser.Argument<Integer> OFFSET =
      new NodeSelectionParser.Argument<>(IntegerArgumentType.integer())
          .execute(c -> CommandUtils.subList(c.getScope(), c.getValue()));
  public static final NodeSelectionParser.Argument<SortMethod> SORT =
      new NodeSelectionParser.Argument<SortMethod>(
          r -> SortMethod.valueOf(r.getRemaining().toUpperCase()))
          .execute(c -> {
            Location playerLocation = c.getSender() instanceof Player player
                ? PathPlugin.wrap(player).getLocation()
                : new Location(0, 0, 0, null);
            return switch (c.getValue()) {
              case NEAREST -> c.getScope().stream()
                  .sorted(Comparator.comparingDouble(o -> o.getLocation().distance(playerLocation)))
                  .collect(Collectors.toList());
              case FURTHEST -> c.getScope().stream()
                  .sorted((o1, o2) -> Double.compare(o2.getLocation().distance(playerLocation),
                      o1.getLocation().distance(playerLocation)))
                  .collect(Collectors.toList());
              case RANDOM -> c.getScope().stream()
                  .collect(Collectors.collectingAndThen(Collectors.toList(), n -> {
                    Collections.shuffle(n);
                    return n;
                  }));
              case ARBITRARY -> c.getScope().stream()
                  .sorted()
                  .collect(Collectors.toList());
            };
          })
          .suggestStrings(Lists.newArrayList("nearest", "furthest", "random", "arbitrary"));
  public static final NodeSelectionParser.Argument<Collection<NodeGroup>> GROUP =
      new NodeSelectionParser.Argument<>(r -> {
        String in = r.getRemaining();
        Collection<NodeGroup> groups = new HashSet<>();
        NamespacedKey key = NamespacedKey.fromString(in);
        if (key == null) {
          throw new IllegalArgumentException("Invalid namespaced key: '" + in + "'.");
        }
        Optional<NodeGroup> group = PathPlugin.getInstance().getStorage().loadGroup(key).join();
        groups.add(group.orElseThrow(
            () -> new IllegalArgumentException("There is no group with the key '" + key + "'")));
        return groups;
      })
          .execute(c -> c.getScope().stream()
              .filter(node -> node instanceof Groupable<?> groupable
                  && groupable.getGroups()
                  .containsAll(c.getValue()))
              .collect(Collectors.toList()))
          .suggestStrings(c -> PathPlugin.getInstance().getStorage().loadAllGroups().join().stream()
              .map(NodeGroup::getKey)
              .map(NamespacedKey::toString)
              .collect(Collectors.toList()));
  public static final Map<String, NodeSelectionParser.Argument<?>> SELECTORS = Map.of(
      "id", ID,
      "offset", OFFSET,
      "limit", LIMIT,
      "distance", DISTANCE,
      "sort", SORT,
      "world", WORLD,
      "group", GROUP
  );
  private static final NodeSelectionParser parser = new NodeSelectionParser("node", "n", "nodes");

  static {
    SELECTORS.forEach(parser::addResolver);
  }

  public static NodeSelection getNodeSelection(Player player, String selectString)
      throws CommandSyntaxException, ParseCancellationException {

    List<Node<?>> nodes = new ArrayList<>(PathPlugin.getInstance().getStorage().loadNodes().join());
    return new NodeSelection(parser.parse(player, selectString, nodes));
  }

  public static CompletableFuture<Suggestions> getNodeSelectionSuggestions(
      SuggestionInfo suggestionInfo, SuggestionsBuilder suggestionsBuilder) {
    if (!(suggestionInfo.sender() instanceof Player player)) {
      return suggestionsBuilder.buildFuture();
    }
    int offset = suggestionInfo.currentInput().length() - suggestionInfo.currentArg().length();

    return parser
        // remove quotation from input
        .applySuggestions(player, suggestionInfo.currentArg(),
            suggestionInfo.currentArg().length() > 0
                ? suggestionInfo.currentArg().substring(1)
                : "")
        //  add quotations to suggestions
        .thenApply(s -> CommandUtils.wrapWithQuotation(suggestionInfo.currentArg(), s,
            suggestionInfo.currentArg(), offset))
        // shift suggestions toward actual command argument offset
        .thenApply(s -> CommandUtils.offsetSuggestions(suggestionInfo.currentArg(), s, offset));
  }

  private enum SortMethod {
    NEAREST, FURTHEST, RANDOM, ARBITRARY;
  }
}
