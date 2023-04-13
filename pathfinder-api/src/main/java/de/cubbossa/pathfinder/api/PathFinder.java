package de.cubbossa.pathfinder.api;

import de.cubbossa.pathfinder.core.events.EventDispatcher;
import de.cubbossa.pathfinder.core.nodegroup.ModifierRegistry;
import de.cubbossa.pathfinder.storage.Storage;
import java.util.logging.Logger;
import org.jetbrains.annotations.ApiStatus;

public interface PathFinder {

  Logger getLogger();

  Storage getStorage();

  EventDispatcher getEventDispatcher();

  ModifierRegistry getModifierRegistry();
}
