package de.cubbossa.pathfinder.visualizer.impl;


import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.VisualizerTypeCommandExtension;
import de.cubbossa.pathfinder.command.VisualizerTypeMessageExtension;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.pathfinder.util.YamlUtils;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.ParticleArgument;
import dev.jorel.commandapi.wrappers.ParticleData;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.pf4j.Extension;

@Extension(points = VisualizerType.class)
public class ParticleVisualizerType extends BezierVisualizerType<ParticleVisualizer>
    implements VisualizerTypeCommandExtension, VisualizerTypeMessageExtension<ParticleVisualizer> {

  public ParticleVisualizerType() {
    super(AbstractPathFinder.pathfinder("particle"));
  }

  @Override
  public ParticleVisualizer createVisualizerInstance(NamespacedKey key) {
    return new ParticleVisualizer(key);
  }

  @Override
  public Message getInfoMessage(ParticleVisualizer element) {
    return Messages.CMD_VIS_INFO_PARTICLES.formatted(
        Messages.formatter().particle("particle", element.getParticle(), element.getParticleData()),
        Messages.formatter().number("particle-steps", element.getSchedulerSteps()),
        Messages.formatter().number("sampling-rate", element.getBezierSamplingRate()),
        Messages.formatter().number("interval", element.getInterval()),
        Messages.formatter().number("amount", element.getAmount()),
        Messages.formatter().number("speed", element.getSpeed()),
        Messages.formatter().vector("offset", BukkitVectorUtils.toInternal(element.getOffset())),
        Messages.formatter().number("point-distance", element.getPointDistance())
    );
  }

  @Override
  public Argument<?> appendEditCommand(Argument<?> tree, int visualizerIndex, int argumentOffset) {
    return super.appendEditCommand(tree, visualizerIndex, argumentOffset)
        .then(Arguments.literal("particle")
            .then(new ParticleArgument("particle")
                .executes((commandSender, args) -> {
                  ParticleVisualizer visualizer = args.getUnchecked(visualizerIndex);
                  onSetParticle(commandSender, visualizer, args.getUnchecked(argumentOffset),
                      null, null, null);
                })
                .then(Arguments.integer("amount", 1)
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
                        .then(Arguments.location("offset")
                            .executes((commandSender, args) -> {
                              ParticleVisualizer visualizer =
                                  args.getUnchecked(visualizerIndex);
                              onSetParticle(commandSender, visualizer,
                                  args.getUnchecked(argumentOffset),
                                  args.getUnchecked(argumentOffset + 1),
                                  args.getUnchecked(argumentOffset + 2),
                                  (BukkitVectorUtils.toBukkit(args.getUnchecked(argumentOffset + 3))).toVector());
                            })
                        )
                    )
                )
            ))
        .then(subCommand("particle-steps", Arguments.integer("amount", 1),
            ParticleVisualizer.PROP_SCHEDULER_STEPS));
  }

  private <T> void onSetParticle(CommandSender sender, ParticleVisualizer visualizer,
                                 ParticleData<T> particle, @Nullable Integer amount,
                                 @Nullable Float speed, @Nullable Vector offset) {
    setProperty(BukkitUtils.wrap(sender), visualizer, particle.particle(), "particle",
        visualizer::getParticle, visualizer::setParticle);
    setProperty(BukkitUtils.wrap(sender), visualizer, particle.data(), "particle-data",
        visualizer::getParticleData, visualizer::setParticleData);
    if (amount != null) {
      setProperty(BukkitUtils.wrap(sender), visualizer, amount, "amount", visualizer::getAmount,
          visualizer::setAmount);
    }
    if (speed != null) {
      setProperty(BukkitUtils.wrap(sender), visualizer, speed, "speed", visualizer::getSpeed,
          visualizer::setSpeed);
    }
    if (offset != null) {
      setProperty(BukkitUtils.wrap(sender), visualizer, offset, "offset", visualizer::getOffset,
          visualizer::setOffset);
    }
  }

  @Override
  public Map<String, Object> serialize(ParticleVisualizer visualizer) {
    Map<String, Object> map = super.serialize(visualizer);
    map.put(ParticleVisualizer.PROP_SCHEDULER_STEPS.getKey(), visualizer.getSchedulerSteps());
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
    loadProperty(values, visualizer, ParticleVisualizer.PROP_AMOUNT);
    loadProperty(values, visualizer, ParticleVisualizer.PROP_OFFSET);
    // TODO all
    if (values.containsKey("particle")) {
      visualizer.setParticle(Particle.valueOf((String) values.get("particle")));
    }
    if (values.containsKey("particle-data")) {
      visualizer.setParticleData(YamlUtils.unwrap(values.get("particle-data")));
    }
    if (values.containsKey("speed")) {
      visualizer.setSpeed(((Double) values.get("speed")).floatValue());
    }
    if (values.containsKey("sample-rate")) {
      visualizer.setBezierSamplingRate((Integer) values.get("sample-rate"));
    }
    if (values.containsKey("point-distance")) {
      visualizer.setPointDistance(((Double) values.get("point-distance")).floatValue());
    }
  }
}