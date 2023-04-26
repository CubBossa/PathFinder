package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.storage.DataStorageException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;

public class SqliteStorage extends SqlStorage {

  private final File file;
  private Connection connection;

  public SqliteStorage(File file, NodeTypeRegistryImpl nodeTypeRegistry,
                       ModifierRegistry modifierRegistry) {
    super(SQLDialect.SQLITE, nodeTypeRegistry, modifierRegistry);
    this.file = file;
  }

  public void init() throws Exception {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    try {
      super.init();

    } catch (SQLException e) {
      throw new DataStorageException("Could not connect to Sqlite database.", e);
    }
  }

  public void shutdown() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        connection = null;
      }
    } catch (SQLException e) {
      throw new DataStorageException("Could not disconnect Sqlite database", e);
    }
  }

  public ConnectionProvider getConnectionProvider() {
    return new ConnectionProvider() {
      @Override
      public synchronized @Nullable Connection acquire() throws DataAccessException {
        if (connection != null) {
          return connection;
        }
        try {
          Class.forName("org.sqlite.JDBC");
          connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
          connection.setAutoCommit(false);
          return connection;
        } catch (ClassNotFoundException | SQLException e) {
          throw new DataStorageException("Could not connect to Sqlite database.", e);
        }
      }

      @SneakyThrows
      @Override
      public void release(Connection con) throws DataAccessException {
        con.commit();
      }
    };

  }
}
