/*
 * This file is generated by jOOQ.
 */
package de.cubbossa.pathfinder.jooq;


import de.cubbossa.pathfinder.jooq.tables.*;
import de.cubbossa.pathfinder.jooq.tables.records.*;
import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in the
 * default schema.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Keys {

  // -------------------------------------------------------------------------
  // UNIQUE and PRIMARY KEY definitions
  // -------------------------------------------------------------------------

  public static final UniqueKey<PathfinderDiscoveringsRecord> PATHFINDER_DISCOVERINGS__PATHFINDER_DISCOVERINGS_PK = Internal.createUniqueKey(PathfinderDiscoverings.PATHFINDER_DISCOVERINGS, DSL.name("pathfinder_discoverings_PK"), new TableField[]{PathfinderDiscoverings.PATHFINDER_DISCOVERINGS.DISCOVER_KEY, PathfinderDiscoverings.PATHFINDER_DISCOVERINGS.PLAYER_ID}, true);
  public static final UniqueKey<PathfinderEdgesRecord> PATHFINDER_EDGES__PATHFINDER_EDGES_PK = Internal.createUniqueKey(PathfinderEdges.PATHFINDER_EDGES, DSL.name("pathfinder_edges_PK"), new TableField[]{PathfinderEdges.PATHFINDER_EDGES.START_ID, PathfinderEdges.PATHFINDER_EDGES.END_ID}, true);
  public static final UniqueKey<PathfinderGroupModifierRelationRecord> PATHFINDER_GROUP_MODIFIER_RELATION__PATHFINDER_GROUP_MODIFIER_RELATION_PK = Internal.createUniqueKey(PathfinderGroupModifierRelation.PATHFINDER_GROUP_MODIFIER_RELATION, DSL.name("pathfinder_group_modifier_relation_PK"), new TableField[]{PathfinderGroupModifierRelation.PATHFINDER_GROUP_MODIFIER_RELATION.GROUP_KEY, PathfinderGroupModifierRelation.PATHFINDER_GROUP_MODIFIER_RELATION.MODIFIER_KEY}, true);
  public static final UniqueKey<PathfinderNodeTypeRelationRecord> PATHFINDER_NODE_TYPE_RELATION__PATHFINDER_NODE_TYPE_RELATION_PK = Internal.createUniqueKey(PathfinderNodeTypeRelation.PATHFINDER_NODE_TYPE_RELATION, DSL.name("pathfinder_node_type_relation_PK"), new TableField[]{PathfinderNodeTypeRelation.PATHFINDER_NODE_TYPE_RELATION.NODE_ID, PathfinderNodeTypeRelation.PATHFINDER_NODE_TYPE_RELATION.NODE_TYPE}, true);
  public static final UniqueKey<PathfinderNodegroupNodesRecord> PATHFINDER_NODEGROUP_NODES__PATHFINDER_NODEGROUP_NODES_PK = Internal.createUniqueKey(PathfinderNodegroupNodes.PATHFINDER_NODEGROUP_NODES, DSL.name("pathfinder_nodegroup_nodes_PK"), new TableField[]{PathfinderNodegroupNodes.PATHFINDER_NODEGROUP_NODES.GROUP_KEY, PathfinderNodegroupNodes.PATHFINDER_NODEGROUP_NODES.NODE_ID}, true);
  public static final UniqueKey<PathfinderNodegroupsRecord> PATHFINDER_NODEGROUPS__PATHFINDER_NODEGROUPS__PK = Internal.createUniqueKey(PathfinderNodegroups.PATHFINDER_NODEGROUPS, DSL.name("pathfinder_nodegroups__PK"), new TableField[]{PathfinderNodegroups.PATHFINDER_NODEGROUPS.KEY}, true);
  public static final UniqueKey<PathfinderSearchTermsRecord> PATHFINDER_SEARCH_TERMS__PK_PATHFINDER_SEARCH_TERMS = Internal.createUniqueKey(PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS, DSL.name("pk_pathfinder_search_terms"), new TableField[]{PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS.GROUP_KEY, PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS.SEARCH_TERM}, true);
  public static final UniqueKey<PathfinderVisualizerRecord> PATHFINDER_VISUALIZER__PK_PATHFINDER_VISUALIZER = Internal.createUniqueKey(PathfinderVisualizer.PATHFINDER_VISUALIZER, DSL.name("pk_pathfinder_visualizer"), new TableField[]{PathfinderVisualizer.PATHFINDER_VISUALIZER.KEY}, true);
  public static final UniqueKey<PathfinderVisualizerTypeRelationRecord> PATHFINDER_VISUALIZER_TYPE_RELATION__PATHFINDER_VISUALIZER_TYPE_RELATION_PK = Internal.createUniqueKey(PathfinderVisualizerTypeRelation.PATHFINDER_VISUALIZER_TYPE_RELATION, DSL.name("pathfinder_visualizer_type_relation_PK"), new TableField[]{PathfinderVisualizerTypeRelation.PATHFINDER_VISUALIZER_TYPE_RELATION.VISUALIZER_KEY, PathfinderVisualizerTypeRelation.PATHFINDER_VISUALIZER_TYPE_RELATION.TYPE_KEY}, true);
  public static final UniqueKey<PathfinderWaypointsRecord> PATHFINDER_WAYPOINTS__PATHFINDER_WAYPOINTS_PK = Internal.createUniqueKey(PathfinderWaypoints.PATHFINDER_WAYPOINTS, DSL.name("pathfinder_waypoints_PK"), new TableField[]{PathfinderWaypoints.PATHFINDER_WAYPOINTS.ID}, true);

  // -------------------------------------------------------------------------
  // FOREIGN KEY definitions
  // -------------------------------------------------------------------------

  public static final ForeignKey<PathfinderSearchTermsRecord, PathfinderNodegroupsRecord> PATHFINDER_SEARCH_TERMS__FK_PATHFINDER_SEARCH_TERMS_PATHFINDER_NODEGROUPS__PK = Internal.createForeignKey(PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS, DSL.name("fk_pathfinder_search_terms_pathfinder_nodegroups__PK"), new TableField[]{PathfinderSearchTerms.PATHFINDER_SEARCH_TERMS.GROUP_KEY}, Keys.PATHFINDER_NODEGROUPS__PATHFINDER_NODEGROUPS__PK, new TableField[]{PathfinderNodegroups.PATHFINDER_NODEGROUPS.KEY}, true);
}
