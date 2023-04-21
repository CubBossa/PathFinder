package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import lombok.Getter;
import lombok.Setter;
import de.cubbossa.pathapi.misc.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@Setter
public class ParticleVisualizer extends BezierPathVisualizer<ParticleVisualizer> {

  public static final Property<ParticleVisualizer, Integer> PROP_SCHEDULER_STEPS =
      new SimpleProperty<>("particle-steps", Integer.class, true,
          ParticleVisualizer::getSchedulerSteps, ParticleVisualizer::setSchedulerSteps);

  private int schedulerSteps = 50;
  private Particle particle = Particle.SCRAPE;
  private Object particleData = null;
  private float speed = .5f;
  private int amount = 1;
  private Vector offset = new Vector(0.02f, 0.02f, 0.02f);

  public ParticleVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
  }

  @Override
  public VisualizerType<ParticleVisualizer> getType() {
    return VisualizerHandler.PARTICLE_VISUALIZER_TYPE;
  }

  @Override
  public void play(VisualizerContext<BezierData, Player> context) {
    for (int i = context.interval() % getSchedulerSteps(); i < context.data().points().size();
         i += getSchedulerSteps()) {
      for (PathPlayer<Player> player : context.players()) {
        Player bukkitPlayer = player.unwrap();
        bukkitPlayer.spawnParticle(particle, context.data().points().get(i), amount, offset.getX(),
            offset.getY(), offset.getZ(), speed, particleData);
      }
    }
  }
}