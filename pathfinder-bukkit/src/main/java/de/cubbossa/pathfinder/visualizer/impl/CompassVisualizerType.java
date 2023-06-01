package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.Argument;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.LinkedHashMap;
import java.util.Map;

public class CompassVisualizerType extends AbstractVisualizerType<CompassVisualizer>
    implements VisualizerTypeCommandExtension, VisualizerTypeMessageExtension<CompassVisualizer> {

  public CompassVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public CompassVisualizer create(NamespacedKey key) {
    CompassVisualizer visualizer = new CompassVisualizer(key);
    visualizer.setInterval(1);
    return visualizer;
  }

  @Override
  public Message getInfoMessage(CompassVisualizer element) {
    return Messages.CMD_VIS_COMPASS_INFO.formatted(
        Placeholder.unparsed("color", element.getColor().toString().toLowerCase()),
        Placeholder.unparsed("overlay", element.getOverlay().toString().toLowerCase()),
        Placeholder.parsed("background", element.getBackgroundFormat()),
        Placeholder.parsed("marker-north", element.getNorth()),
        Placeholder.parsed("marker-east", element.getEast()),
        Placeholder.parsed("marker-south", element.getSouth()),
        Placeholder.parsed("marker-west", element.getWest()),
        Placeholder.parsed("marker-target", element.getTarget()),
        Placeholder.unparsed(CompassVisualizer.PROP_RADIUS.getKey(), String.valueOf(element.getRadius()))
    );
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex,
                                       int argumentOffset) {
    return tree
        .then(subCommand("color", CustomArgs.enumArgument("value", BossBar.Color.class),
            BossBarVisualizer.PROP_COLOR))
        .then(subCommand("overlay", CustomArgs.enumArgument("value", BossBar.Overlay.class),
            BossBarVisualizer.PROP_OVERLAY))
        .then(subCommand("background", CustomArgs.miniMessageArgument("value"),
            CompassVisualizer.PROP_BACKGROUND))
        .then(subCommand("north", CustomArgs.miniMessageArgument("value"),
            CompassVisualizer.PROP_NORTH))
        .then(subCommand("east", CustomArgs.miniMessageArgument("value"),
            CompassVisualizer.PROP_EAST))
        .then(subCommand("south", CustomArgs.miniMessageArgument("value"),
            CompassVisualizer.PROP_SOUTH))
        .then(subCommand("west", CustomArgs.miniMessageArgument("value"),
            CompassVisualizer.PROP_WEST))
        .then(subCommand("target", CustomArgs.miniMessageArgument("value"),
            CompassVisualizer.PROP_TARGET))
        .then(subCommand("radius", CustomArgs.integer("value", 1), CompassVisualizer.PROP_RADIUS));
  }

  @Override
  public Map<String, Object> serialize(CompassVisualizer visualizer) {
    Map<String, Object> map = new LinkedHashMap<>();
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
