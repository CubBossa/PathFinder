package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.events.EventDispatcher;
import de.cubbossa.pathfinder.core.nodegroup.ModifierRegistry;
import de.cubbossa.pathfinder.storage.Storage;
import java.util.logging.Logger;
import org.jetbrains.annotations.ApiStatus;

public interface PathFinder {

  Logger getLogger();

  Storage getStorage();

  EventDispatcher getEventDispatcher();

  PathPluginConfig getConfiguration();

  ModifierRegistry getModifierRegistry();

}
