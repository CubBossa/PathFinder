package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;

public class CompassVisualizerType extends VisualizerType<CompassVisualizer> {

  public CompassVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public CompassVisualizer create(NamespacedKey key, String nameFormat) {
    return new CompassVisualizer(key, nameFormat);
  }

  @Override
  public Message getInfoMessage(CompassVisualizer element) {
    return Messages.CMD_VIS_COMPASS_INFO.format(TagResolver.builder()
        .resolver(Placeholder.unparsed("color", element.getColor().toString().toLowerCase()))
        .resolver(Placeholder.unparsed("overlay", element.getOverlay().toString().toLowerCase()))
        .resolver(Placeholder.parsed("background", element.getBackgroundFormat()))
        .resolver(Placeholder.parsed("marker-north", element.getNorth()))
        .resolver(Placeholder.parsed("marker-east", element.getEast()))
        .resolver(Placeholder.parsed("marker-south", element.getSouth()))
        .resolver(Placeholder.parsed("marker-west", element.getWest()))
        .resolver(Placeholder.parsed("marker-target", element.getTarget()))
        .resolver(
            Placeholder.unparsed(CompassVisualizer.PROP_RADIUS.getKey(), element.getRadius() + ""))
        .build()
    );
  }

  @Override
  public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex,
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
        .then(subCommand("radius", new IntegerArgument("value", 1), CompassVisualizer.PROP_RADIUS));
  }

  @Override
  public Map<String, Object> serialize(CompassVisualizer visualizer) {
    return new LinkedHashMapBuilder<String, Object>()
        .put(BossBarVisualizer.PROP_COLOR.getKey(), visualizer.getColor().toString())
        .put(BossBarVisualizer.PROP_OVERLAY.getKey(), visualizer.getOverlay().toString())
        .put(CompassVisualizer.PROP_BACKGROUND.getKey(), visualizer.getBackgroundFormat())
        .put(CompassVisualizer.PROP_NORTH.getKey(), visualizer.getNorth())
        .put(CompassVisualizer.PROP_EAST.getKey(), visualizer.getEast())
        .put(CompassVisualizer.PROP_SOUTH.getKey(), visualizer.getSouth())
        .put(CompassVisualizer.PROP_WEST.getKey(), visualizer.getWest())
        .put(CompassVisualizer.PROP_TARGET.getKey(), visualizer.getTarget())
        .put(CompassVisualizer.PROP_RADIUS.getKey(), visualizer.getRadius())
        .build();
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
