package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.storage.implementation.SqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SqliteStorageTest extends StorageTest {

  @Override
  StorageImplementation storage(NodeTypeRegistry registry) {
    SqlStorage implementation = new SqliteStorage(new File("./src/test/resources/database.db"), registry) {
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
