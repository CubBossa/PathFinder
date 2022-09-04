package de.cubbossa.pathfinder.core.configuration;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.data.DatabaseType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class Configuration {

	public enum NodeGroupPolicy {
		NATURAL_ORDER, SMALLEST_VALUE, LARGEST_VALUE
	}

	// Config fields

	@ConfigValue(path = "development.verbose")
	private boolean verbose = false;

	@ConfigValue(path = "development.test_environment")
	private boolean testing = false;

	@ConfigValue(path = "lang.client-language", comments = """
			If messages should automatically be translated to client language, if a translation file
			for the provided client language exists.""")
	private boolean clientLanguage = false;
	@ConfigValue(path = "lang.fallback-language", comments = """
			The language that automatically will be used for players with unknown client locale.""")
	private String fallbackLanguage = "en_US";


	@ConfigValue(path = "data.general.type", comments = """
			Set the database type to either SQLITE, YML, SQL or IN_MEMORY
			!!! Up to the first full version of the plugin, only the SQLITE option
			is guaranteed to work !!!""")
	private DatabaseType databaseType = DatabaseType.SQLITE;

	@ConfigValue(path = "nodegroups.policies.permission", comments = """
			If one node has multiple node groups, SMALLEST_VALUE will make
			one missing permission dominant, LARGEST_VALUE will require the
			player to lack permissions for all groups. (AND and OR, in terms of logic operators)""")
	private NodeGroupPolicy permissionPolicy = NodeGroupPolicy.SMALLEST_VALUE;
	@ConfigValue(path = "nodegroups.policies.navigable")
	private NodeGroupPolicy navigablePolicy = NodeGroupPolicy.SMALLEST_VALUE;
	@ConfigValue(path = "nodegroups.policies.discoverable")
	private NodeGroupPolicy discoverablePolicy = NodeGroupPolicy.SMALLEST_VALUE;
	@ConfigValue(path = "nodegroups.policies.find-distance")
	private NodeGroupPolicy findDistancePolicy = NodeGroupPolicy.LARGEST_VALUE;

	@ConfigValue(path = "module.navigation.enabled", comments = """
			Allows players to use the /find command to navigate to certain points of interest.
			Make sure to setup nodegroups, the find command relies on the search terms of nodegroups to work.""")
	private boolean navigationEnabled = true;

	@ConfigValue(path = "module.discovery.enabled", comments = """
			Allows players to discover nodegroups if the according groups have the feature enabled.
			This will display an effect that can be modified in effects.nbo.""")
	private boolean discoveryEnabled = true;

	@ConfigValue(path = "module.navigation.requires-location-discovery", comments = """
			Set this to true, if players have to discover nodegroups first to use the /find location <filter> command.
			If set to false, one can always navigate to every group, even if it hasn't been discovered first.""")
	private boolean findLocationRequiresDiscovery = true;

	@ConfigValue(path = "version", comments = """
			Just don't change this, it helps to convert your data to newer database types automatically when updating""")
	private String versionString = PathPlugin.getInstance().getDescription().getVersion();

	// Load and save

	public void saveToFile(File file) throws IOException, IllegalAccessException {

		if (!file.exists()) {
			if (!file.getParentFile().mkdirs() && !file.createNewFile()) {
				throw new RuntimeException("Unexpected error while saving config file.");
			}
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		List<Field> fields = Arrays.stream(Configuration.class.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(ConfigValue.class))
				.toList();

		for (Field field : fields) {
			ConfigValue meta = field.getAnnotation(ConfigValue.class);
			cfg.set(meta.path(), field.getType().isEnum() ? field.get(this).toString().toLowerCase() : field.get(this));
			List<String> comments = Arrays.stream(meta.comments())
					.map(s -> s.split("\n"))
					.flatMap(Arrays::stream)
					.toList();
			if (!comments.isEmpty() && !(comments.size() == 1 && comments.get(0).equals(""))) {
				cfg.setComments(meta.path(), comments);
			}
		}

		cfg.save(file);
	}

	public static Configuration loadFromFile(File file) throws IllegalAccessException, IOException {
		if (!file.exists()) {
			Configuration configuration = new Configuration();
			configuration.saveToFile(file);
			return configuration;
		}

		Configuration configuration = new Configuration();
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		List<Field> fields = Arrays.stream(Configuration.class.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(ConfigValue.class))
				.toList();

		for (Field field : fields) {
			ConfigValue meta = field.getAnnotation(ConfigValue.class);
			if (cfg.isSet(meta.path())) {
				field.set(configuration, field.getType().isEnum() ? Enum.valueOf((Class) field.getType(), cfg.get(meta.path()).toString().toUpperCase()) : cfg.get(meta.path()));
			}
		}

		return configuration;
	}
}
