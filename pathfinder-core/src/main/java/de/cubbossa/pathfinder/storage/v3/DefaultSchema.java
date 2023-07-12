/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.storage.v3;


import de.cubbossa.pathfinder.jooq.tables.*;
import de.cubbossa.pathfinder.storage.v3.tables.PathfinderNodegroupsNodes;
import de.cubbossa.pathfinder.storage.v3.tables.PathfinderNodes;
import de.cubbossa.pathfinder.storage.v3.tables.PathfinderPathVisualizer;
import de.cubbossa.pathfinder.storage.v3.tables.PathfinderRoadmaps;
import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class DefaultSchema extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>DEFAULT_SCHEMA</code>
     */
    public static final DefaultSchema DEFAULT_SCHEMA = new DefaultSchema();

    /**
     * The table <code>pathfinder_discoverings</code>.
     */
    public final PathfinderDiscoverings PATHFINDER_DISCOVERINGS = PathfinderDiscoverings.PATHFINDER_DISCOVERINGS;

    /**
     * The table <code>pathfinder_edges</code>.
     */
    public final PathfinderEdges PATHFINDER_EDGES = PathfinderEdges.PATHFINDER_EDGES;

    /**
     * The table <code>pathfinder_nodegroups</code>.
     */
    public final PathfinderNodegroups PATHFINDER_NODEGROUPS = PathfinderNodegroups.PATHFINDER_NODEGROUPS;

    /**
     * The table <code>pathfinder_nodegroups_nodes</code>.
     */
    public final PathfinderNodegroupsNodes PATHFINDER_NODEGROUPS_NODES = PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES;

    /**
     * The table <code>pathfinder_nodes</code>.
     */
    public final PathfinderNodes PATHFINDER_NODES = PathfinderNodes.PATHFINDER_NODES;

    /**
     * The table <code>pathfinder_path_visualizer</code>.
     */
    public final PathfinderPathVisualizer PATHFINDER_PATH_VISUALIZER = PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER;

    /**
     * The table <code>pathfinder_roadmaps</code>.
     */
    public final PathfinderRoadmaps PATHFINDER_ROADMAPS = PathfinderRoadmaps.PATHFINDER_ROADMAPS;

    /**
     * The table <code>pathfinder_search_terms</code>.
     */
    public final PathfinderSearchTerms PATHFINDER_SEARCH_TERMS = PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS;

    /**
     * The table <code>sqlite_sequence</code>.
     */
    public final SqliteSequence SQLITE_SEQUENCE = SqliteSequence.SQLITE_SEQUENCE;

    /**
     * No further instances allowed
     */
    private DefaultSchema() {
        super("", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            PathfinderDiscoverings.PATHFINDER_DISCOVERINGS,
            PathfinderEdges.PATHFINDER_EDGES,
            PathfinderNodegroups.PATHFINDER_NODEGROUPS,
            PathfinderNodegroupsNodes.PATHFINDER_NODEGROUPS_NODES,
            PathfinderNodes.PATHFINDER_NODES,
            PathfinderPathVisualizer.PATHFINDER_PATH_VISUALIZER,
            PathfinderRoadmaps.PATHFINDER_ROADMAPS,
            PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS,
            SqliteSequence.SQLITE_SEQUENCE
        );
    }
}
