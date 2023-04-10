package de.cubbossa.pathfinder.storage;

import java.io.IOException;

public interface DataImporter {

  void load(StorageImplementation storage) throws IOException;
}
