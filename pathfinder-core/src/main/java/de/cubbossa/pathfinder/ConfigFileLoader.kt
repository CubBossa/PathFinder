package de.cubbossa.pathfinder

import de.cubbossa.disposables.Disposable
import de.exlll.configlib.NameFormatters
import de.exlll.configlib.Serializer
import de.exlll.configlib.YamlConfigurationProperties
import de.exlll.configlib.YamlConfigurations
import lombok.AllArgsConstructor
import java.awt.Color
import java.io.File
import java.util.*

class ConfigFileLoader(
    private val dataFolder: File? = null
) : Disposable {

    fun loadConfig(): PathFinderConfigImpl {
        val configuration: PathFinderConfigImpl

        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            configuration = PathFinderConfigImpl()
            YamlConfigurations.save(
                configFile.toPath(),
                PathFinderConfigImpl::class.java,
                configuration,
                properties
            )
            return configuration
        }
        return YamlConfigurations.load(
            configFile.toPath(),
            PathFinderConfigImpl::class.java,
            properties
        )
    }

    companion object {
        private val properties: YamlConfigurationProperties =
            YamlConfigurationProperties.newBuilder()
                .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
                .addSerializer(Locale::class.java, object : Serializer<Locale, String> {
                    override fun serialize(element: Locale): String {
                        return element.toLanguageTag()
                    }

                    override fun deserialize(element: String): Locale {
                        return Locale.forLanguageTag(element.replace("_", "-"))
                    }
                })
                .addSerializer(Color::class.java, object : Serializer<Color, String> {
                    override fun serialize(element: Color): String {
                        return Integer.toHexString(element.rgb and 0xffffff)
                    }

                    override fun deserialize(element: String): Color {
                        return Color(element.toInt(16))
                    }
                })
                .createParentDirectories(true)
                .header(
                    """
          #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#
          #                                                               #
          #       _____      _   _     ______ _           _               #
          #      |  __ \    | | | |   |  ____(_)         | |              #
          #      | |__) |_ _| |_| |__ | |__   _ _ __   __| | ___ _ __     #
          #      |  ___/ _` | __| '_ \|  __| | | '_ \ / _` |/ _ \ '__|    #
          #      | |  | (_| | |_| | | | |    | | | | | (_| |  __/ |       #
          #      |_|   \__,_|\__|_| |_|_|    |_|_| |_|\__,_|\___|_|       #
          #                        Configuration                          #
          #                                                               #
          #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#
                      
          
                      
          """.trimIndent()
                )
                .build()
    }
}
