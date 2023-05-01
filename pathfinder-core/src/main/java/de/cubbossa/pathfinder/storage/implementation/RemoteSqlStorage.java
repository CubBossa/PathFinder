package de.cubbossa.pathfinder.storage.implementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cubbossa.pathapi.PathFinderConfig;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;

public class RemoteSqlStorage extends SqlStorage {

  private final HikariDataSource dataSource;

    public RemoteSqlStorage(PathFinderConfig.SqlStorageConfig configuration,
                            NodeTypeRegistry nodeTypeRegistry,
                            ModifierRegistry modifierRegistry,
                            VisualizerTypeRegistry visualizerTypeRegistry) {
        super(SQLDialect.valueOf(configuration.getDialect()), nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry);

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
  public void shutdown() {
    dataSource.close();
  }

  @Override
  public ConnectionProvider getConnectionProvider() {
    return new DataSourceConnectionProvider(dataSource);
  }
}
