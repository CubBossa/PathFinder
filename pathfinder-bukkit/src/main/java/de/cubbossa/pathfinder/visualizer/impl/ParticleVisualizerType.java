package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.ParticleArgument;
import dev.jorel.commandapi.wrappers.ParticleData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParticleVisualizerType extends BezierVisualizerType<ParticleVisualizer>
    implements VisualizerTypeCommandExtension, VisualizerTypeMessageExtension<ParticleVisualizer> {

  public ParticleVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public ParticleVisualizer create(NamespacedKey key) {
    return new ParticleVisualizer(key);
  }

  @Override
  public Message getInfoMessage(ParticleVisualizer element) {
    return Messages.CMD_VIS_INFO_PARTICLES.formatted(TagResolver.builder()
        .resolver(Placeholder.component("particle",
            Messages.formatParticle(element.getParticle(), element.getParticleData())))
        .resolver(
            Placeholder.component("particle-steps", Component.text(element.getSchedulerSteps())))
        .resolver(Placeholder.component("amount", Component.text(element.getAmount())))
        .resolver(Formatter.number("speed", element.getSpeed()))
        .resolver(Placeholder.component("offset",
            Messages.formatVector(BukkitVectorUtils.toInternal(element.getOffset()))))
        .resolver(Formatter.number("point-distance", element.getPointDistance()))
        .build());
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex,
                                       int argumentOffset) {
    return super.appendEditCommand(tree, visualizerIndex, argumentOffset)
        .then(CustomArgs.literal("particle")
            .then(new ParticleArgument("particle")
                .executes((commandSender, args) -> {
                  ParticleVisualizer visualizer = args.getUnchecked(visualizerIndex);
                  onSetParticle(commandSender, visualizer, args.getUnchecked(argumentOffset),
                      null, null, null);
                })
                .then(CustomArgs.integer("amount", 1)
                    .executes((commandSender, args) -> {
                      ParticleVisualizer visualizer = args.getUnchecked(visualizerIndex);
                      onSetParticle(commandSender, visualizer,
                          args.getUnchecked(argumentOffset),
                          args.getUnchecked(argumentOffset + 1), null, null);
                    })
                    .then(new FloatArgument("speed", 0)
                        .executes((commandSender, args) -> {
                          ParticleVisualizer visualizer =
                              args.getUnchecked(visualizerIndex);
                          onSetParticle(commandSender, visualizer,
                              args.getUnchecked(argumentOffset),
                              args.getUnchecked(argumentOffset + 1),
                              args.getUnchecked(argumentOffset + 2), null);
                        })
                        .then(CustomArgs.location("offset")
                            .executes((commandSender, args) -> {
                              ParticleVisualizer visualizer =
                                  args.getUnchecked(visualizerIndex);
                              onSetParticle(commandSender, visualizer,
                                  args.getUnchecked(argumentOffset),
                                  args.getUnchecked(argumentOffset + 1),
                                  args.getUnchecked(argumentOffset + 2),
                                  (args.<Location>getUnchecked(argumentOffset + 3)).toVector());
                            })
                        )
                    )
                )
            ))
        .then(subCommand("particle-steps", CustomArgs.integer("amount", 1),
            ParticleVisualizer.PROP_SCHEDULER_STEPS));
  }

  private <T> void onSetParticle(CommandSender sender, ParticleVisualizer visualizer,
                                 ParticleData<T> particle, @Nullable Integer amount,
                                 @Nullable Float speed, @Nullable Vector offset) {
    setProperty(BukkitUtils.wrap(sender), visualizer, particle.particle(), "particle", true,
        visualizer::getParticle, visualizer::setParticle);
    setProperty(BukkitUtils.wrap(sender), visualizer, particle.data(), "particle-data", true,
        visualizer::getParticleData, visualizer::setParticleData);
    if (amount != null) {
      setProperty(BukkitUtils.wrap(sender), visualizer, amount, "amount", true, visualizer::getAmount,
          visualizer::setAmount);
    }
    if (speed != null) {
      setProperty(BukkitUtils.wrap(sender), visualizer, speed, "speed", true, visualizer::getSpeed,
          visualizer::setSpeed);
    }
    if (offset != null) {
      setProperty(BukkitUtils.wrap(sender), visualizer, offset, "offset", true, visualizer::getOffset,
          visualizer::setOffset);
    }
  }

  @Override
  public Map<String, Object> serialize(ParticleVisualizer visualizer) {
    super.serialize(visualizer);
    Map<String, Object> map = new LinkedHashMap<>();
    map.put(ParticleVisualizer.PROP_SCHEDULER_STEPS.getKey(), visualizer.getSchedulerSteps());
    map.put("interval", visualizer.getInterval());
    map.put("particle", visualizer.getParticle().toString());
    map.put("particle-data", YamlUtils.wrap(visualizer.getParticleData()));
    map.put("speed", visualizer.getSpeed());
    map.put("amount", visualizer.getAmount());
    map.put("offset", visualizer.getOffset());
    map.put("sample-rate", visualizer.getBezierSamplingRate());
    map.put("point-distance", visualizer.getPointDistance());
    return map;
  }

  @Override
  public void deserialize(ParticleVisualizer visualizer, Map<String, Object> values) {
    super.deserialize(visualizer, values);
    loadProperty(values, visualizer, ParticleVisualizer.PROP_SCHEDULER_STEPS);
    if (values.containsKey("interval")) {
      visualizer.setInterval((Integer) values.get("interval"));
    }
    if (values.containsKey("particle")) {
      visualizer.setParticle(Particle.valueOf((String) values.get("particle")));
    }
    if (values.containsKey("particle-data")) {
      visualizer.setParticleData(YamlUtils.unwrap(values.get("particle-data")));
    }
    if (values.containsKey("speed")) {
      visualizer.setSpeed(((Double) values.get("speed")).floatValue());
    }
    if (values.containsKey("amount")) {
      visualizer.setAmount((Integer) values.get("amount"));
    }
    if (values.containsKey("offset")) {
      visualizer.setOffset((Vector) values.get("offset"));
    }
    if (values.containsKey("sample-rate")) {
      visualizer.setBezierSamplingRate((Integer) values.get("sample-rate"));
    }
    if (values.containsKey("point-distance")) {
      visualizer.setPointDistance(((Double) values.get("point-distance")).floatValue());
    }
  }
}