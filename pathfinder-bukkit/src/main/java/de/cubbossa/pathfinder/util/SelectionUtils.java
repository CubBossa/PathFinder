package de.cubbossa.pathfinder.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.Range;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.node.selection.BukkitSelectionParser;
import de.cubbossa.pathfinder.node.selection.NumberRange;
import de.cubbossa.pathfinder.storage.StorageUtil;
import dev.jorel.commandapi.SuggestionInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

  public static final BukkitSelectionParser.Argument<UUID> ID =
      new BukkitSelectionParser.Argument<>(r -> UUID.fromString(r.getRemaining()))
          .execute(c -> c.getScope().stream()
              .filter(n -> c.getValue().equals(n.getNodeId()))
              .collect(Collectors.toList()))
          .suggestStrings(c -> PathFinderProvider.get().getStorage().loadNodes().join().stream()
              .map(Node::getNodeId)
              .map(integer -> integer + "")
              .collect(Collectors.toList()));

  public static final BukkitSelectionParser.Argument<NumberRange> DISTANCE =
      new BukkitSelectionParser.Argument<>(r -> NumberRange.fromString(r.getRemaining()))
          .execute(c -> {
            if (c.getSender() instanceof Player player) {
              PathPlayer<Player> p = BukkitPathFinder.wrap(player);
              return c.getScope().stream()
                  .filter(
                      n -> c.getValue().contains(n.getLocation().distance(p.getLocation())))
                  .collect(Collectors.toList());
            }
            return Lists.newArrayList();
          });

  public static final BukkitSelectionParser.Argument<World> WORLD =
      new BukkitSelectionParser.Argument<>(r -> {
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

  public static final BukkitSelectionParser.Argument<Integer> LIMIT =
      new BukkitSelectionParser.Argument<>(IntegerArgumentType.integer())
          .execute(c -> CollectionUtils.subList(c.getScope(), Range.range(0, c.getValue())));

  public static final BukkitSelectionParser.Argument<Integer> OFFSET =
      new BukkitSelectionParser.Argument<>(IntegerArgumentType.integer())
          .execute(c -> CollectionUtils.subList(c.getScope(), c.getValue()));
  public static final BukkitSelectionParser.Argument<SortMethod> SORT =
      new BukkitSelectionParser.Argument<>(
          r -> SortMethod.valueOf(r.getRemaining().toUpperCase()))
          .execute(c -> {
            Location playerLocation = c.getSender() instanceof Player player
                ? BukkitPathFinder.wrap(player).getLocation()
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
  public static final BukkitSelectionParser.Argument<Collection<NodeGroup>> GROUP =
      new BukkitSelectionParser.Argument<>(r -> {
        String in = r.getRemaining();
        Collection<NodeGroup> groups = new HashSet<>();
        NamespacedKey key = NamespacedKey.fromString(in);
        if (key == null) {
          throw new IllegalArgumentException("Invalid namespaced key: '" + in + "'.");
        }
        Optional<NodeGroup> group = PathFinderProvider.get().getStorage().loadGroup(key).join();
        groups.add(group.orElseThrow(
            () -> new IllegalArgumentException("There is no group with the key '" + key + "'")));
        return groups;
      })
          .execute(c -> c.getScope().stream()
              .filter(node -> StorageUtil.getGroups(node).containsAll(c.getValue()))
              .collect(Collectors.toList()))
          .suggestStrings(c -> PathFinderProvider.get().getStorage().loadAllGroups().join().stream()
              .map(NodeGroup::getKey)
              .map(NamespacedKey::toString)
              .collect(Collectors.toList()));
  public static final Map<String, BukkitSelectionParser.Argument<?>> SELECTORS = Map.of(
      "id", ID,
      "offset", OFFSET,
      "limit", LIMIT,
      "distance", DISTANCE,
      "sort", SORT,
      "world", WORLD,
      "group", GROUP
  );
  private static final BukkitSelectionParser parser = new BukkitSelectionParser("node", "n", "nodes");

  static {
    SELECTORS.forEach(parser::addResolver);
  }

  public static NodeSelection getNodeSelection(Player player, String selectString)
      throws ParseCancellationException {

    List<Node> nodes = new ArrayList<>(PathFinderProvider.get().getStorage().loadNodes().join());
    NodeSelection selection = new NodeSelection(parser.parse(player, selectString, nodes));
    selection.setMeta(new NodeSelection.Meta(selectString, new HashMap<>()));
    return selection;
  }

  public static CompletableFuture<Suggestions> getNodeSelectionSuggestions(SuggestionInfo suggestionInfo, SuggestionsBuilder suggestionsBuilder) {
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
