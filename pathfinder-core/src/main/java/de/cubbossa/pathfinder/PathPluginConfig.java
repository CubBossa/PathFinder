package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.data.DatabaseType;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import java.io.File;
import org.jooq.SQLDialect;

@Configuration
public class PathPluginConfig {

  public LanguageConfig language = new LanguageConfig();
  public DatabaseConfig database = new DatabaseConfig();
  public NavigationConfig navigation = new NavigationConfig();
  public ModuleConfig moduleConfig = new ModuleConfig();
  public SystemConfig system = new SystemConfig();

  @Configuration
  public static class LanguageConfig {
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
  public static class DatabaseConfig {
    @Comment("""
        Choose a database type.
        Valid types: IN_MEMORY, YAML, SQLITE, REMOTE_SQL
        
        See also: https://docs.leonardbausenwein.de/configuration/config.html#type""")
    public DatabaseType type = DatabaseType.YML;
    public EmbeddedSqlStorageConfig embeddedSql = new EmbeddedSqlStorageConfig();
    public SqlStorageConfig remoteSql = new SqlStorageConfig();
  }

  @Configuration
  public static class NavigationConfig {
    @Comment("""
      Set this to true, if players have to discover nodegroups first to use the /find location
      <filter> command. If set to false, one can always navigate to every group, even if it hasn't
      been discovered first.""")
    public boolean requireDiscovery = false;

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
  public static class ModuleConfig {
    public boolean discoveryModule = true;
    public boolean navigationModule = true;
  }

  @Configuration
  public static class EmbeddedSqlStorageConfig {
    public File file = new File(PathPlugin.getInstance().getDataFolder(), "data/database.db");
  }

  @Configuration
  public static class SqlStorageConfig {
    public SQLDialect dialect = SQLDialect.MYSQL;
    public String jdbcUrl = "jdbc:mysql://localhost/";
    public String username = "root";
    public String password = "KeepItSecretKeepItSafe";
  }

  @Configuration
  public static class SystemConfig {
    public String version = PathPlugin.getInstance().getDescription().getVersion();
  }

  public enum DistancePolicy {
    SMALLEST,
    LARGEST,
    NATURAL
  }
}
