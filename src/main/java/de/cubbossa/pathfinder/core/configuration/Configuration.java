package de.cubbossa.pathfinder.core.configuration;

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

	// Config fields

	@ConfigValue(path = "lang.client-language", comments = """
			If messages should automatically be translated to client language, if a translation file
			for the provided client language exists.""")
	private boolean clientLanguage = false;
	@ConfigValue(path = "lang.fallback-language", comments = """
			The language that automatically will be used for players with unknown client locale.""")
	private String fallbackLanguage = "en_US";


	@ConfigValue(path = "data.general.type")
	private DatabaseType databaseType = DatabaseType.IN_MEMORY;

	@ConfigValue(path = "module.navigation.requires-location-discovery", comments = """
			Set this to true, if players have to discover nodegroups first to use the /find location <filter> command.
			If set to false, one can always navigate to every group, even if it hasn't been discovered first.""")
	private boolean findLocationRequiresDiscovery = true;

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
			cfg.setComments(meta.path(), Arrays.stream(meta.comments())
					.map(s -> s.split("\n"))
					.flatMap(Arrays::stream)
					.toList());
			cfg.set(meta.path(), field.getType().isEnum() ? field.get(this).toString().toLowerCase() : field.get(this));
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
