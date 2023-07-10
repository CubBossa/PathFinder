package de.cubbossa.pathfinder.storage.v3;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.storage.v3.tables.*;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.util.Collection;
import java.util.UUID;

import static de.cubbossa.pathfinder.storage.v3.tables.PathfinderDiscoverings.PATHFINDER_DISCOVERINGS;
import static de.cubbossa.pathfinder.storage.v3.tables.PathfinderEdges.PATHFINDER_EDGES;

@Deprecated(forRemoval = true)
public abstract class V3SqlStorage implements V3Storage {

  abstract ConnectionProvider getConnectionProvider();

  private DSLContext context;
  private final SQLDialect dialect;

  public V3SqlStorage(SQLDialect dialect) {
    this.dialect = dialect;
  }

  @Override
  public void connect() {

    System.setProperty("org.jooq.no-logo", "true");
    System.setProperty("org.jooq.no-tips", "true");

    context = DSL.using(getConnectionProvider(), dialect, new Settings()
        .withRenderQuotedNames(RenderQuotedNames.ALWAYS)
        .withRenderSchema(dialect != SQLDialect.SQLITE));
  }

  @Override
  public Collection<V3RoadMap> loadRoadmaps() {
    return context
        .selectFrom(PathfinderRoadmaps.PATHFINDER_ROADMAPS)
        .fetch(record -> new V3RoadMap(
            record.getKey(),
            record.getNameFormat(),
            NamespacedKey.fromString(record.getPathVisualizer()),
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
