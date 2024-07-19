package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.storage.DatabaseType;
import java.awt.Color;
import java.io.File;
import java.util.Locale;

public interface PathFinderConfig {

  LanguageConfig getLanguage();

  DatabaseConfig getDatabase();

  NavigationConfig getNavigation();

  ModuleConfig getModuleConfig();

  EditModeConfig getEditMode();

  enum DistancePolicy {
    SMALLEST,
    LARGEST,
    NATURAL
  }

  interface LanguageConfig {
    /**
     * If client language is set to true plugin messages will appear in the according client
     * language. You need to add according language files if you want to use this feature.
     * <p>
     * See also: https://docs.leonardbausenwein.de/configuration/config.html#client-language
     */
    boolean isClientLanguage();

    /**
     * The fallback language serves as default language for all users. If the client language
     * setting is enabled but no file for a client language is present the fallback language
     * will be used instead.
     * <p>
     * The input value is a string that refers to the name of the language file in the
     * /pathfinder/lang/ directory.
     * <p>
     * See also: https://docs.leonardbausenwein.de/configuration/config.html#fallback-language
     */
    Locale getFallbackLanguage();
  }

  interface DatabaseConfig {

    /**
     * Choose a database type.
     * Valid types: IN_MEMORY, YAML, SQLITE, REMOTE_SQL
     *
     * <p>
     * See also: https://docs.leonardbausenwein.de/configuration/config.html#type
     */
    DatabaseType getType();

    /**
     * This should be true to immensely boost performance and can only be disabled for testing purposes.
     *
     * @return if data is being cached.
     */
    boolean isCaching();

    EmbeddedSqlStorageConfig getEmbeddedSql();

    SqlStorageConfig getRemoteSql();
  }

  interface NavigationConfig {
    /**
     * Set this to true, if players have to discover nodegroups first to use the /find location
     * <filter> command. If set to false, one can always navigate to every group, even if it hasn't
     * been discovered first.
     */
    boolean isRequireDiscovery();

    FindLocationCommandConfig getFindLocation();

    /**
     * This setting decides whether a player has to have all permissions of all groups of a node
     * or just one matching permission. True means all, so the permission query is linked by AND
     * operator. False means OR, so the player has to match only one permission node.
     */
    boolean isRequireAllGroupPermissions();

    /**
     * This setting decides whether all groups of a node have to be navigable to make it navigable
     * or just one. True means that all groups have to be navigable.
     */
    boolean isRequireAllGroupsNavigable();

    /**
     * This setting decides whether all groups of a node have to be discoverable to make it discoverable
     * or just one. True means that all groups have to be discoverable.
     */
    boolean isRequireAllGroupsDiscoverable();

    /**
     * This settings decides, which node group find distance applies to the node.
     * LARGEST: the group with largest find distance applies
     * SMALLEST: the group with smallest find distance applies
     * NATURAL: the first group applies. (later groups might be sortable with weights)
     */
    DistancePolicy getDistancePolicy();
  }

  interface ModuleConfig {
    boolean isDiscoveryModule();

    boolean isNavigationModule();
  }

  interface EmbeddedSqlStorageConfig {
    File getFile();
  }

  interface SqlStorageConfig {
    String getDialect();

    String getJdbcUrl();

    String getUsername();

    String getPassword();
  }

  interface FindLocationCommandConfig {
    /**
     * The command /findlocation <location> creates a virtual waypoint at the given location
     * and connects it with the nearest waypoint around. The maximum distance can be set to
     * not allow commands with locations far away from the actual roadmap. Default's set to 20.
     * -1 can be set to disable a distance check.
     */
    float getMaxDistance();
  }

  interface EditModeConfig {
    /**
     * If the edit mode should start with the edges tool set to directed edges or undirected edges.
     * A directed edge only goes one way while undirected edges go in both directions.
     */
    boolean isDirectedEdgesByDefault();

    /**
     * The spacing of the particles that are used to display edit mode edges. A spacing of 0.3 blocks is default.
     * Increase this value if you are having client side performance issues while in edit mode.
     */
    float getEdgeParticleSpacing();

    /**
     * The delay in ticks to wait before showing an edge particle again.
     * 1s = 20t
     */
    int getEdgeParticleTickDelay();

    /**
     * The edge color that indicates an outgoing or undirected edge.
     */
    Color getEdgeParticleColorFrom();

    /**
     * The edge color that indicates an incoming edge.
     */
    Color getEdgeParticleColorTo();

    /**
     * The distance up to which the player can see edit mode particles.
     * You may want to decrease this value if you run into client side performance issues.
     */
    float getEdgeParticleRenderDistance();

    /**
     * The distance up to which the player can see node armorstands.
     * You may want to decrease this value if you run into client side performance issues.
     */
    float getNodeArmorStandRenderDistance();

    /**
     * The distance up to which the player can see edge armorstands.
     * You may want to decrease this value if you run into client side performance issues.
     */
    float getEdgeArmorStandRenderDistance();
  }
}
