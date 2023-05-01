package de.cubbossa.pathapi;

import de.cubbossa.pathapi.storage.DatabaseType;

import java.io.File;

public interface PathFinderConfig {

    LanguageConfig getLanguage();

    DatabaseConfig getDatabase();

    NavigationConfig getNavigation();

    ModuleConfig getModuleConfig();

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
        public boolean isClientLanguage();

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
        String getFallbackLanguage();
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

        NearestLocationSolverConfig getNearestLocationSolver();

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
        double getMaxDistance();
    }

    interface NearestLocationSolverConfig {
        /**
         * Define an algorithm to find the nearest node to a certain location.
         * SIMPLE: Finds the absolute nearest node.
         * RAYCAST: Sends multiple raycasts to find the nearest node that is not obstructed by walls.
         */
        String getAlgorithm();

        SimpleLocationWeightSolverConfig getSimpleConfig();

        RaycastLocationWeightSolverConfig getRaycastConfig();
    }

    interface SimpleLocationWeightSolverConfig {
        /**
         * Finds the closest n amount of nodes and connects them to a virtual node at the players position.
         * Default is 1, but it can also be increased to let the pathfinding algorithm find the shortest
         * of the n connections.
         */
        int getConnectionCount();
    }

    interface RaycastLocationWeightSolverConfig {
        /**
         * The algorithm finds the n nearest nodes and sends a raycast to each. Set the amount of
         * nodes. Default: 10
         */
        int getRaycastCount();

        /**
         * If nodes in the players view direction should be preferred.
         * 1 means that a node counts as 1 block closer to the player if it is in its view direction. Default: 1
         */
        double getStartLocationDirectionWeight();

        /**
         * If the node location direction should have an effect on its closeness to the player. Similar
         * to start-direction-weight but for nodes instead of player. Default: 0
         */
        double getScopeLocationDirectionWeight();

        /**
         * Each block between the player/a node and another node will count as the given amount of
         * distance in blocks. Default of 10.000 means that two blocks between a player and a node
         * will count as a distance of 20.000 blocks. While another node that is further away from the
         * player but not obstructed will have 0 extra weight and will therefore be prioritized.
         */
        double getBlockCollisionWeight();
    }

}
