package de.cubbossa.pathfinder.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;

public class SqliteDataStorage extends SqlDataStorage {

  private final File file;
  @Getter
  private Connection connection;

  public SqliteDataStorage(File file) {
    super(SQLDialect.SQLITE);
    this.file = file;
  }

  public void connect(Runnable initial) throws IOException {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();

      initial.run();
    }
    try {
      String url = "jdbc:sqlite:" + file.getAbsolutePath();
      connection = DriverManager.getConnection(url);
      connection.setAutoCommit(false);
      super.connect(initial);

    } catch (SQLException e) {
      throw new DataStorageException("Could not connect to Sqlite database.", e);
    }
  }

  public void disconnect() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      throw new DataStorageException("Could not disconnect Sqlite database", e);
    }
  }

  public Connection getConnection() {
    try {
      connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
      return connection;
    } catch (SQLException e) {
      throw new DataStorageException("Could not connect to Sqlite database.", e);
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
