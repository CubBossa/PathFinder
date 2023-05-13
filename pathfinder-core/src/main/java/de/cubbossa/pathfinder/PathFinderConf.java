package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderConfig;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.storage.DatabaseType;
import de.cubbossa.pathfinder.util.CommonLocationWeightSolverRegistry;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import org.jooq.SQLDialect;

import java.io.File;

@Getter
@Configuration
public class PathFinderConf implements PathFinderConfig {

  private final PathFinder pathFinder;

  public LanguageConf language = new LanguageConf();
  public DatabaseConf database = new DatabaseConf();
  public NavigationConf navigation = new NavigationConf();
  public ModuleConf moduleConfig = new ModuleConf();
  @Comment("Don't change, specifies plugin version while generating config and helps to identify outdated files.")
  public String version = "-1";

  public PathFinderConf() {
    pathFinder = PathFinderProvider.get();
    version = pathFinder.getVersion();
  }

  @Configuration
  @Getter
  public static class LanguageConf implements LanguageConfig {
    @Comment("""
        If client language is set to true plugin messages will appear in the according client
        language. You need to add according language files if you want to use this feature.
                
        See also: https://docs.leonardbausenwein.de/configuration/config.html#client-language""")
    public boolean clientLanguage = false;
    @Comment("""
        The fallback language serves as default language for all users. If the client language
        setting is enabled but no file for a client language is present the fallback language
        will be used instead.
                
        The input value is a string that refers to the name of the language file in the
        /pathfinder/lang/ directory.
                
        See also: https://docs.leonardbausenwein.de/configuration/config.html#fallback-language""")
    public String fallbackLanguage = "en_US";
  }

  @Configuration
  @Getter
  public static class DatabaseConf implements DatabaseConfig {
    @Comment("""
        Choose a database type.
        Valid types: IN_MEMORY, YAML, SQLITE, REMOTE_SQL
                
        See also: https://docs.leonardbausenwein.de/configuration/config.html#type""")
    public DatabaseType type = DatabaseType.SQLITE;
    public EmbeddedSqlStorageConf embeddedSql = new EmbeddedSqlStorageConf();
    public SqlStorageConf remoteSql = new SqlStorageConf();
  }

  @Configuration
  @Getter
  public static class NavigationConf implements NavigationConfig {
    @Comment("""
        Set this to true, if players have to discover nodegroups first to use the /find location
        <filter> command. If set to false, one can always navigate to every group, even if it hasn't
        been discovered first.""")
    public boolean requireDiscovery = false;
    public FindLocationCommandConf findLocation = new FindLocationCommandConf();
    public NearestLocationSolverConf nearestLocationSolver = new NearestLocationSolverConf();
    @Comment("""
        This setting decides whether a player has to have all permissions of all groups of a node
        or just one matching permission. True means all, so the permission query is linked by AND
        operator. False means OR, so the player has to match only one permission node.""")
    public boolean requireAllGroupPermissions = true;
    @Comment("""
        This setting decides whether all groups of a node have to be navigable to make it navigable
        or just one. True means that all groups have to be navigable.""")
    public boolean requireAllGroupsNavigable = true;
    @Comment("""
        This setting decides whether all groups of a node have to be discoverable to make it discoverable
        or just one. True means that all groups have to be discoverable.""")
    public boolean requireAllGroupsDiscoverable = true;
    @Comment("""
        This settings decides, which node group find distance applies to the node.
        LARGEST: the group with largest find distance applies
        SMALLEST: the group with smallest find distance applies
        NATURAL: the first group applies. (later groups might be sortable with weights)""")
    public DistancePolicy distancePolicy = DistancePolicy.LARGEST;
  }

  @Configuration
  @Getter
  public static class ModuleConf implements ModuleConfig {
    public boolean discoveryModule = true;
    public boolean navigationModule = true;
  }

  @Configuration
  @Getter
  public static class EmbeddedSqlStorageConf implements EmbeddedSqlStorageConfig {
    public File file = new File(PathFinderProvider.get().getDataFolder(), "data/database.db");
  }

  @Configuration
  @Getter
  public static class SqlStorageConf implements SqlStorageConfig {
    public String dialect = SQLDialect.MYSQL.getName();
    public String jdbcUrl = "jdbc:mysql://localhost/";
    public String username = "root";
    public String password = "KeepItSecretKeepItSafe";
  }

  @Configuration
  @Getter
  public static class FindLocationCommandConf implements FindLocationCommandConfig {
    @Comment("""
        The command /findlocation <location> creates a virtual waypoint at the given location
        and connects it with the nearest waypoint around. The maximum distance can be set to
        not allow commands with locations far away from the actual roadmap. Default's set to 20.
        -1 can be set to disable a distance check.""")
    public double maxDistance = 20.;
  }

  @Configuration
  @Getter
  public static class NearestLocationSolverConf implements NearestLocationSolverConfig {
    @Comment("""
        Define an algorithm to find the nearest node to a certain location.
        SIMPLE: Finds the absolute nearest node.
        RAYCAST: Sends multiple raycasts to find the nearest node that is not obstructed by walls.""")
    public String algorithm = CommonLocationWeightSolverRegistry.KEY_RAYCAST;
    public SimpleLocationWeightSolverConf simpleConfig = new SimpleLocationWeightSolverConf();
    public RaycastLocationWeightSolverConf raycastConfig = new RaycastLocationWeightSolverConf();
  }

  @Configuration
  @Getter
  public static class SimpleLocationWeightSolverConf implements SimpleLocationWeightSolverConfig {
    @Comment("""
        Finds the closest n amount of nodes and connects them to a virtual node at the players position.
        Default is 1, but it can also be increased to let the pathfinding algorithm find the shortest
        of the n connections.""")
    public int connectionCount = 1;
  }

  @Configuration
  @Getter
  public static class RaycastLocationWeightSolverConf implements RaycastLocationWeightSolverConfig {
    @Comment("""
        The algorithm finds the n nearest nodes and sends a raycast to each. Set the amount of
        nodes. Default: 10""")
    public int raycastCount = 10;
    @Comment("""
        If nodes in the players view direction should be preferred.
        1 means that a node counts as 1 block closer to the player if it is in its view direction. Default: 1""")
    public double startLocationDirectionWeight = 1;
    @Comment("""
        If the node location direction should have an effect on its closeness to the player. Similar
        to start-direction-weight but for nodes instead of player. Default: 0""")
    public double scopeLocationDirectionWeight = 0;
    @Comment("""
        Each block between the player/a node and another node will count as the given amount of
        distance in blocks. Default of 10.000 means that two blocks between a player and a node
        will count as a distance of 20.000 blocks. While another node that is further away from the
        player but not obstructed will have 0 extra weight and will therefore be prioritized.""")
    public double blockCollisionWeight = 10_000d;
  }
}
