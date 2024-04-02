package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.storage.StorageImplementation;
import java.io.File;

public class CacheLayerTest extends StorageTest {

  public CacheLayerTest() {
    useCaches = true;
  }

  @Override
  StorageImplementation storage(File dir) {
    return inMemoryStorage();
  }
}
