package de.cubbossa.pathfinder.storage.implementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.PathPluginConfig;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import org.jooq.ConnectionProvider;
import org.jooq.impl.DataSourceConnectionProvider;

public class RemoteSqlStorage extends SqlStorage {

  private final HikariDataSource dataSource;

  public RemoteSqlStorage(PathPluginConfig.SqlStorageConfig configuration,
                          NodeTypeRegistryImpl nodeTypeRegistry,
                          ModifierRegistry modifierRegistry,
                          VisualizerTypeRegistry visualizerTypeRegistry) {
    super(configuration.dialect, nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry);

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
