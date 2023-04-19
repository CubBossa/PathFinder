package de.cubbossa.pathfinder.api;

import de.cubbossa.pathfinder.api.event.EventDispatcher;
import de.cubbossa.pathfinder.api.group.ModifierRegistry;
import de.cubbossa.pathfinder.api.storage.Storage;
import de.cubbossa.pathfinder.api.visualizer.VisualizerTypeRegistry;
import java.util.logging.Logger;

public interface PathFinder {

  Logger getLogger();

  Storage getStorage();

  ExtensionsRegistry getExtensionRegistry();

  EventDispatcher getEventDispatcher();

  ModifierRegistry getModifierRegistry();

  VisualizerTypeRegistry getVisualizerTypeRegistry();
}
