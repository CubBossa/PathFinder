package de.cubbossa.pathfinder.storage.implementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.cubbossa.pathfinder.PathFinderConfig;
import de.cubbossa.pathfinder.group.ModifierRegistry;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistry;
import javax.sql.DataSource;
import org.jooq.SQLDialect;

public class RemoteSqlStorage extends SqlStorage {

  private final HikariDataSource dataSource;

  public RemoteSqlStorage(PathFinderConfig.SqlStorageConfig configuration,
                          NodeTypeRegistry nodeTypeRegistry,
                          ModifierRegistry modifierRegistry,
                          VisualizerTypeRegistry visualizerTypeRegistry) {
    super(SQLDialect.valueOf(configuration.dialect), nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry);

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
  public DataSource getDataSource() {
    return dataSource;
  }
}
