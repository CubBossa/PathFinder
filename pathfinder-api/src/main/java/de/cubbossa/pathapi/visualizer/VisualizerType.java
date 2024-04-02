package de.cubbossa.pathapi.visualizer;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.VisualizerStorageImplementation;
import java.util.Map;

public interface VisualizerType<VisualizerT extends PathVisualizer<?, ?>> extends Keyed {

  String getCommandName();

  VisualizerT create(NamespacedKey key);

  void deserialize(VisualizerT visualizer, Map<String, Object> values);

  Map<String, Object> serialize(VisualizerT visualizer);

  VisualizerStorageImplementation<VisualizerT> getStorage();

  void setStorage(VisualizerStorageImplementation<VisualizerT> storage);
}
