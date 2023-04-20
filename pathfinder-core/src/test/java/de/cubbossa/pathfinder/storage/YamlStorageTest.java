package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.storage.implementation.SqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.YmlStorage;
import lombok.SneakyThrows;

import java.io.File;
import java.util.logging.Logger;

public class YamlStorageTest extends StorageTest {

  @SneakyThrows
  @Override
  StorageImplementation storage(NodeTypeRegistry registry) {
    new File("./src/test/resources/data/").mkdir();
    YmlStorage implementation = new YmlStorage(new File("./src/test/resources/data/"), registry) {

      @Override
      public void init() throws Exception {
        new File("./src/test/resources/data/").mkdir();
        super.init();
      }

      @Override
      public void shutdown() {
        super.shutdown();
        new File("./src/test/resources/data/").delete();
      }
    };
    implementation.setLogger(Logger.getLogger("TESTS"));
    return implementation;
  }
}
