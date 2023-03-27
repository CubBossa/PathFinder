package de.cubbossa.pathfinder.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;

public class SqlDataStorageTest extends DataStorageTest {

  @Override
  DataStorage storage() {
    return new SqlDataStorage(SQLDialect.H2, nodeTypeRegistry) {
      @Override
      ConnectionProvider getConnectionProvider() {
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
      public void disconnect() {
        try {
          getConnectionProvider().acquire().prepareStatement("DROP ALL OBJECTS").execute();
          getConnectionProvider().acquire().close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
}
