package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.module.visualizing.AbstractVisualizer;
import de.cubbossa.pathfinder.module.visualizing.AbstractVisualizer.Property;
import de.cubbossa.pathfinder.module.visualizing.AbstractVisualizer.SimpleProperty;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.api.visualizer.VisualizerType;
import de.cubbossa.pathfinder.util.StringCompass;
import de.cubbossa.pathfinder.util.VectorUtils;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import org.bukkit.entity.Player;

@Getter
@Setter
public class CompassVisualizer
    extends BossBarVisualizer<CompassVisualizer, CompassVisualizer.Data> {

  public static final Property<CompassVisualizer, Integer> PROP_RADIUS =
      new SimpleProperty<>("radius", Integer.class, true,
          CompassVisualizer::getRadius, CompassVisualizer::setRadius);

  public static final Property<CompassVisualizer, String> PROP_BACKGROUND =
      new SimpleProperty<>("background", String.class, true,
          CompassVisualizer::getBackgroundFormat, CompassVisualizer::setBackgroundFormat);

  public static final Property<CompassVisualizer, String> PROP_NORTH =
      new SimpleProperty<>("marker-north", String.class, true,
          CompassVisualizer::getNorth, CompassVisualizer::setNorth);

  public static final Property<CompassVisualizer, String> PROP_EAST =
      new SimpleProperty<>("marker-east", String.class, true,
          CompassVisualizer::getEast, CompassVisualizer::setEast);

  public static final Property<CompassVisualizer, String> PROP_SOUTH =
      new SimpleProperty<>("marker-south", String.class, true,
          CompassVisualizer::getSouth, CompassVisualizer::setSouth);

  public static final Property<CompassVisualizer, String> PROP_WEST =
      new SimpleProperty<>("marker-west", String.class, true,
          CompassVisualizer::getWest, CompassVisualizer::setWest);

  public static final Property<CompassVisualizer, String> PROP_TARGET =
      new SimpleProperty<>("marker-target", String.class, true,
          CompassVisualizer::getTarget, CompassVisualizer::setTarget);
  private String backgroundFormat =
      "<gray>" + "  |- · · · -+- · · · -|- · · · -+- · · · -| ".repeat(4);
  private String north = "<red>N</red>";
  private String east = "<red>E</red>";
  private String south = "<red>S</red>";
  private String west = "<red>W</red>";
  private String target = "<green>♦</green>";
  private int radius = 20;
  private Location leadPoint = null;

  public CompassVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
  }

  @Override
  public VisualizerType<CompassVisualizer> getType() {
    return VisualizerHandler.COMPASS_VISUALIZER_TYPE;
  }

  @Override
  public Data newData(UUID player, List<Node<?>> nodes, List<Edge> edges, BossBar bossBar) {
    StringCompass compass = new StringCompass(backgroundFormat, radius, null);
    compass.addMarker("N", north, 0.);
    compass.addMarker("E", east, 90.);
    compass.addMarker("S", south, 180.);
    compass.addMarker("W", west, 270.);
    return new Data(nodes, edges, bossBar, compass);
  }

  @Override
  public void play(VisualizerContext<Data> context, Location nearestPoint, Location leadPoint,
                   Edge nearestEdge) {
    if (context.data().getCompass().getAngle() == null) {
      Player player = Bukkit.getPlayer(context.player());
      context.data().getCompass().setAngle(() -> {
        return VectorUtils.convertDirectionToXZAngle(player.getLocation());
      });
      context.data().getCompass().addMarker("target", target, () -> {
        return VectorUtils.convertDirectionToXZAngle(
            this.leadPoint.clone().subtract(player.getLocation()).toVector());
      });
    }
    this.leadPoint = leadPoint;
    context.data().getBossBar().name(context.data().getCompass());
  }

  @Getter
  public static class Data extends BossBarVisualizer.Data {
    private final StringCompass compass;

    public Data(List<Node<?>> nodes, List<Edge> edges, BossBar bossBar, StringCompass compass) {
      super(nodes, edges, bossBar);
      this.compass = compass;
    }
  }
}
