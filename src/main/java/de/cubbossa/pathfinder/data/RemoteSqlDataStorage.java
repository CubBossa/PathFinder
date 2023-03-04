package de.cubbossa.pathfinder.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cubbossa.pathfinder.core.configuration.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.impl.DataSourceConnectionProvider;

public class RemoteSqlDataStorage extends SqlDataStorage {

  private final HikariDataSource dataSource;

  public RemoteSqlDataStorage(Configuration configuration) {
    super(configuration.getDialect());

    HikariConfig config = new HikariConfig();
    config.setUsername(configuration.getUsername());
    config.setPassword(configuration.getPassword());
    config.setAutoCommit(false);
    config.setJdbcUrl(configuration.getJdbcUrl());
    config.setMaximumPoolSize(2);
    config.setMinimumIdle(1);
    dataSource = new HikariDataSource(config);
  }

  @Override
  public void disconnect() {
    dataSource.close();
  }

  @Override
  ConnectionProvider getConnectionProvider() {
    return new DataSourceConnectionProvider(dataSource);
  }
}
