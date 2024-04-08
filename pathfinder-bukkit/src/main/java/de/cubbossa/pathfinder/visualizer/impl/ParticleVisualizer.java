package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@Setter
public class ParticleVisualizer extends BezierPathVisualizer {

  public static final Property<ParticleVisualizer, Integer> PROP_SCHEDULER_STEPS =
      new PropertyImpl<>("particle-steps", Integer.class,
          ParticleVisualizer::getSchedulerSteps, ParticleVisualizer::setSchedulerSteps);
  public static final Property<ParticleVisualizer, Integer> PROP_AMOUNT =
      new PropertyImpl<>("amount", Integer.class,
          ParticleVisualizer::getAmount, ParticleVisualizer::setAmount);
  public static final Property<ParticleVisualizer, Vector> PROP_OFFSET =
      new PropertyImpl<>("offset", Vector.class,
          ParticleVisualizer::getOffset, ParticleVisualizer::setOffset);

  private int schedulerSteps = 50;
  private Particle particle = Particle.SCRAPE;
  private Object particleData = null;
  private float speed = .5f;
  private int amount = 1;
  private Vector offset = new Vector(0.02f, 0.02f, 0.02f);

  public ParticleVisualizer(NamespacedKey key) {
    super(key);
    PROP_INTERVAL.setValue(this, 1);
  }

  @Override
  public BezierView createView(List<Node> nodes, PathPlayer<Player> player) {
    return new BezierView(player, nodes.toArray(Node[]::new)) {

      private long lastException = 0;

      @Override
      void play(int interval) {
        if (points == null) {
          return;
        }
        for (int i = interval % getSchedulerSteps(); i < points.size();
             i += getSchedulerSteps()) {
          for (PathPlayer<Player> player : getViewers()) {
            Player bukkitPlayer = player.unwrap();
            Location point = points.get(i);
            var data = particleData;
            if (particle.getDataType().equals(Void.class)) {
              data = null;
            } else if (!Objects.equals(particleData.getClass(), particle.getDataType())) {
              if (System.currentTimeMillis() - lastException < 3000) {
                return;
              }
              lastException = System.currentTimeMillis();
              throw new IllegalStateException("Particle data is of wrong type - given: "
                  + particleData.getClass().getName() + ", required: " + particle.getDataType().getName());
            }
            bukkitPlayer.spawnParticle(particle, point, amount, offset.getX(), offset.getY(), offset.getZ(), speed, data);
          }
        }
      }
    };
  }
}
