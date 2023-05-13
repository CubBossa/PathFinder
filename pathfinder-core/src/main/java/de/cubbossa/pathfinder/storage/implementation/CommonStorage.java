package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.storage.WorldLoader;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CommonStorage implements StorageImplementation, WaypointDataStorage, InternalVisualizerDataStorage {

  final NodeTypeRegistry nodeTypeRegistry;
  final VisualizerTypeRegistry visualizerTypeRegistry;
  final ModifierRegistry modifierRegistry;

  @Getter
  @Setter
  WorldLoader worldLoader = uuid -> {
    throw new IllegalStateException("No WorldLoader registered for storage " + getClass().getSimpleName());
  };
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
}
