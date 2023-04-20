package de.cubbossa.pathfinder.storage.implementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cubbossa.pathfinder.PathPluginConfig;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import org.jooq.ConnectionProvider;
import org.jooq.impl.DataSourceConnectionProvider;

public class RemoteSqlStorage extends SqlStorage {

  private final HikariDataSource dataSource;

  public RemoteSqlStorage(PathPluginConfig.SqlStorageConfig configuration, NodeTypeRegistry nodeTypeRegistry) {
    super(configuration.dialect, nodeTypeRegistry);

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
  public void shutdown() {
    dataSource.close();
  }

  @Override
  public ConnectionProvider getConnectionProvider() {
    return new DataSourceConnectionProvider(dataSource);
  }
}
