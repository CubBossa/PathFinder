package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.api.node.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@Setter
public abstract class AdvancedParticleVisualizer<T extends AdvancedParticleVisualizer<T>>
    extends BezierPathVisualizer<T> {

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

  public AdvancedParticleVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
  }

  @Override
  public BezierData prepare(List<Node<?>> nodes, Player player) {
    BezierData bezierData = super.prepare(nodes, player);
    List<Location> points = new ArrayList<>();
    for (int i = 1; i < bezierData.points().size() - 1; i++) {
      Location previous = bezierData.points().get(i - 1);
      Location point = bezierData.points().get(i);
      Location next = bezierData.points().get(i + 1);

      Vector dir = next.toVector().subtract(previous.toVector()).normalize();
      Vector right = new Vector(0, 1, 0).crossProduct(dir).normalize();
      Vector up = dir.clone().crossProduct(right).normalize();

      Context c = new Context(player, point, 0, 0, i, bezierData.points().size());
      points.add(point.clone()
          .add(right.multiply(pathOffsetX.apply(c)))
          .add(up.multiply(pathOffsetY.apply(c)))
          .add(dir.multiply(pathOffsetZ.apply(c))));
    }
    return new BezierData(points);
  }

  @Override
  public void play(VisualizerContext<BezierData> context) {
    int step = context.interval() % schedulerSteps;
    for (int i = step; i < context.data().points().size(); i += schedulerSteps) {
      for (Player player : context.players()) {
        Location point = context.data().points().get(i);
        Context c =
            new Context(player, point, context.interval(), step, i, context.data().points().size());
        Particle p = particle.apply(c);
        Object data = particleData.apply(c);
        if (data == null || !p.getDataType().equals(data.getClass())) {
          player.spawnParticle(p, point, amount.apply(c), particleOffsetX.apply(c),
              particleOffsetY.apply(c), particleOffsetZ.apply(c), speed.apply(c), null);
        } else {
          player.spawnParticle(p, point, amount.apply(c), particleOffsetX.apply(c),
              particleOffsetY.apply(c), particleOffsetZ.apply(c), speed.apply(c), data);
        }
      }
    }
  }

  public record Context(Player player, Location point, int interval, int step, int index,
                        int count) {

  }


}
