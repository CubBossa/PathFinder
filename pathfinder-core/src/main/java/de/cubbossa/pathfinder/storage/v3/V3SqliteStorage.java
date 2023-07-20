package de.cubbossa.pathfinder.storage.v3;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.storage.DataStorageException;
import de.cubbossa.pathfinder.storage.v3.tables.*;
import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static de.cubbossa.pathfinder.storage.v3.tables.PathfinderDiscoverings.PATHFINDER_DISCOVERINGS;
import static de.cubbossa.pathfinder.storage.v3.tables.PathfinderEdges.PATHFINDER_EDGES;

@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "v5")
public class V3SqliteStorage implements V3Storage {

  private DSLContext context;
  private final SQLDialect dialect;
  private final File file;
  private Connection connection;

  public V3SqliteStorage(File file) {
    this.dialect = SQLDialect.SQLITE;
    this.file = file;
  }


  ConnectionProvider connectionProvider = new ConnectionProvider() {
    @Override
    public synchronized @Nullable Connection acquire() throws DataAccessException {
      if (connection != null) {
        return connection;
      }
      try {
        SQLiteConfig config = new SQLiteConfig();
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath(), config.toProperties());
        connection.setAutoCommit(false);

        return connection;
      } catch (ClassNotFoundException | SQLException e) {
        throw new DataStorageException("Could not connect to Sqlite database.", e);
      }
    }

    @SneakyThrows
    @Override
    public void release(Connection con) throws DataAccessException {
      con.commit();
    }
  };

  public ConnectionProvider getConnectionProvider() {
    return connectionProvider;
  }

  @SneakyThrows
  @Override
  public void connect() {

    if (!file.exists()) {
      throw new IllegalStateException("V3SqliteStorage requires existing file!");
    }
    try {
      ConnectionProvider fac = getConnectionProvider();
      Connection con = fac.acquire();
      con.prepareStatement("PRAGMA ignore_check_constraints = true;").execute();
      fac.release(con);

    } catch (SQLException e) {
      throw new DataStorageException("Could not connect to Sqlite database.", e);
    }

    System.setProperty("org.jooq.no-logo", "true");
    System.setProperty("org.jooq.no-tips", "true");

    context = DSL.using(getConnectionProvider(), dialect, new Settings()
        .withRenderQuotedNames(RenderQuotedNames.ALWAYS)
        .withRenderSchema(dialect != SQLDialect.SQLITE));
  }

  @Override
  public void disconnect() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        connection = null;
      }
    } catch (SQLException e) {
      throw new DataStorageException("Could not disconnect Sqlite database", e);
    }
  }

  @Override
  public Collection<V3RoadMap> loadRoadmaps() {
    return context
        .selectFrom(PathfinderRoadmaps.PATHFINDER_ROADMAPS)
        .fetch(record -> new V3RoadMap(
            record.getKey(),
            record.getNameFormat(),
            record.getPathVisualizer() == null ? null : NamespacedKey.fromString(record.getPathVisualizer()),
            record.getPathCurveLength()
        ));
  }

  @Override
  public Collection<V3Edge> loadEdges() {
    return context
        .selectFrom(PATHFINDER_EDGES)
        .fetch(record -> new V3Edge(
            record.getStartId(), record.getEndId(), record.getWeightModifier().floatValue()
        ));
  }

  @Override
  public Collection<V3Node> loadNodes() {
    return context
        .selectFrom(PathfinderNodes.PATHFINDER_NODES)
        .fetch(record -> new V3Node(
            record.getId(),
            record.getType(),
            record.getRoadmapKey(),
            record.getX(),
            record.getY(),
            record.getZ(),
            UUID.fromString(record.getWorld()),
            record.getPathCurveLength()
        ));
  }

  @Override
  public Collection<V3GroupNode> loadGroupNodes() {
    return context
        .selectFrom(PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES)
        .fetch(record -> new V3GroupNode(
            record.getNodeId(), record.getGroupKey()
        ));
  }

  @Override
  public Collection<V3NodeGroup> loadNodeGroups() {
    return context
        .selectFrom(PathfinderNodegroups.PATHFINDER_NODEGROUPS)
        .fetch(record -> new V3NodeGroup(
            record.getKey(),
            record.getNameFormat(),
            record.getPermission(),
            record.getNavigable(),
            record.getDiscoverable(),
            record.getFindDistance()
        ));
  }

  @Override
  public Collection<V3SearchTerm> loadSearchTerms() {
    return context
        .selectFrom(PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS)
        .fetch(record -> new V3SearchTerm(record.getGroupKey(), record.getSearchTerm()));
  }

  @Override
  public Collection<V3Discovering> loadDiscoverings() {
    return context
        .selectFrom(PATHFINDER_DISCOVERINGS)
        .fetch(record -> new V3Discovering(
            UUID.fromString(record.getPlayerId()), record.getDiscoverKey(), record.getDate())
        );
  }

  @Override
  public Collection<V3Visualizer> loadVisualizers() {
    return context
        .selectFrom(PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER)
        .fetch(record -> new V3Visualizer(
            record.getKey(),
            record.getType(),
            record.getNameFormat(),
            record.getPermission(),
            record.getInterval(),
            record.getData()
        ));
  }
}
