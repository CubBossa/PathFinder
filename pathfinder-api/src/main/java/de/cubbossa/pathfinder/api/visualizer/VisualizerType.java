package de.cubbossa.pathfinder.api.visualizer;

import de.cubbossa.pathfinder.api.misc.Keyed;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;

import java.util.Map;

public interface VisualizerType<T extends PathVisualizer<T, ?>> extends Keyed {
	String getCommandName();

	T create(NamespacedKey key, String nameFormat);

	Message getInfoMessage(T element);

	ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex,
	                               int argumentOffset);

	void deserialize(T visualizer, Map<String, Object> values);

	Map<String, Object> serialize(T visualizer);

	de.cubbossa.pathfinder.api.storage.VisualizerDataStorage<T> getStorage();

	void setStorage(de.cubbossa.pathfinder.api.storage.VisualizerDataStorage<T> storage);
}
