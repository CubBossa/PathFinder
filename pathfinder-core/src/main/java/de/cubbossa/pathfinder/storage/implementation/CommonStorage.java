package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;
import de.cubbossa.pathfinder.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CommonStorage implements StorageImplementation, WaypointDataStorage, InternalVisualizerDataStorage {

  final NodeTypeRegistry nodeTypeRegistry;
  final VisualizerTypeRegistry visualizerTypeRegistry;
  final ModifierRegistry modifierRegistry;
  @Setter
  @Getter
  Storage storage;
  @Getter
  @Setter
  private @Nullable Logger logger;

  public CommonStorage(NodeTypeRegistry nodeTypeRegistry, VisualizerTypeRegistry visualizerTypeRegistry, ModifierRegistry modifierRegistry) {
    this.nodeTypeRegistry = nodeTypeRegistry;
    this.visualizerTypeRegistry = visualizerTypeRegistry;
    this.modifierRegistry = modifierRegistry;
  }

  protected void debug(String message) {
    if (logger != null) {
      logger.log(Level.INFO, message);
    }
  }

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> resolveOptVisualizerType(VisualizerT visualizer) {
    return resolveOptVisualizerType(visualizer.getKey());
  }

  <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerType<VisualizerT>> resolveOptVisualizerType(NamespacedKey key) {
    return storage.<VisualizerT>loadVisualizerType(key).join();
  }

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> resolveVisualizerType(VisualizerT visualizer) {
    return resolveVisualizerType(visualizer.getKey());
  }

  <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> resolveVisualizerType(NamespacedKey key) {
    return this.<VisualizerT>resolveOptVisualizerType(key).orElseThrow(() -> {
      return new IllegalStateException("Tried to create visualizer of type '" + key + "' but could not find registered type with this key.");
    });
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> VisualizerT createAndLoadVisualizer(VisualizerType<VisualizerT> type, NamespacedKey key) {
    String nameFormat = StringUtils.toDisplayNameFormat(key);
    VisualizerT visualizer = type.create(key, nameFormat);
    saveVisualizer(visualizer);
    return visualizer;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void deleteVisualizer(VisualizerT visualizer) {
    resolveVisualizerType(visualizer).getStorage().deleteVisualizer(visualizer);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void saveVisualizer(VisualizerT visualizer) {
    resolveVisualizerType(visualizer).getStorage().saveVisualizer(visualizer);
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Map<NamespacedKey, VisualizerT> loadVisualizers(VisualizerType<VisualizerT> type) {
    return type.getStorage().loadVisualizers();
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> Optional<VisualizerT> loadVisualizer(NamespacedKey key) {
    Optional<VisualizerType<VisualizerT>> opt = resolveOptVisualizerType(key);
    return opt
        .map(VisualizerType::getStorage)
        .map(s -> s.loadVisualizer(key))
        .filter(Optional::isPresent).map(Optional::get);
  }
}
