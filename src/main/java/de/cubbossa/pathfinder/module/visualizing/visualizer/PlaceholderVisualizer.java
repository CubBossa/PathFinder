package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.hook.PlaceholderHook;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.util.VectorUtils;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@Getter
@Setter
public class PlaceholderVisualizer
    extends EdgeBasedVisualizer<PlaceholderVisualizer, PlaceholderVisualizer.Data> {

  public static final Property<PlaceholderVisualizer, String> PROP_NORTH =
      new Property.SimpleProperty<>("format-north",
          String.class, true, PlaceholderVisualizer::getNorth, PlaceholderVisualizer::setNorth);
  public static final Property<PlaceholderVisualizer, String> PROP_NORTH_EAST =
      new Property.SimpleProperty<>("format-north-east",
          String.class, true, PlaceholderVisualizer::getNorthEast,
          PlaceholderVisualizer::setNorthEast);
  public static final Property<PlaceholderVisualizer, String> PROP_EAST =
      new Property.SimpleProperty<>("format-east",
          String.class, true, PlaceholderVisualizer::getEast, PlaceholderVisualizer::setEast);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH_EAST =
      new Property.SimpleProperty<>("format-south-east",
          String.class, true, PlaceholderVisualizer::getSouthEast,
          PlaceholderVisualizer::setSouthEast);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH =
      new Property.SimpleProperty<>("format-south",
          String.class, true, PlaceholderVisualizer::getSouth, PlaceholderVisualizer::setSouth);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH_WEST =
      new Property.SimpleProperty<>("format-south-west",
          String.class, true, PlaceholderVisualizer::getSouthWest,
          PlaceholderVisualizer::setSouthWest);
  public static final Property<PlaceholderVisualizer, String> PROP_WEST =
      new Property.SimpleProperty<>("format-west",
          String.class, true, PlaceholderVisualizer::getWest, PlaceholderVisualizer::setWest);
  public static final Property<PlaceholderVisualizer, String> PROP_NORTH_WEST =
      new Property.SimpleProperty<>("format-north-west",
          String.class, true, PlaceholderVisualizer::getNorthWest,
          PlaceholderVisualizer::setNorthWest);
  public static final Property<PlaceholderVisualizer, String> PROP_DISTANCE =
      new Property.SimpleProperty<>("format-distance",
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
  public Data newData(Player player, List<Node> nodes, List<Edge> edges) {
    return new Data(nodes, edges);
  }

  @Override
  public Data prepare(List<Node> nodes, Player player) {
    Data data = super.prepare(nodes, player);
    PlaceholderHook.getInstance().register(PlaceholderHook.DIRECTION, player, data::getDirection);
    PlaceholderHook.getInstance().register(PlaceholderHook.DISTANCE, player, data::getDistance);
    return data;
  }

  @Override
  public void play(VisualizerContext<Data> context, Location nearestPoint, Location leadPoint,
                   Edge nearestEdge) {
    double distance = context.player().getLocation().distance(nearestPoint) + nearestPoint.distance(
        nearestEdge.target());
    int nearestEdgeIndex = context.data().getEdges().indexOf(nearestEdge);
    for (Edge edge : context.data().getEdges()
        .subList(nearestEdgeIndex + 1, context.data().getEdges().size())) {
      distance += edge.support().distance(edge.target());
    }


    double angle = VectorUtils.convertDirectionToXZAngle(
        leadPoint.clone().subtract(context.player().getLocation()).toVector());

    context.data().direction = directions[(int) ((angle + 22.5) / 45) % 8];
    context.data().distance = resolveDistance(distance);
  }

  public String resolveDistance(double distance) {
    return resolver.serialize(
            resolver.deserialize(distanceFormat, Formatter.number("distance", distance)))
        .replace("\\<", "<");
  }

  @Override
  public VisualizerType<PlaceholderVisualizer> getType() {
    return PlaceholderHook.PLACEHOLDER_VISUALIZER_TYPE;
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
