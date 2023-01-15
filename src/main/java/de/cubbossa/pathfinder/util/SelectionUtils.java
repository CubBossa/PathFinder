package de.cubbossa.pathfinder.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.util.selection.NodeSelectionParser;
import de.cubbossa.pathfinder.util.selection.NumberRange;
import dev.jorel.commandapi.SuggestionInfo;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SelectionUtils {

  public static final NodeSelectionParser.Argument<NumberRange> ID =
      new NodeSelectionParser.Argument<NumberRange>(r -> {
        try {
          return NumberRange.parse(r.getRemaining());
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }
      })
          .execute(c -> c.getScope().stream()
              .filter(n -> c.getValue().contains(n.getNodeId()))
              .collect(Collectors.toList()))
          .suggestStrings(c -> RoadMapHandler.getInstance().getRoadMaps().values().stream()
              .map(RoadMap::getNodes)
              .flatMap(Collection::stream)
              .map(Node::getNodeId)
              .map(integer -> integer + "")
              .collect(Collectors.toList()));

  public static final NodeSelectionParser.Argument<NumberRange> TANGENT_LENGTH =
      NodeSelectionParser.argument(r -> NumberRange.fromString(r.getRemaining()))
          .execute(c -> c.getScope().stream()
              .filter(n -> {
                Double curveLength = n.getCurveLength();
                if (curveLength == null) {
                  curveLength = RoadMapHandler.getInstance().getRoadMap(n.getRoadMapKey())
                      .getDefaultCurveLength();
                }
                return c.getValue().contains(curveLength);
              })
              .collect(Collectors.toList()));

  public static final NodeSelectionParser.Argument<NumberRange> DISTANCE =
      new NodeSelectionParser.Argument<>(r -> NumberRange.fromString(r.getRemaining()))
          .execute(c -> {
            if (c instanceof Player player) {
              return c.getScope().stream()
                  .filter(
                      n -> c.getValue().contains(n.getLocation().distance(player.getLocation())))
                  .collect(Collectors.toList());
            }
            return Lists.newArrayList();
          });

  public static final NodeSelectionParser.Argument<NamespacedKey> ROADMAP =
      new NodeSelectionParser.Argument<>(
          r -> NamespacedKey.fromString(r.getRemaining()))
          .execute(c -> {
            NamespacedKey roadmapKey = c.getValue();
            return c.getScope().stream()
                .filter(n -> n.getRoadMapKey().equals(roadmapKey))
                .collect(Collectors.toList());
          })
          .suggestStrings(RoadMapHandler.getInstance().getRoadMapsStream()
              .map(RoadMap::getKey)
              .map(NamespacedKey::toString)
              .collect(Collectors.toList()));


  public static final NodeSelectionParser.Argument<World> WORLD =
      new NodeSelectionParser.Argument<World>(r -> {
        World world = Bukkit.getWorld(r.getRemaining());
        if (world == null) {
          throw new RuntimeException("'" + r.getRemaining() + "' is not a valid world.");
        }
        return world;
      }).execute(c -> c.getScope().stream()
              .filter(node -> Objects.equals(node.getLocation().getWorld(), c.getValue()))
              .collect(Collectors.toList()))
          .suggestStrings(c -> Bukkit.getWorlds().stream()
              .map(World::getName)
              .collect(Collectors.toList()));

  public static final NodeSelectionParser.Argument<Integer> LIMIT =
      new NodeSelectionParser.Argument<Integer>(IntegerArgumentType.integer())
          .execute(c -> CommandUtils.subList(c.getScope(), 0, c.getValue()));

  public static final NodeSelectionParser.Argument<Integer> OFFSET =
      new NodeSelectionParser.Argument<Integer>(IntegerArgumentType.integer())
          .execute(c -> CommandUtils.subList(c.getScope(), c.getValue()));

  private enum SortMethod {
    NEAREST, FURTHEST, RANDOM, ARBITRARY;
  }

  public static final NodeSelectionParser.Argument<SortMethod> SORT =
      new NodeSelectionParser.Argument<SortMethod>(
          r -> SortMethod.valueOf(r.getRemaining().toUpperCase()))
          .execute(c -> {
            Location playerLocation = c.getSender() instanceof Player player
                ? player.getLocation()
                : new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
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
                  .sorted(Comparator.comparingInt(Node::getNodeId))
                  .collect(Collectors.toList());
            };
          })
          .suggestStrings(Lists.newArrayList("nearest", "furthest", "random", "arbitrary"));

  public static final NodeSelectionParser.Argument<Collection<NodeGroup>> GROUP =
      new NodeSelectionParser.Argument<>(r -> {
        String in = r.getRemaining();
        Collection<NodeGroup> groups = new HashSet<>();
        if (in.startsWith("@")) {
          // TODO parse groups
        } else {
          NamespacedKey key = NamespacedKey.fromString(in);
          if (key == null) {
            throw new IllegalArgumentException("Invalid namespaced key: '" + in + "'.");
          }
          NodeGroup group = NodeGroupHandler.getInstance().getNodeGroup(key);
          if (group == null) {
            throw new IllegalArgumentException("There is no group with the key '" + key + "'");
          }
          groups.add(group);
        }
        return groups;
      })
          .execute(c -> c.getScope().stream()
              .filter(node -> node instanceof Groupable groupable
                  && groupable.getGroups().containsAll(c.getValue()))
              .collect(Collectors.toList()))
          .suggestStrings(c -> NodeGroupHandler.getInstance().getNodeGroups().stream()
              .map(NodeGroup::getKey)
              .map(NamespacedKey::toString)
              .collect(Collectors.toList()));

  public static final Map<String, NodeSelectionParser.Argument<?>> SELECTORS = Map.of(
      "id", ID,
      "offset", OFFSET,
      "limit", LIMIT,
      "distance", DISTANCE,
      "curvelength", TANGENT_LENGTH,
      "sort", SORT,
      "world", WORLD,
      "roadmap", ROADMAP,
      "group", GROUP
  );

  private static final NodeSelectionParser parser = new NodeSelectionParser("node", "n", "nodes");

  static {
    SELECTORS.forEach(parser::addResolver);
  }

  public static NodeSelection getNodeSelection(Player player, String selectString)
      throws CommandSyntaxException {

    return new NodeSelection(parser.parse(
        player,
        selectString,
        RoadMapHandler.getInstance().getRoadMaps().values().stream()
            .flatMap(roadMap -> roadMap.getNodes().stream())
            .collect(Collectors.toList())));
  }

  public static CompletableFuture<Suggestions> getNodeSelectionSuggestions(
      SuggestionInfo suggestionInfo, SuggestionsBuilder suggestionsBuilder)
      throws CommandSyntaxException {
    return suggestionInfo.sender() instanceof Player player ?
        parser.applySuggestions(player, suggestionInfo.currentInput()) :
        suggestionsBuilder.buildFuture();
  }
}
