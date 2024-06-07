package de.cubbossa.pathfinder;

import de.cubbossa.disposables.Disposable;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.Serializer;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.awt.Color;
import java.io.File;
import java.util.Locale;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ConfigFileLoader implements Disposable {

  private static final YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
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

  private final File dataFolder;

  public PathFinderConfigImpl loadConfig() {
    PathFinderConfigImpl configuration;

    File configFile = new File(dataFolder, "config.yml");
    if (!configFile.exists()) {
      configuration = new PathFinderConfigImpl();
      YamlConfigurations.save(configFile.toPath(), PathFinderConfigImpl.class, configuration, properties);
      return configuration;
    }
    return YamlConfigurations.load(configFile.toPath(), PathFinderConfigImpl.class, properties);
  }
}
