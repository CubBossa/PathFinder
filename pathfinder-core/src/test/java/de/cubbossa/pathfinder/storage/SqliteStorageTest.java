package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.storage.implementation.SqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import java.io.File;
import java.util.logging.Logger;

public class SqliteStorageTest extends StorageTest {

  @Override
  StorageImplementation storage(NodeTypeRegistry registry, ModifierRegistry modifierRegistry,
                                VisualizerTypeRegistry visualizerTypeRegistry) {
    SqlStorage implementation =
        new SqliteStorage(new File("./src/test/resources/database.db"), registry,
            modifierRegistry, visualizerTypeRegistry) {
          @Override
          public void shutdown() {
            super.shutdown();
            new File("./src/test/resources/database.db").delete();
          }
        };
    implementation.setLogger(Logger.getLogger("TESTS"));
    return implementation;
  }
}
