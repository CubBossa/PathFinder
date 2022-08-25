package de.cubbossa.pathfinder.data;

import lombok.Getter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteDatabase extends SqlDatabase {

	private final File file;
	@Getter
	private Connection connection;

	public SqliteDatabase(File file) {
		this.file = file;
	}

	public void connect() {
		try {
			String url = "jdbc:sqlite:" + file.getAbsolutePath();
			connection = DriverManager.getConnection(url);

		} catch (SQLException e) {
			throw new DataStorageException("Could not connect to Sqlite database.", e);
		}
		super.connect();
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
