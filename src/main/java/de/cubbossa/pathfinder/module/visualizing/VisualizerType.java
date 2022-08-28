package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

@Getter
@Setter
public abstract class VisualizerType<T extends PathVisualizer<T>> implements Keyed {

	private final NamespacedKey key;

	public VisualizerType(NamespacedKey key) {
		this.key = key;
	}

	public String getCommandName() {
		return key.getKey();
	}

	public abstract Message getInfoMessage(T element);

	public abstract void appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset);
}
