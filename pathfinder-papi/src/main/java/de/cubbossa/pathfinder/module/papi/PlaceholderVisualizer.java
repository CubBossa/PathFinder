package de.cubbossa.pathfinder.module.papi;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.visualizer.impl.EdgeBasedVisualizer;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
@Setter
public class PlaceholderVisualizer
    extends EdgeBasedVisualizer<PlaceholderVisualizer.PlaceholderView> {

  public static final Property<PlaceholderVisualizer, String> PROP_NORTH =
      new PropertyImpl<>("format-north",
          String.class, PlaceholderVisualizer::getNorth, PlaceholderVisualizer::setNorth);
  public static final Property<PlaceholderVisualizer, String> PROP_NORTH_EAST =
      new PropertyImpl<>("format-north-east",
          String.class, PlaceholderVisualizer::getNorthEast,
          PlaceholderVisualizer::setNorthEast);
  public static final Property<PlaceholderVisualizer, String> PROP_EAST =
      new PropertyImpl<>("format-east",
          String.class, PlaceholderVisualizer::getEast, PlaceholderVisualizer::setEast);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH_EAST =
      new PropertyImpl<>("format-south-east",
          String.class, PlaceholderVisualizer::getSouthEast,
          PlaceholderVisualizer::setSouthEast);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH =
      new PropertyImpl<>("format-south",
          String.class, PlaceholderVisualizer::getSouth, PlaceholderVisualizer::setSouth);
  public static final Property<PlaceholderVisualizer, String> PROP_SOUTH_WEST =
      new PropertyImpl<>("format-south-west",
          String.class, PlaceholderVisualizer::getSouthWest,
          PlaceholderVisualizer::setSouthWest);
  public static final Property<PlaceholderVisualizer, String> PROP_WEST =
      new PropertyImpl<>("format-west",
          String.class, PlaceholderVisualizer::getWest, PlaceholderVisualizer::setWest);
  public static final Property<PlaceholderVisualizer, String> PROP_NORTH_WEST =
      new PropertyImpl<>("format-north-west",
          String.class, PlaceholderVisualizer::getNorthWest,
          PlaceholderVisualizer::setNorthWest);
  public static final Property<PlaceholderVisualizer, String> PROP_DISTANCE =
      new PropertyImpl<>("format-distance",
          String.class, PlaceholderVisualizer::getDistanceFormat,
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

  public PlaceholderVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public PlaceholderView createView(List<Node> nodes, PathPlayer<Player> player) {
    PlaceholderView placeholderView = super.createView(nodes, player);
    Player bp = player.unwrap();
    PlaceholderHook.getInstance().register(PlaceholderHook.DIRECTION, bp, placeholderView::getDirection);
    PlaceholderHook.getInstance().register(PlaceholderHook.DISTANCE, bp, placeholderView::getDistance);
    return placeholderView;
  }

  @Override
  public PlaceholderView createView(List<Node> nodes, List<Edge> edges, PathPlayer<Player> player) {
    return new PlaceholderView(player, nodes, edges);
  }

  public String resolveDistance(double distance) {
    return resolver.serialize(
            resolver.deserialize(distanceFormat, Messages.formatter().number("distance", distance)))
        .replace("\\<", "<");
  }

  @Getter
  @Setter
  protected class PlaceholderView extends EdgeBasedVisualizer<PlaceholderView>.EdgeBasedView {

    private String direction = "", distance = "";

    public PlaceholderView(PathPlayer<Player> player, List<Node> nodes, List<Edge> edges) {
      super(player, nodes, edges);
    }

    @Override
    public void play(Location nearestPoint, Location leadPoint, Edge nearestEdge) {
      Location playerLoc = BukkitVectorUtils.toBukkit(getTargetViewer().getLocation());

      double dist = playerLoc.distance(nearestPoint) + nearestPoint.distance(nearestEdge.target());
      int nearestEdgeIndex = getEdges().indexOf(nearestEdge);
      for (Edge edge : getEdges().subList(nearestEdgeIndex + 1, getEdges().size())) {
        dist += edge.support().distance(edge.target());
        dist += nearestEdge.support().distance(nearestEdge.target());
      }

      double angle = BukkitVectorUtils.convertDirectionToXZAngle(
          leadPoint.clone().subtract(playerLoc));

      direction = directions[(int) ((angle + 22.5) / 45) % 8];
      distance = resolveDistance(dist);
    }
  }
}
