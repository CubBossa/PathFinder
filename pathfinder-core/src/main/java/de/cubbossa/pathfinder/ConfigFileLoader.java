package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.storage.DatabaseType;
import de.cubbossa.pathfinder.util.Version;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.Serializer;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.function.BiConsumer;

@AllArgsConstructor
@RequiredArgsConstructor
public class ConfigFileLoader {

    private final File dataFolder;
    private final BiConsumer<String, Boolean> saveResource;
    private Version configRegenerationVersion = new Version("4.0.0");

    @Getter
    private boolean versionChange;
    @Getter
    private DatabaseType oldDatabaseType;

    public PathFinderConf loadConfig() {
        PathFinderConf configuration;

        File configFile = new File(dataFolder, "config.yml");
        YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
                .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
                .addSerializer(Locale.class, new Serializer<Locale, String>() {
                    @Override
                    public String serialize(Locale element) {
                return element.toLanguageTag();
              }

              @Override
              public Locale deserialize(String element) {
                return Locale.forLanguageTag(element.replace("_", "-"));
              }
            })
        .addSerializer(Color.class, new Serializer<Color, String>() {
          @Override
          public String serialize(Color element) {
            return Integer.toHexString(element.getRGB() & 0xffffff);
          }

          @Override
          public Color deserialize(String element) {
            return new Color(Integer.parseInt(element, 16));
          }
        })
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
      configuration = new PathFinderConf();
      YamlConfigurations.save(configFile.toPath(), PathFinderConf.class, configuration, properties);
      return configuration;
    }
    configuration = YamlConfigurations.load(configFile.toPath(), PathFinderConf.class, properties);

    if (new Version(configuration.version).compareTo(configRegenerationVersion) < 0) {
        this.versionChange = true;


        saveFileAsOld(configFile, "config", ".yml");
        saveFileAsOld(new File(dataFolder, "effects.nbo"), "effects", ".nbo");
        oldDatabaseType = configuration.database.type;

        configuration = loadConfig();
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
