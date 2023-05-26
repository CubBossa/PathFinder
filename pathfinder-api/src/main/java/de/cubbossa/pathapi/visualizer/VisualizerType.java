package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.VisualizerDataStorage;
import java.util.Map;

public interface VisualizerType<VisualizerT extends PathVisualizer<?, ?>> extends Keyed {
  String getCommandName();

  VisualizerT create(NamespacedKey key);

  void deserialize(VisualizerT visualizer, Map<String, Object> values);

  Map<String, Object> serialize(VisualizerT visualizer);

  VisualizerDataStorage<VisualizerT> getStorage();

  void setStorage(VisualizerDataStorage<VisualizerT> storage);
}
