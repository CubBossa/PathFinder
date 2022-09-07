package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.translations.Message;
import org.bukkit.NamespacedKey;

public class AdvancedParticleVisualizerType extends BezierVisualizerType<AdvancedParticleVisualizer> {

	public AdvancedParticleVisualizerType(NamespacedKey key) {
		super(key);
	}

	@Override
	public AdvancedParticleVisualizer create(NamespacedKey key, String nameFormat) {
		return new AdvancedParticleVisualizer(key, nameFormat);
	}

	@Override
	public Message getInfoMessage(AdvancedParticleVisualizer element) {
		return new Message("lol");
	}
}
