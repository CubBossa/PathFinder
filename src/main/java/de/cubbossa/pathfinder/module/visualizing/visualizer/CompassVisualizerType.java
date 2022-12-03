package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import org.bukkit.NamespacedKey;

public class CompassVisualizerType extends VisualizerType<CompassVisualizer> {

	public CompassVisualizerType(NamespacedKey key) {
		super(key);
	}

	@Override
	public CompassVisualizer create(NamespacedKey key, String nameFormat) {
		return new CompassVisualizer(key, nameFormat);
	}

	@Override
	public Message getInfoMessage(CompassVisualizer element) {
		return null;
	}

	@Override
	public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
		return null;
	}
}
