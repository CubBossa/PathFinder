package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.visualizer.impl.EdgeBasedVisualizer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
@Setter
public class PlaceholderVisualizer
    extends EdgeBasedVisualizer<PlaceholderVisualizer, PlaceholderVisualizer.Data> {

  public static final Property<PlaceholderVisualizer, String> PROP_NORTH =
      new SimpleProperty<>("format-north",
          String.class, true, PlaceholderVisualizer::getNorth, PlaceholderVisualizer::setNorth);
  public static final Property<PlaceholderVisualizer, String> PROP_NORTH_EAST =
      new SimpleProperty<>("format-north-east",
          String.class, true, PlaceholderVisualizer::getNorthEast,
          PlaceholderVisualizer::setNorthEast);
  public static final Property<PlaceholderVisualizer, String> PROP_EAST =
      new SimpleProperty<>("format-east",
          String.class, true, PlaceholderVisualizer::getEast, PlaceholderVisualizer::setEast);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH_EAST =
      new SimpleProperty<>("format-south-east",
          String.class, true, PlaceholderVisualizer::getSouthEast,
          PlaceholderVisualizer::setSouthEast);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH =
      new SimpleProperty<>("format-south",
          String.class, true, PlaceholderVisualizer::getSouth, PlaceholderVisualizer::setSouth);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH_WEST =
      new SimpleProperty<>("format-south-west",
          String.class, true, PlaceholderVisualizer::getSouthWest,
          PlaceholderVisualizer::setSouthWest);
  public static final Property<PlaceholderVisualizer, String> PROP_WEST =
      new SimpleProperty<>("format-west",
          String.class, true, PlaceholderVisualizer::getWest, PlaceholderVisualizer::setWest);
  public static final Property<PlaceholderVisualizer, String> PROP_NORTH_WEST =
      new SimpleProperty<>("format-north-west",
          String.class, true, PlaceholderVisualizer::getNorthWest,
          PlaceholderVisualizer::setNorthWest);
  public static final Property<PlaceholderVisualizer, String> PROP_DISTANCE =
      new SimpleProperty<>("format-distance",
          String.class, true, PlaceholderVisualizer::getDistanceFormat,
          PlaceholderVisualizer::setDistanceFormat);
  public static final Property<PlaceholderVisualizer, String>[] PROPS = new Property[] {
      PROP_NORTH, PROP_NORTH_EAST, PROP_EAST, PROP_SOUTH_EAST, PROP_SOUTH, PROP_SOUTH_WEST,
      PROP_WEST, PROP_NORTH_WEST, PROP_DISTANCE
  };
  private final MiniMessage resolver = MiniMessage.builder()
      .tags(TagResolver.empty())
      .build();
  private String north = "N";
  private String northEast = "NE";
  private String east = "E";
  private String southEast = "SE";
  private String south = "S";
  private String southWest = "SW";
  private String west = "W";
  private String northWest = "NW";
  private String[] directions =
      {north, northEast, east, southEast, south, southWest, west, northWest};
  private String distanceFormat = "<distance:#.#> Blocks away";

  public PlaceholderVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
  }

  @Override
  public Data newData(PathPlayer<Player> player, List<Node> nodes, List<Edge> edges) {
    return new Data(nodes, edges);
  }

  @Override
  public Data prepare(List<Node> nodes, PathPlayer<Player> player) {
    Data data = super.prepare(nodes, player);
    Player bp = player.unwrap();
    PlaceholderHook.getInstance().register(PlaceholderHook.DIRECTION, bp, data::getDirection);
    PlaceholderHook.getInstance().register(PlaceholderHook.DISTANCE, bp, data::getDistance);
    return data;
  }

  @Override
  public void play(VisualizerContext<Data, Player> context, Location nearestPoint,
                   Location leadPoint,
                   Edge nearestEdge) {
    double distance = BukkitVectorUtils.toBukkit(context.player().getLocation()).distance(nearestPoint)
        + nearestPoint.distance(
        nearestEdge.target());
    int nearestEdgeIndex = context.data().getEdges().indexOf(nearestEdge);
    for (Edge edge : context.data().getEdges()
        .subList(nearestEdgeIndex + 1, context.data().getEdges().size())) {
      distance += edge.support().distance(edge.target());
    }


    double angle = BukkitVectorUtils.convertDirectionToXZAngle(
        leadPoint.clone().subtract(BukkitVectorUtils.toBukkit(context.player().getLocation())));

    context.data().direction = directions[(int) ((angle + 22.5) / 45) % 8];
    context.data().distance = resolveDistance(distance);
  }

  public String resolveDistance(double distance) {
    return resolver.serialize(
            resolver.deserialize(distanceFormat, Formatter.number("distance", distance)))
        .replace("\\<", "<");
  }

  @Getter
  @Setter
  protected static class Data extends EdgeBasedVisualizer.Data {

    private String direction = "", distance = "";

    public Data(List<Node> nodes, List<Edge> edges) {
      super(nodes, edges);
    }
  }
}
