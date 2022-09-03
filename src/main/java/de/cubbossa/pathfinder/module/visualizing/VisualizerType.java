package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.Map;

@Getter
@Setter
public abstract class VisualizerType<T extends PathVisualizer<T, ?>> implements Keyed {

	private final NamespacedKey key;

	public VisualizerType(NamespacedKey key) {
		this.key = key;
	}

	public String getCommandName() {
		return key.getKey();
	}

	public abstract T create(NamespacedKey key, String nameFormat);

	public abstract Message getInfoMessage(T element);

	public abstract ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset);

	public Map<String, Object> serialize(T visualizer) {
		return null;
	}

	public void deserialize(T visualizer, Map<String, Object> values) {
	}
}
