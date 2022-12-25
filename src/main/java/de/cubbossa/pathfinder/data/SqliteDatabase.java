package de.cubbossa.pathfinder.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.Getter;

public class SqliteDatabase extends SqlDatabase {

  private final File file;
  @Getter
  private Connection connection;

  public SqliteDatabase(File file) {
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

    } catch (SQLException e) {
      throw new DataStorageException("Could not connect to Sqlite database.", e);
    }
    super.connect(initial);
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
}
