package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public abstract class AdvancedParticleVisualizer extends BezierPathVisualizer {

  private int schedulerSteps = 40;
  private Function<Context, Particle> particle = c -> Particle.FLAME;
  private Function<Context, Object> particleData = c -> null;
  private Function<Context, Float> speed = c -> .0001f;
  private Function<Context, Integer> amount = c -> 1;
  private Function<Context, Float> particleOffsetX = c -> 0.002f;
  private Function<Context, Float> particleOffsetY = c -> 0.002f;
  private Function<Context, Float> particleOffsetZ = c -> 0.002f;
  private Function<Context, Float> pathOffsetX = c -> (float) Math.sin(c.index() / 2.) * .3f;
  private Function<Context, Float> pathOffsetY = c -> (float) Math.cos(c.index() / 2.) * .3f;
  private Function<Context, Float> pathOffsetZ = c -> 0f;

  public AdvancedParticleVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public BezierView createView(List<Node> nodes, PathPlayer<Player> player) {
    BezierView bezierView = new BezierView(player, nodes) {
      @Override
      void play(int interval) {
      }
    };
    List<Location> points = new ArrayList<>();
    for (int i = 1; i < bezierView.getPoints().size() - 1; i++) {
      Location previous = bezierView.getPoints().get(i - 1);
      Location point = bezierView.getPoints().get(i);
      Location next = bezierView.getPoints().get(i + 1);

      Vector dir = next.toVector().subtract(previous.toVector()).normalize();
      Vector right = new Vector(0, 1, 0).crossProduct(dir).normalize();
      Vector up = dir.clone().crossProduct(right).normalize();

      Context c = new Context(player, point, 0, 0, i, bezierView.getPoints().size());
      points.add(point.clone()
          .add(right.multiply(pathOffsetX.apply(c)))
          .add(up.multiply(pathOffsetY.apply(c)))
          .add(dir.multiply(pathOffsetZ.apply(c))));
    }
    return new BezierView(player, nodes, points) {
      @Override
      void play(int interval) {
        int step = interval % schedulerSteps;
        for (int i = step; i < points.size(); i += schedulerSteps) {
          for (PathPlayer<Player> player : getViewers()) {
            Player bukkitPlayer = player.unwrap();
            Location point = points.get(i);
            Context c = new Context(player, point, interval, step, i, points.size());
            Particle p = particle.apply(c);
            Object data = particleData.apply(c);
            if (data == null || !p.getDataType().equals(data.getClass())) {
              bukkitPlayer.spawnParticle(p, point, amount.apply(c), particleOffsetX.apply(c),
                  particleOffsetY.apply(c), particleOffsetZ.apply(c), speed.apply(c), null);
            } else {
              bukkitPlayer.spawnParticle(p, point, amount.apply(c), particleOffsetX.apply(c),
                  particleOffsetY.apply(c), particleOffsetZ.apply(c), speed.apply(c), data);
            }
          }
        }
      }
    };
  }

  public record Context(PathPlayer<Player> player, Location point, int interval, int step, int index, int count) {
  }
}
