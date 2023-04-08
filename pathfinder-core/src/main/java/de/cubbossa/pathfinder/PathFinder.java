package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.storage.Storage;
import java.util.logging.Logger;

public interface PathFinder {

  Logger getLogger();

  Storage getStorage();

  PathPluginConfig getConfiguration();

}
