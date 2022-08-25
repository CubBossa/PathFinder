package de.bossascrew.pathfinder.module.visualizing.visualizer;

import de.bossascrew.pathfinder.core.node.Node;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ParticleVisualizer extends Visualizer {

	private int particleSteps = 10;
	/*	private ParticleBuilder particle = new ParticleBuilder(ParticleEffect.BLOCK_DUST)
				.setAmount(1)
				.setSpeed(0)
				.setColor(java.awt.Color.GREEN);*/
	private ParticleBuilder particle = new ParticleBuilder(ParticleEffect.FLAME).setAmount(1).setSpeed(0);
	private Double particleDistance = .2;
	private Map<Node, Double> curveLengths;

	public ParticleVisualizer(NamespacedKey key, String nameFormat) {
		super(key, nameFormat);
	}

	@Override
	public void play(List<Location> path, VisualizerContext context) {
		for (int i = context.interval() % getParticleSteps(); i < path.size(); i += getParticleSteps()) {
			getParticle().setLocation(path.get(i)).display(context.players());
		}
	}
}
