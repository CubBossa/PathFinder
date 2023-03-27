package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.util.Version;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.util.function.BiConsumer;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class ConfigFileLoader {

  private final File dataFolder;
  private final BiConsumer<String, Boolean> saveResource;
  private Version configRegenerationVersion = new Version("3.0.0");

  public PathPluginConfig loadConfig() {
    PathPluginConfig configuration;

    File configFile = new File(dataFolder, "config.yml");
    YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
        .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
        .createParentDirectories(true)
        .header("""
            #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#
            #                                                               #
            #       _____      _   _     ______ _           _               #
            #      |  __ \\    | | | |   |  ____(_)         | |              #
            #      | |__) |_ _| |_| |__ | |__   _ _ __   __| | ___ _ __     #
            #      |  ___/ _` | __| '_ \\|  __| | | '_ \\ / _` |/ _ \\ '__|    #
            #      | |  | (_| | |_| | | | |    | | | | | (_| |  __/ |       #
            #      |_|   \\__,_|\\__|_| |_|_|    |_|_| |_|\\__,_|\\___|_|       #
            #                        Configuration                          #
            #                                                               #
            #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#
            
            """)
        .build();

    if (!configFile.exists()) {
      configuration = new PathPluginConfig();
      YamlConfigurations.save(configFile.toPath(), PathPluginConfig.class, configuration, properties);
      return configuration;
    }
    configuration = YamlConfigurations.load(configFile.toPath(), PathPluginConfig.class, properties);

    if (new Version(configuration.version).compareTo(configRegenerationVersion) < 0) {

      saveFileAsOld(configFile, "config", ".yml");
      saveFileAsOld(new File(dataFolder, "effects.nbo"), "effects", ".nbo");
      loadConfig();
      saveResource.accept("effects.nbo", true);
    }
    return configuration;
  }

  private void saveFileAsOld(File file, String base, String suffix) {
    int test = 1;
    String b = base + "_old";
    File f = new File(file.getParentFile(), b + suffix);
    while (f.exists()) {
      b = String.format("%s_old_%02d%s", base, test++, suffix);
      f = new File(file.getParentFile(), b);
    }
    file.renameTo(f);
  }
}
