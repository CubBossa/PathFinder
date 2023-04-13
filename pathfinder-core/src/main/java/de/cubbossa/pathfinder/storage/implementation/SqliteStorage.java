package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.storage.DataStorageException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;

public class SqliteStorage extends SqlStorage {

  private final File file;
  private Connection connection;

  public SqliteStorage(File file, NodeTypeRegistry nodeTypeRegistry) {
    super(SQLDialect.SQLITE, nodeTypeRegistry);
    this.file = file;

    PathPlugin.getInstance().getLogger().info("Setting up SQLITE database: " + file.getAbsolutePath());
  }

  public void init() throws Exception {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    try {
      String url = "jdbc:sqlite:" + file.getAbsolutePath();
      connection = DriverManager.getConnection(url);
      connection.setAutoCommit(false);
      super.init();

    } catch (SQLException e) {
      throw new DataStorageException("Could not connect to Sqlite database.", e);
    }
  }

  public void shutdown() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      throw new DataStorageException("Could not disconnect Sqlite database", e);
    }
  }

  public ConnectionProvider getConnectionProvider() {
    return new ConnectionProvider() {
      @Override
      public @Nullable Connection acquire() throws DataAccessException {
        try {
          connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
          return connection;
        } catch (SQLException e) {
          throw new DataStorageException("Could not connect to Sqlite database.", e);
        }
      }

      @Override
      public void release(Connection connection) throws DataAccessException {
        try {
          connection.close();
        } catch (SQLException e) {
          throw new DataStorageException("Could not close Sqlite database.", e);
        }
      }
    };

  }
}
