package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.node.NodeTypeRegistry;
import de.cubbossa.pathfinder.storage.implementation.SqlStorage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;

public class SqlStorageTest extends StorageTest {

  @Override
  StorageImplementation storage(NodeTypeRegistry registry) {
    SqlStorage implementation = new SqlStorage(SQLDialect.H2, nodeTypeRegistry) {
      @Override
      public ConnectionProvider getConnectionProvider() {
        final Connection connection;
        try {
          Class.forName("org.h2.jdbcx.JdbcDataSource");
          connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        } catch (SQLException | ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
        return new ConnectionProvider() {
          @Override
          public @Nullable Connection acquire() throws DataAccessException {
            return connection;
          }

          @Override
          public void release(Connection connection) throws DataAccessException {
          }
        };
      }

      @Override
      public void shutdown() {
        try {
          getConnectionProvider().acquire().prepareStatement("DROP ALL OBJECTS").execute();
          getConnectionProvider().acquire().close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
    implementation.setLogger(Logger.getLogger("TESTS"));
    return implementation;
  }
}
