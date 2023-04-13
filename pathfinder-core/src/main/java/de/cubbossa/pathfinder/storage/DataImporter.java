package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.api.storage.StorageImplementation;
import java.io.IOException;

public interface DataImporter {

  void load(StorageImplementation storage) throws IOException;
}
