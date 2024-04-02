package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.storage.implementation.SqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import java.io.File;
import java.util.logging.Logger;

public class SqliteStorageTest extends StorageTest {

  @Override
  StorageImplementation storage(File dir) {
    SqlStorage implementation =
        new SqliteStorage(new File(dir, "temp_database.db"),
            nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry);
    implementation.setLogger(Logger.getLogger("TESTS"));
    return implementation;
  }
}
