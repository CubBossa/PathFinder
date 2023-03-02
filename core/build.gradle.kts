import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    antlr
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("io.freefair.lombok") version "6.6.2"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
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
}

dependencies {
    // Antlr
    antlr("org.antlr:antlr4:4.12.0")

    // Adventure
    implementation("net.kyori:adventure-api:4.12.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.2")
    implementation("net.kyori:adventure-text-minimessage:4.12.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.12.0")

    // Configuration
    implementation("de.cubbossa:NBO-Core:1.0")

    // Commands
    implementation("de.cubbossa:commandapi-shade:8.7.5")

    // NBT
    implementation("de.tr7zw:item-nbt-api:2.11.1")

    // Other
    implementation("com.google.guava:guava:31.1-jre")

    // Particles
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
    implementation("xyz.xenondevs:particle:1.8.3")
    implementation("de.cubbossa:splinelib:1.0")
    implementation("de.cubbossa:SerializedEffects:1.0")

    // Plugins
    implementation(project(":pathfinder-graph"))
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.2")

    // Statistics
    implementation("org.bstats:bstats-bukkit:3.0.1")

    // Spigot
    compileOnly("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.0.18")
    testImplementation("com.mojang:brigadier:1.0.18")
    compileOnly("com.mojang:authlib:1.5.25")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

    // UI
    implementation("de.cubbossa:Translations:1.1")
    implementation("de.cubbossa:MenuFramework:1.2")
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

    depend = listOf("ProtocolLib")
    softDepend = listOf("PlaceholderAPI")

    defaultPermission = BukkitPluginDescription.Permission.Default.OP
    permissions {
        register("pathfinder.command.pathfinder.info")
        register("pathfinder.command.pathfinder.help")
        register("pathfinder.command.pathfinder.reload")
        register("pathfinder.command.pathfinder.import")
        register("pathfinder.command.pathfinder.export")
        register("pathfinder.command.find")
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
        fun relocate(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to", null)
        }

        relocate("org.bstats", "bstats")
        relocate("de.cubbossa.serializedeffects", "serializedeffects")
        relocate("de.cubbossa.nbo", "nbo")
        relocate("net.kyori", "kyori")
        relocate("de.cubbossa.menuframework", "gui")
        relocate("de.cubbossa.translations", "translations")
        relocate("de.cubbossa.splinelib", "splinelib")
        relocate("de.cubbossa.particle", "particle")
        relocate("dev.jorel.commandapi", "commandapi")
        relocate("de.tr7zw.changeme.nbtapi", "nbtapi")
        relocate("org.antlr", "antlr")
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