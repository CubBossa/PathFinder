package de.cubbossa.pathfinder.storage;

import java.io.File;

public class H2StorageTest extends StorageTest {

  @Override
  StorageImplementation storage(File dir) {
    return inMemoryStorage();
  }
}
