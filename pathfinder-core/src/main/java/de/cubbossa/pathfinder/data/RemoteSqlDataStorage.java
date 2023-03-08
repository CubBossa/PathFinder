package de.cubbossa.pathfinder.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cubbossa.pathfinder.PathPluginConfig;
import org.jooq.ConnectionProvider;
import org.jooq.impl.DataSourceConnectionProvider;

public class RemoteSqlDataStorage extends SqlDataStorage {

  private final HikariDataSource dataSource;

  public RemoteSqlDataStorage(PathPluginConfig.SqlStorageConfig configuration) {
    super(configuration.dialect);

    HikariConfig config = new HikariConfig();
    config.setUsername(configuration.username);
    config.setPassword(configuration.password);
    config.setAutoCommit(false);
    config.setJdbcUrl(configuration.jdbcUrl);
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
