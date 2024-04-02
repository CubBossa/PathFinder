package de.cubbossa.pathfinder.visualizer.impl;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.tinytranslations.Message;
import dev.jorel.commandapi.arguments.Argument;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;

@AutoService(VisualizerType.class)
public class CompassVisualizerType extends IntervalVisualizerType<CompassVisualizer>
    implements VisualizerTypeCommandExtension, VisualizerTypeMessageExtension<CompassVisualizer> {

  public CompassVisualizerType() {
    super(AbstractPathFinder.pathfinder("compass"));
  }

  @Override
  public CompassVisualizer create(NamespacedKey key) {
    CompassVisualizer visualizer = new CompassVisualizer(key);
    visualizer.setInterval(1);
    return visualizer;
  }

  @Override
  public Message getInfoMessage(CompassVisualizer element) {
    return Messages.CMD_VIS_COMPASS_INFO
        .insertString("bb_color", element.getColor().toString().toLowerCase())
        .insertString("overlay", element.getOverlay().toString().toLowerCase())
        .insertParsed("background", element.getBackgroundFormat())
        .insertParsed("marker-north", element.getNorth())
        .insertParsed("marker-east", element.getEast())
        .insertParsed("marker-south", element.getSouth())
        .insertParsed("marker-west", element.getWest())
        .insertParsed("marker-target", element.getTarget())
        .insertNumber(CompassVisualizer.PROP_RADIUS.getKey(), element.getRadius());
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset) {
    return super.appendEditCommand(tree, visualizerIndex, argumentOffset)
        .then(subCommand("color", Arguments.enumArgument("value", BossBar.Color.class),
            BossBarVisualizer.PROP_COLOR))
        .then(subCommand("overlay", Arguments.enumArgument("value", BossBar.Overlay.class),
            BossBarVisualizer.PROP_OVERLAY))
        .then(subCommand("background", Arguments.miniMessageArgument("value"),
            CompassVisualizer.PROP_BACKGROUND))
        .then(subCommand("north", Arguments.miniMessageArgument("value"),
            CompassVisualizer.PROP_NORTH))
        .then(subCommand("east", Arguments.miniMessageArgument("value"),
            CompassVisualizer.PROP_EAST))
        .then(subCommand("south", Arguments.miniMessageArgument("value"),
            CompassVisualizer.PROP_SOUTH))
        .then(subCommand("west", Arguments.miniMessageArgument("value"),
            CompassVisualizer.PROP_WEST))
        .then(subCommand("target", Arguments.miniMessageArgument("value"),
            CompassVisualizer.PROP_TARGET))
        .then(subCommand("radius", Arguments.integer("value", 1), CompassVisualizer.PROP_RADIUS));
  }

  @Override
  public Map<String, Object> serialize(CompassVisualizer visualizer) {
    Map<String, Object> map = super.serialize(visualizer);
    map.put(BossBarVisualizer.PROP_COLOR.getKey(), visualizer.getColor().toString());
    map.put(BossBarVisualizer.PROP_OVERLAY.getKey(), visualizer.getOverlay().toString());
    map.put(CompassVisualizer.PROP_BACKGROUND.getKey(), visualizer.getBackgroundFormat());
    map.put(CompassVisualizer.PROP_NORTH.getKey(), visualizer.getNorth());
    map.put(CompassVisualizer.PROP_EAST.getKey(), visualizer.getEast());
    map.put(CompassVisualizer.PROP_SOUTH.getKey(), visualizer.getSouth());
    map.put(CompassVisualizer.PROP_WEST.getKey(), visualizer.getWest());
    map.put(CompassVisualizer.PROP_TARGET.getKey(), visualizer.getTarget());
    map.put(CompassVisualizer.PROP_RADIUS.getKey(), visualizer.getRadius());
    return map;
  }

  @Override
  public void deserialize(CompassVisualizer visualizer, Map<String, Object> values) {
    super.deserialize(visualizer, values);
    // bossbar
    loadEnumProperty(values, BossBarVisualizer.PROP_COLOR.getKey(), BossBar.Color.class,
        visualizer::setColor);
    loadEnumProperty(values, BossBarVisualizer.PROP_OVERLAY.getKey(), BossBar.Overlay.class,
        visualizer::setOverlay);
    // compass
    loadProperty(values, CompassVisualizer.PROP_BACKGROUND.getKey(), String.class,
        visualizer::setBackgroundFormat);
    loadProperty(values, CompassVisualizer.PROP_NORTH.getKey(), String.class, visualizer::setNorth);
    loadProperty(values, CompassVisualizer.PROP_EAST.getKey(), String.class, visualizer::setEast);
    loadProperty(values, CompassVisualizer.PROP_SOUTH.getKey(), String.class, visualizer::setSouth);
    loadProperty(values, CompassVisualizer.PROP_WEST.getKey(), String.class, visualizer::setWest);
    loadProperty(values, CompassVisualizer.PROP_TARGET.getKey(), String.class,
        visualizer::setTarget);
    loadProperty(values, CompassVisualizer.PROP_RADIUS.getKey(), Integer.class,
        visualizer::setRadius);
  }
}
