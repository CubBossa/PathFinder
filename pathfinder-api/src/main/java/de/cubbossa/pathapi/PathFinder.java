package de.cubbossa.pathapi;

import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import java.util.logging.Logger;

public interface PathFinder {

  Logger getLogger();

  Storage getStorage();

  ExtensionsRegistry getExtensionRegistry();

  EventDispatcher getEventDispatcher();

  ModifierRegistry getModifierRegistry();

  VisualizerTypeRegistry getVisualizerTypeRegistry();
}
