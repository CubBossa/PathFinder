package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.VisualizerDataStorage;
import java.util.Map;

public interface VisualizerType<T extends PathVisualizer<T, ?, ?>> extends Keyed {
  String getCommandName();

  T create(NamespacedKey key, String nameFormat);

  void deserialize(T visualizer, Map<String, Object> values);

  Map<String, Object> serialize(T visualizer);

  VisualizerDataStorage<T> getStorage();

  void setStorage(VisualizerDataStorage<T> storage);
}
