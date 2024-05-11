package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathfinder.group.ModifierRegistry;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.storage.InternalVisualizerStorageImplementation;
import de.cubbossa.pathfinder.storage.StorageImplementation;
import de.cubbossa.pathfinder.storage.WaypointStorageImplementation;
import de.cubbossa.pathfinder.storage.WorldLoader;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistry;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractStorage implements StorageImplementation, WaypointStorageImplementation, InternalVisualizerStorageImplementation {

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

  public AbstractStorage(NodeTypeRegistry nodeTypeRegistry, VisualizerTypeRegistry visualizerTypeRegistry, ModifierRegistry modifierRegistry) {
    this.nodeTypeRegistry = nodeTypeRegistry;
    this.visualizerTypeRegistry = visualizerTypeRegistry;
    this.modifierRegistry = modifierRegistry;
  }
}
