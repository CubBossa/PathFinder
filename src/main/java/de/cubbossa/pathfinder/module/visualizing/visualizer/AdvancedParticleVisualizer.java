package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public class AdvancedParticleVisualizer extends BezierPathVisualizer<AdvancedParticleVisualizer> {

	public record Context(Player player, int interval, int step, int index, int count) {

	}

	private int schedulerSteps = 40;
	private Function<Context, Particle> particle = c -> Particle.DOLPHIN;
	private Function<Context, Object> particleData = c -> null;
	private Function<Context, Float> speed = c -> .05f;
	private Function<Context, Integer> amount = c -> 1;
	private Function<Context, Float> particleOffsetX = c -> 0.002f;
	private Function<Context, Float> particleOffsetY = c -> 0.002f;
	private Function<Context, Float> particleOffsetZ = c -> 0.002f;
	private Function<Context, Float> pathOffsetX = c -> 0f;
	private Function<Context, Float> pathOffsetY = c -> 0f;
	private Function<Context, Float> pathOffsetZ = c -> 0f;

	public AdvancedParticleVisualizer(NamespacedKey key, String nameFormat) {
		super(key, nameFormat);
	}

	@Override
	public VisualizerType<AdvancedParticleVisualizer> getType() {
		return VisualizerHandler.ADV_PARTICLE_VISUALIZER_TYPE;
	}

	@Override
	public BezierData prepare(List<Node> nodes) {
		BezierData bezierData = super.prepare(nodes);
		List<Location> points = new ArrayList<>();
		for (int i = 1; i < bezierData.points().size() - 1; i++) {
			Location previous = bezierData.points().get(i - 1);
			Location point = bezierData.points().get(i);
			Location next = bezierData.points().get(i + 1);

			Vector dir = next.toVector().subtract(previous.toVector()).normalize();
			Vector right = new Vector(0, 1, 0).crossProduct(dir).normalize();
			Vector up = dir.clone().crossProduct(right).normalize();

			Context c = new Context(null, 0, 0, i, bezierData.points().size());
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
				Context c = new Context(player, context.interval(), step, i, context.data().points().size());
				player.spawnParticle(particle.apply(c), context.data().points().get(i), amount.apply(c), particleOffsetX.apply(c),
						particleOffsetY.apply(c), particleOffsetZ.apply(c), speed.apply(c), particleData.apply(c));
			}
		}
	}
}
