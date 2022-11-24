package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import org.bukkit.NamespacedKey;

public class CompassVisualizerType extends VisualizerType<NodeLocationVisualizer> {

	public CompassVisualizerType(NamespacedKey key) {
		super(key);
	}

	@Override
	public NodeLocationVisualizer create(NamespacedKey key, String nameFormat) {
		return new NodeLocationVisualizer();
	}

	@Override
	public Message getInfoMessage(NodeLocationVisualizer element) {
		return null;
	}

	@Override
	public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
		return null;
	}
}
