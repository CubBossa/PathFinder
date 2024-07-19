package de.cubbossa.pathfinder;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.storage.DatabaseType;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import lombok.Getter;
import lombok.ToString;
import org.jooq.SQLDialect;

@Getter
@Configuration
@ToString
public class PathFinderConfigImpl implements PathFinderConfig {

  private final PathFinder pathFinder;

  public LanguageConfigImpl language = new LanguageConfigImpl();
  public DatabaseConfigImpl database = new DatabaseConfigImpl();
  public NavigationConfigImpl navigation = new NavigationConfigImpl();
  public ModuleConfigImpl moduleConfig = new ModuleConfigImpl();
  @Comment("""
      A set of commands for different game events. Use <player> for players name and use
      /execute as <player> run say hi
      to execute commands as player. Commands are executed from console, be careful with commands like
      /op <player>""")
  public EffectsConfigImpl effects = new EffectsConfigImpl();
  public EditModeConfigImpl editMode = new EditModeConfigImpl();
  @Comment("Don't change, specifies plugin version while generating config and helps to identify outdated files.")
  public String version = "-1";

  public PathFinderConfigImpl() {
    pathFinder = PathFinder.get();
    version = pathFinder.getVersion();
  }

  @Configuration
  @Getter
  @ToString
  public static class LanguageConfigImpl implements LanguageConfig {
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
                    
            Make sure to make locales either two letters ('en', 'de', 'au'), or a locale key pair ('en-US', 'de-DE').
            See also: https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html
            See also: https://docs.leonardbausenwein.de/configuration/config.html#fallback-language""")
    public Locale fallbackLanguage = Locale.ENGLISH;
  }

  @Configuration
  @ToString
  @Getter
  public static class DatabaseConfigImpl implements DatabaseConfig {
    @Comment("""
        Choose a database type.
        Valid types: IN_MEMORY, YAML, SQLITE, REMOTE_SQL
                
        See also: https://docs.leonardbausenwein.de/configuration/config.html#type""")
    public DatabaseType type = DatabaseType.SQLITE;
    @Comment("""
        This should be true to immensely boost performance and can only be disabled for testing purposes.""")
    public boolean caching = true;
    public EmbeddedSqlStorageConfigImpl embeddedSql = new EmbeddedSqlStorageConfigImpl();
    public SqlStorageConfigImpl remoteSql = new SqlStorageConfigImpl();
  }

  @Configuration
  @Getter
  @ToString
  public static class NavigationConfigImpl implements NavigationConfig {
    @Comment("""
        Set this to true, if players have to discover nodegroups first to use the /find location
        <filter> command. If set to false, one can always navigate to every group, even if it hasn't
        been discovered first.""")
    public boolean requireDiscovery = false;
    public FindLocationCommandConfigImpl findLocation = new FindLocationCommandConfigImpl();
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
  @ToString
  public static class ModuleConfigImpl implements ModuleConfig {
    public boolean discoveryModule = true;
    public boolean navigationModule = true;
  }

  @Configuration
  @Getter
  @ToString
  public static class EmbeddedSqlStorageConfigImpl implements EmbeddedSqlStorageConfig {
    public File file = new File(PathFinder.get().getDataFolder(), "data/database.db");
  }

  @Configuration
  @Getter
  @ToString
  public static class SqlStorageConfigImpl implements SqlStorageConfig {
    public String dialect = SQLDialect.MYSQL.getName();
    public String jdbcUrl = "jdbc:mysql://localhost/";
    public String username = "root";
    public String password = "KeepItSecretKeepItSafe";
  }

  @Configuration
  @Getter
  @ToString
  public static class FindLocationCommandConfigImpl implements FindLocationCommandConfig {
    @Comment("""
        The command /findlocation <location> creates a virtual waypoint at the given location
        and connects it with the nearest waypoint around. The maximum distance can be set to
        not allow commands with locations far away from the actual roadmap. Default's set to 20.
        -1 can be set to disable a distance check.""")
    public float maxDistance = 20.f;
  }

  // <player> <player-loc> <player-loc-x>
  @Configuration
  @ToString
  public static class EffectsConfigImpl {
    public ArrayList<String> onPathStart = Lists.newArrayList(
        "tellraw ${player} ${translation.commands.find.success.gson}"
    );
    public ArrayList<String> onPathTargetReach = Lists.newArrayList(
        "tellraw ${player} ${translation.general.target_reached.gson}"
    );
    public ArrayList<String> onPathStop;
    public ArrayList<String> onPathCancel = Lists.newArrayList(
        "tellraw ${player} ${translation.commands.cancel_path.gson}"
    );
    public ArrayList<String> onDiscover = Lists.newArrayList(
        "title ${player} subtitle ${translation.discovery.discover.json}",
        "title ${player} title {\"text\":\"\"}",
        "playsound minecraft:entity.villager.work_cartographer neutral ${player} ${player.location} 1 1"
    );
    public ArrayList<String> onDiscoverProgress = Lists.newArrayList(
        "title ${player} actionbar ${translation.discovery.progress.json}"
    );
    public ArrayList<String> onForget = Lists.newArrayList(
        "tellraw ${player} ${translation.discovery.forget.gson}"
    );
  }

  @Configuration
  @Getter
  @ToString
  public static class EditModeConfigImpl implements EditModeConfig {
    @Comment("""
        If the edit mode should start with the edges tool set to directed edges or undirected edges.
        A directed edge only goes one way while undirected edges go in both directions.""")
    public boolean directedEdgesByDefault = false;
    @Comment("""
        If the edit mode should start with the node tool set to chain mode. In chain mode, a new node
        will have a connection the the node that was created before. The new edge will be directed if
        the edge tool is set to directed edges.""")
    public boolean nodeChainModeByDefault = false;
    @Comment("""
        The spacing of the particles that are used to display edit mode edges. A spacing of 0.3 blocks is default.
        Increase this value if you are having client side performance issues while in edit mode.""")
    public float edgeParticleSpacing = .3f;
    @Comment("""
        The delay in ticks to wait before showing an edge particle again.
        1s = 20t""")
    public int edgeParticleTickDelay = 6;
    @Comment("The edge color that indicates an outgoing or undirected edge.")
    public Color edgeParticleColorFrom = new Color(0xff5500);
    @Comment("The edge color that indicates an incoming edge.")
    public Color edgeParticleColorTo = new Color(0x0088ff);
    @Comment("""
        The distance up to which the player can see edit mode particles.
        You may want to decrease this value if you run into client side performance issues.""")
    public float edgeParticleRenderDistance = 100f;
    @Comment("""
        The distance up to which the player can see node armorstands.
        You may want to decrease this value if you run into client side performance issues.""")
    public float nodeArmorStandRenderDistance = 50f;
    @Comment("""
        The distance up to which the player can see edge armorstands.
        You may want to decrease this value if you run into client side performance issues.""")
    public float edgeArmorStandRenderDistance = 25f;
  }
}
