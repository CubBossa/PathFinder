package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.storage.implementation.YmlStorage;
import java.io.File;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class YamlStorageTest extends StorageTest {

  @SneakyThrows
  @Override
  StorageImplementation storage(NodeTypeRegistryImpl registry, ModifierRegistry modifierRegistry) {
    new File("./src/test/resources/data/").mkdir();
    YmlStorage implementation =
        new YmlStorage(new File("./src/test/resources/data/"), registry, modifierRegistry) {

          static boolean deleteDirectory(File path) {
            if (path.exists()) {
              File[] files = path.listFiles();
              for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                  deleteDirectory(files[i]);
                } else {
                  files[i].delete();
                }
              }
            }
            return (path.delete());
          }

          @Override
          public void init() throws Exception {
            new File("./src/test/resources/data/").mkdir();
            super.init();
          }

          @Override
          public void shutdown() {
            super.shutdown();
            deleteDirectory(new File("./src/test/resources/data/"));
          }
        };
    implementation.setLogger(Logger.getLogger("TESTS"));
    return implementation;
  }
}
