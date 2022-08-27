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

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ParticleVisualizer extends Visualizer<ParticleVisualizer> {

	private int schedulerSteps = 10;
	private Particle particle = Particle.FLAME;
	private Object particleData = null;
	private float speed = 1;
	private int amount = 1;
	private Vector offset = new Vector(0.01f, 0.01f, 0.01f);
	private float pointDistance = .2f;
	private Map<Node, Double> curveLengths;

	@Override
	public VisualizerType<ParticleVisualizer> getType() {
		return VisualizerHandler.PARTICLE_VISUALIZER_TYPE;
	}

	public ParticleVisualizer(NamespacedKey key, String nameFormat) {
		super(key, nameFormat);
	}

	@Override
	public void play(List<Location> path, VisualizerContext context) {
		for (int i = context.interval() % getSchedulerSteps(); i < path.size(); i += getSchedulerSteps()) {
			for (Player player : context.players()) {
				player.spawnParticle(particle, path.get(i), amount, offset.getX(), offset.getY(), offset.getZ(), speed, particleData);
			}
		}
	}
}
