package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.util.StringCompass;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
@Setter
public class CompassVisualizer
    extends BossBarVisualizer<CompassVisualizer.CompassBossbarView> {

  public static final Property<CompassVisualizer, Integer> PROP_RADIUS =
      new PropertyImpl<>("radius", Integer.class,
          CompassVisualizer::getRadius, CompassVisualizer::setRadius);

  public static final Property<CompassVisualizer, String> PROP_BACKGROUND =
      new PropertyImpl<>("background", String.class,
          CompassVisualizer::getBackgroundFormat, CompassVisualizer::setBackgroundFormat);

  public static final Property<CompassVisualizer, String> PROP_NORTH =
      new PropertyImpl<>("marker-north", String.class,
          CompassVisualizer::getNorth, CompassVisualizer::setNorth);

  public static final Property<CompassVisualizer, String> PROP_EAST =
      new PropertyImpl<>("marker-east", String.class,
          CompassVisualizer::getEast, CompassVisualizer::setEast);

  public static final Property<CompassVisualizer, String> PROP_SOUTH =
      new PropertyImpl<>("marker-south", String.class,
          CompassVisualizer::getSouth, CompassVisualizer::setSouth);

  public static final Property<CompassVisualizer, String> PROP_WEST =
      new PropertyImpl<>("marker-west", String.class,
          CompassVisualizer::getWest, CompassVisualizer::setWest);

  public static final Property<CompassVisualizer, String> PROP_TARGET =
      new PropertyImpl<>("marker-target", String.class,
          CompassVisualizer::getTarget, CompassVisualizer::setTarget);
  private String backgroundFormat =
      "<gray>" + "  |- · · · -+- · · · -|- · · · -+- · · · -| ".repeat(4);
  private String north = "<red>N</red>";
  private String east = "<red>E</red>";
  private String south = "<red>S</red>";
  private String west = "<red>W</red>";
  private String target = "<green>♦</green>";
  private int radius = 20;

  public CompassVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public CompassBossbarView createView(PathPlayer<Player> player, UpdatingPath nodes, BossBar bossBar) {
    return new CompassBossbarView(player, nodes, bossBar);
  }

  @Getter
  public class CompassBossbarView extends BossBarVisualizer<CompassBossbarView>.BossbarView {
    private final StringCompass compass;
    private Location leadPoint = new Location(null, 0, 0, 0);

    public CompassBossbarView(PathPlayer<Player> p, UpdatingPath nodes, BossBar bossBar) {
      super(p, nodes, bossBar);
      this.compass = new StringCompass(backgroundFormat, radius, null);
      compass.addMarker("N", north, 0.);
      compass.addMarker("E", east, 90.);
      compass.addMarker("S", south, 180.);
      compass.addMarker("W", west, 270.);
      compass.setAngle(() -> {
        Player player = getTargetViewer().unwrap();
        if (player == null || !player.isOnline()) {
          return 0d;
        }
        return BukkitVectorUtils.convertYawToAngle(player.getLocation());
      });
      compass.addMarker("target", target, () -> {
        Player player = getTargetViewer().unwrap();
        if (player == null || !player.isOnline()) {
          return 0.;
        }
        Location location = leadPoint.clone().subtract(player.getLocation());
        return BukkitVectorUtils.convertDirectionToXZAngle(location);
      });
    }

    @Override
    public void play(Location nearestPoint, Location leadPoint, Edge nearestEdge) {
      this.leadPoint = leadPoint;
      getBossBar().name(compass);
    }


  }
}
