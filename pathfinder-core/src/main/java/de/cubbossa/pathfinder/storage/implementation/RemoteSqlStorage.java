package de.cubbossa.pathfinder.storage.implementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.PathPluginConfig;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.jooq.ConnectionProvider;
import org.jooq.impl.DataSourceConnectionProvider;

public class RemoteSqlStorage extends SqlStorage {

  private final HikariDataSource dataSource;

  public RemoteSqlStorage(PathPluginConfig.SqlStorageConfig configuration,
                          NodeTypeRegistryImpl nodeTypeRegistry,
                          ModifierRegistry modifierRegistry) {
    super(configuration.dialect, nodeTypeRegistry, modifierRegistry);

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
