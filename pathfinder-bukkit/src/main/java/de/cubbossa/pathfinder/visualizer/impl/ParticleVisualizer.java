package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.visualizer.BukkitParticlePlayer;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
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
  public BezierView createView(UpdatingPath nodes, PathPlayer<Player> player) {
    final BukkitParticlePlayer particlePlayer = new BukkitParticlePlayer(
        new ArrayList<>(), player.unwrap(), particle, particleData, amount, offset, speed
    );
    particlePlayer.setSteps(schedulerSteps);
    var view = new BezierView(player, nodes) {

      @Override
      public void update() {
        super.update();
        particlePlayer.setNewestPathAndConvert(getPoints());
      }

      @Override
      void play(int interval) {
        if (points == null) {
          return;
        }
        if (getViewers().isEmpty()) {
          return;
        }
        particlePlayer.run();
      }
    };
    PathFinderProvider.get().getDisposer().register(view, particlePlayer);
    return view;
  }
}
