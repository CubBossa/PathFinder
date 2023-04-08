import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.ForcedType

plugins {
    antlr
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("io.freefair.lombok") version "6.6.2"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("nu.studer.jooq") version "8.1"
}

val minecraftVersion = project.property("minecraft_version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    // Antlr
    antlr("org.antlr:antlr4:4.12.0") { isTransitive = true }

    // Adventure
    api("net.kyori:adventure-api:4.12.0")
    api("net.kyori:adventure-platform-bukkit:4.1.2")
    api("net.kyori:adventure-text-minimessage:4.12.0")
    api("net.kyori:adventure-text-serializer-plain:4.12.0")

    // Configuration
    api("de.cubbossa:NBO-Core:1.0")
    api("com.github.Exlll.ConfigLib:configlib-yaml:v4.2.0")
    runtimeOnly("org.snakeyaml:snakeyaml-engine:2.3")

    // Commands
    api("de.cubbossa:commandapi-shade:8.7.5")

    // Other
    implementation("org.jooq:jooq:3.17.8")
    jooqGenerator("org.xerial:sqlite-jdbc:3.41.0.0")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("io.r2dbc:r2dbc-spi:1.0.0.RELEASE")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Particles
    implementation("de.cubbossa:splinelib:1.0")
    api("de.cubbossa:SerializedEffects:1.0")

    // Plugins
    runtimeOnly(project(":pathfinder-editmode"))
    runtimeOnly(project(":pathfinder-scripted-visualizer"))
    implementation(project(":pathfinder-graph"))

    // Statistics
    implementation("org.bstats:bstats-bukkit:3.0.1")

    // Spigot
    compileOnlyApi("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnlyApi("com.mojang:brigadier:1.0.18")
    testImplementation("com.mojang:brigadier:1.0.18")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.19:2.145.0")
    testImplementation("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    // UI
    api("de.cubbossa:Translations:1.1")

    implementation(files("generated/plugin-yml/Bukkit/plugin.yml"))
}

bukkit {
    name = "PathFinder"
    version = rootProject.version.toString()
    description = null
    website = "https://docs.leonardbausenwein.de"
    author = "CubBossa"
    authors = listOf("LooFifteen")

    main = "de.cubbossa.pathfinder.PathPlugin"

    val versionSplit = minecraftVersion.split(".")
    apiVersion = minecraftVersion[0] + "." + 13.coerceAtLeast(versionSplit[1].toInt())

    softDepend = listOf("PlaceholderAPI")

    defaultPermission = BukkitPluginDescription.Permission.Default.OP
    permissions {
        register("pathfinder.command.pathfinder.info")
        register("pathfinder.command.pathfinder.help")
        register("pathfinder.command.pathfinder.reload")
        register("pathfinder.command.pathfinder.import")
        register("pathfinder.command.pathfinder.export")
        register("pathfinder.command.find")
        register("pathfinder.command.findlocation")
        register("pathfinder.command.cancel_path")
        register("pathfinder.command.roadmap.info")
        register("pathfinder.command.roadmap.create")
        register("pathfinder.command.roadmap.delete")
        register("pathfinder.command.roadmap.editmode")
        register("pathfinder.command.roadmap.list")
        register("pathfinder.command.roadmap.forcefind")
        register("pathfinder.command.roadmap.forceforget")
        register("pathfinder.command.roadmap.set_visualizer")
        register("pathfinder.command.roadmap.set_name")
        register("pathfinder.command.roadmap.set_curvelength")
        register("pathfinder.command.nodegroup.list")
        register("pathfinder.command.nodegroup.create")
        register("pathfinder.command.nodegroup.delete")
        register("pathfinder.command.nodegroup.set_name")
        register("pathfinder.command.nodegroup.set_findable")
        register("pathfinder.command.nodegroup.searchterms.list")
        register("pathfinder.command.nodegroup.searchterms.add")
        register("pathfinder.command.nodegroup.searchterms.remove")
        register("pathfinder.command.waypoint.info")
        register("pathfinder.command.waypoint.list")
        register("pathfinder.command.waypoint.create")
        register("pathfinder.command.waypoint.delete")
        register("pathfinder.command.waypoint.tp")
        register("pathfinder.command.waypoint.tphere")
        register("pathfinder.command.waypoint.connect")
        register("pathfinder.command.waypoint.disconnect")
        register("pathfinder.command.waypoint.set_curve_length")
        register("pathfinder.command.waypoint.add_group")
        register("pathfinder.command.waypoint.remove_group")
        register("pathfinder.command.waypoint.clear_groups")
        register("pathfinder.command.visualizer.list")
        register("pathfinder.command.visualizer.create")
        register("pathfinder.command.visualizer.delete")
        register("pathfinder.command.visualizer.info")
        register("pathfinder.command.visualizer.set_name")
        register("pathfinder.command.visualizer.set_permission")
        register("pathfinder.command.visualizer.set_interval")
        register("pathfinder.command.visualizer.edit")
        register("pathfinder.admin") {
            children = listOf(
                "pathfinder.command.pathfinder.*",
                "pathfinder.command.roadmap.*",
                "pathfinder.command.nodegroup.*",
                "pathfinder.command.waypoint.*",
                "pathfinder.command.visualizer.*"
            )
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    generateGrammarSource {
        // Tell ANTLR to generate visitor classes
        arguments.plusAssign("-visitor")
    }
    processResources {
        // Replace tokens in plugin.yml
        filter(
            org.apache.tools.ant.filters.ReplaceTokens::class,
            "tokens" to mapOf(
                "version" to project.version.toString(),
                "name" to rootProject.name
            )
        )
    }
    runServer {
        minecraftVersion(minecraftVersion)
    }
    shadowJar {

        archiveFileName.set("PathFinder-${parent?.version}.jar")
        mergeServiceFiles()

        // "whitelist" approach, only include transitive dependencies that are truly necessary.
        // otherwise jar grows from ~8mb to ~30mb
        dependencies {
            include(project(":pathfinder-graph"))
            include(project(":pathfinder-editmode"))
            include(project(":pathfinder-scripted-visualizer"))
            include(dependency("net.kyori:.*"))
            include(dependency("org.bstats:bstats-bukkit:.*"))
            include(dependency("de.cubbossa:MenuFramework:.*"))
            include(dependency("de.cubbossa:Translations:.*"))
            include(dependency("de.cubbossa:splinelib:.*"))
            include(dependency("de.cubbossa:NBO-Core:.*"))
            include(dependency("de.cubbossa:SerializedEffects:.*"))
            include(dependency("org.bstats:.*"))
            include(dependency("xyz.xenondevs:particle:.*"))
            include(dependency("de.cubbossa:commandapi-shade:.*"))
            include(dependency("org.openjdk.nashorn:nashorn-core:.*"))
            include(dependency("org.ow2.asm:asm:.*"))
            include(dependency("org.ow2.asm:asm-util:.*"))
            include(dependency("de.tr7zw:item-nbt-api-plugin:.*"))
            include(dependency("org.antlr:antlr4-runtime:.*"))
            include(dependency("com.github.Exlll.ConfigLib:configlib-yaml:.*"))
            include(dependency("com.github.Exlll.ConfigLib:configlib-core:.*"))
            include(dependency("org.snakeyaml:snakeyaml-engine:.*"))
            include(dependency("org.jooq:jooq:.*"))
            include(dependency("org.reactivestreams:reactive-streams:.*"))
            include(dependency("io.r2dbc:r2dbc-spi:.*"))
            include(dependency("com.zaxxer:HikariCP:.*"))
        }

        fun relocate(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to", null)
        }

        relocate("com.zaxxer.hikari", "hikari")
        relocate("org.bstats", "bstats")
        relocate("de.cubbossa.serializedeffects", "serializedeffects")
        relocate("de.cubbossa.nbo", "nbo")
        relocate("net.kyori", "kyori")
        relocate("de.cubbossa.menuframework", "gui")
        relocate("de.cubbossa.translations", "translations")
        relocate("de.cubbossa.splinelib", "splinelib")
        relocate("xyz.xenondevs.particle", "particle")
        relocate("dev.jorel.commandapi", "commandapi")
        relocate("de.tr7zw.changeme.nbtapi", "nbtapi")
        relocate("org.antlr", "antlr")
        relocate("org.jooq", "jooq")
        relocate("de.exlll", "exlll")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

sourceSets {
    main {
        // Include ANTLR generated sources
        java.srcDirs += file("build/generated-src/antlr/main")
        resources {
            exclude("*.db")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "Nexus"
            url = uri("https://nexus.leonardbausenwein.de/repository/maven-public/")
            credentials {
                username = "admin"
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
    publications.create<MavenPublication>("maven") {
        groupId = project.group.toString()
        artifactId = rootProject.name
        version = project.version.toString()

        from(components["java"])
    }
}

jooq {
    version.set("3.17.8")
    edition.set(JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    url = "jdbc:sqlite:src/main/resources/database_template.db"
                }
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database.apply {
                        name = "org.jooq.meta.sqlite.SQLiteDatabase"
                        forcedTypes = listOf(
                            ForcedType().apply {
                                userType = "org.bukkit.NamespacedKey"
                                converter = "de.cubbossa.pathfinder.storage.misc.NamespacedKeyConverter"
                                includeExpression = ".*key|.*type"
                            },
                            ForcedType().apply {
                                userType = "java.util.UUID"
                                converter = "de.cubbossa.pathfinder.storage.misc.UUIDConverter"
                                includeExpression = "id|.*_id|world"
                            }
                        )
                    }
                    target.apply {
                        directory = "src/main/jooq"
                        packageName = "de.cubbossa.pathfinder.jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}