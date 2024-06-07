package de.cubbossa.pathfinder

import de.cubbossa.pathfinder.storage.DatabaseType
import java.awt.Color
import java.io.File
import java.util.*

interface PathFinderConfig {

    val language: LanguageConfig
    val database: DatabaseConfig
    val navigation: NavigationConfig
    val moduleConfig: ModuleConfig
    val editMode: EditModeConfig

    enum class DistancePolicy {
        SMALLEST,
        LARGEST,
        NATURAL
    }

    interface LanguageConfig {
        /**
         * If client language is set to true plugin messages will appear in the according client
         * language. You need to add according language files if you want to use this feature.
         *
         *
         * See also: https://docs.leonardbausenwein.de/configuration/config.html#client-language
         */
        val isClientLanguage: Boolean

        /**
         * The fallback language serves as default language for all users. If the client language
         * setting is enabled but no file for a client language is present the fallback language
         * will be used instead.
         *
         *
         * The input value is a string that refers to the name of the language file in the
         * /pathfinder/lang/ directory.
         *
         *
         * See also: https://docs.leonardbausenwein.de/configuration/config.html#fallback-language
         */
        val fallbackLanguage: Locale
    }

    interface DatabaseConfig {
        /**
         * Choose a database type.
         * Valid types: IN_MEMORY, YAML, SQLITE, REMOTE_SQL
         *
         *
         *
         * See also: https://docs.leonardbausenwein.de/configuration/config.html#type
         */
        val type: DatabaseType

        /**
         * This should be true to immensely boost performance and can only be disabled for testing purposes.
         *
         * @return if data is being cached.
         */
        val isCaching: Boolean

        val embeddedSql: EmbeddedSqlStorageConfig

        val remoteSql: SqlStorageConfig
    }

    interface NavigationConfig {
        /**
         * Set this to true, if players have to discover nodegroups first to use the /find location
         * <filter> command. If set to false, one can always navigate to every group, even if it hasn't
         * been discovered first.
        </filter> */
        val isRequireDiscovery: Boolean

        val findLocation: FindLocationCommandConfig

        /**
         * This setting decides whether a player has to have all permissions of all groups of a node
         * or just one matching permission. True means all, so the permission query is linked by AND
         * operator. False means OR, so the player has to match only one permission node.
         */
        val isRequireAllGroupPermissions: Boolean

        /**
         * This setting decides whether all groups of a node have to be navigable to make it navigable
         * or just one. True means that all groups have to be navigable.
         */
        val isRequireAllGroupsNavigable: Boolean

        /**
         * This setting decides whether all groups of a node have to be discoverable to make it discoverable
         * or just one. True means that all groups have to be discoverable.
         */
        val isRequireAllGroupsDiscoverable: Boolean

        /**
         * This settings decides, which node group find distance applies to the node.
         * LARGEST: the group with largest find distance applies
         * SMALLEST: the group with smallest find distance applies
         * NATURAL: the first group applies. (later groups might be sortable with weights)
         */
        val distancePolicy: DistancePolicy
    }

    interface ModuleConfig {
        val isDiscoveryModule: Boolean

        val isNavigationModule: Boolean
    }

    interface EmbeddedSqlStorageConfig {
        val file: File
    }

    interface SqlStorageConfig {
        val dialect: String

        val jdbcUrl: String

        val username: String

        val password: String
    }

    interface FindLocationCommandConfig {
        /**
         * The command /findlocation <location> creates a virtual waypoint at the given location
         * and connects it with the nearest waypoint around. The maximum distance can be set to
         * not allow commands with locations far away from the actual roadmap. Default's set to 20.
         * -1 can be set to disable a distance check.
        </location> */
        val maxDistance: Float
    }

    interface SimpleLocationWeightSolverConfig {
        /**
         * Finds the closest n amount of nodes and connects them to a virtual node at the players position.
         * Default is 1, but it can also be increased to let the pathfinding algorithm find the shortest
         * of the n connections.
         */
        val connectionCount: Int
    }

    interface RaycastLocationWeightSolverConfig {
        /**
         * The algorithm finds the n nearest nodes and sends a raycast to each. Set the amount of
         * nodes. Default: 10
         */
        val raycastCount: Int

        /**
         * If nodes in the players view direction should be preferred.
         * 1 means that a node counts as 1 block closer to the player if it is in its view direction. Default: 1
         */
        val startLocationDirectionWeight: Float

        /**
         * If the node location direction should have an effect on its closeness to the player. Similar
         * to start-direction-weight but for nodes instead of player. Default: 0
         */
        val scopeLocationDirectionWeight: Float

        /**
         * Each block between the player/a node and another node will count as the given amount of
         * distance in blocks. Default of 10.000 means that two blocks between a player and a node
         * will count as a distance of 20.000 blocks. While another node that is further away from the
         * player but not obstructed will have 0 extra weight and will therefore be prioritized.
         */
        val blockCollisionWeight: Float
    }

    interface EditModeConfig {
        /**
         * If the edit mode should start with the edges tool set to directed edges or undirected edges.
         * A directed edge only goes one way while undirected edges go in both directions.
         */
        val isDirectedEdgesByDefault: Boolean

        /**
         * The spacing of the particles that are used to display edit mode edges. A spacing of 0.3 blocks is default.
         * Increase this value if you are having client side performance issues while in edit mode.
         */
        val edgeParticleSpacing: Float

        /**
         * The delay in ticks to wait before showing an edge particle again.
         * 1s = 20t
         */
        val edgeParticleTickDelay: Int

        /**
         * The edge color that indicates an outgoing or undirected edge.
         */
        val edgeParticleColorFrom: Color

        /**
         * The edge color that indicates an incoming edge.
         */
        val edgeParticleColorTo: Color

        /**
         * The distance up to which the player can see edit mode particles.
         * You may want to decrease this value if you run into client side performance issues.
         */
        val edgeParticleRenderDistance: Float

        /**
         * The distance up to which the player can see node armorstands.
         * You may want to decrease this value if you run into client side performance issues.
         */
        val nodeArmorStandRenderDistance: Float

        /**
         * The distance up to which the player can see edge armorstands.
         * You may want to decrease this value if you run into client side performance issues.
         */
        val edgeArmorStandRenderDistance: Float
    }
}
