package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.storage.implementation.SqlStorage;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.SQLDialect;

import javax.sql.DataSource;
import java.util.logging.Logger;

public class CacheLayerTest extends StorageTest {

  public CacheLayerTest() {
    useCaches = true;
  }

  @Override
  StorageImplementation storage(NodeTypeRegistry registry, ModifierRegistry modifierRegistry,
                                VisualizerTypeRegistry visualizerTypeRegistry) {
    SqlStorage implementation =
        new SqlStorage(SQLDialect.H2, nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry) {
            @Override
            public DataSource getDataSource() {
                JdbcDataSource dataSource = new JdbcDataSource();
                dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
                return dataSource;
            }

            @Override
            public void shutdown() {
            }
        };
    implementation.setLogger(Logger.getLogger("TESTS"));
    return implementation;
  }
}
