import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("io.freefair.lombok") version "6.6.2"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
}

group = "de.cubbossa"


val minecraftVersion = project.property("minecraft_version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.codemc.org/repository/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    api(project(":pathfinder-api"))
    api(project(":pathfinder-core"))
    api("de.cubbossa:disposables-bukkit:1.1")
    runtimeOnly(project(path = ":pathfinder-editmode", configuration = "shadow"))
    runtimeOnly(project(":pathfinder-scripted-visualizer"))

    // Spigot
    compileOnlyApi("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnlyApi("com.mojang:brigadier:1.0.18")
    testImplementation("com.mojang:brigadier:1.0.18")

    // Commands
    api("dev.jorel:commandapi-bukkit-shade:9.3.0")

    // Statistics
    implementation("org.bstats:bstats-bukkit:3.0.1")

    // Tests
    testImplementation(project(":pathfinder-test-utils"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    // Utility
    implementation(files("generated/plugin-yml/Bukkit/plugin.yml"))
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

tasks.test {
    useJUnitPlatform()
}

bukkit {
    name = "PathFinder"
    version = rootProject.version.toString()
    description = null
    website = "https://docs.leonardbausenwein.de"
    author = "CubBossa"
    authors = listOf("LooFifteen")

    main = "de.cubbossa.pathfinder.PathFinderPlugin"

    apiVersion = "1.18"

    softDepend = listOf(
            "PlaceholderAPI",
            "ProtocolLib",
            "ProtocolSupport",
            "ViaVersion",
            "ViaBackwards",
            "ViaRewind",
            "Geyser-Spigot"
    )
    libraries = listOf(
            "org.openjdk.nashorn:nashorn-core:15.4",
            "org.snakeyaml:snakeyaml-engine:2.0",
            "com.zaxxer:HikariCP:5.0.1",
            "org.antlr:antlr4-runtime:4.12.0",
            "org.jooq:jooq:3.18.4",
            "com.github.ben-manes.caffeine:caffeine:3.1.6"
    )

    defaultPermission = BukkitPluginDescription.Permission.Default.OP
    permissions {
        register("pathfinder.command.pathfinder.info")
        register("pathfinder.command.pathfinder.help")
        register("pathfinder.command.pathfinder.reload")
        register("pathfinder.command.pathfinder.import")
        register("pathfinder.command.pathfinder.export")
        register("pathfinder.command.discoveries") { default = BukkitPluginDescription.Permission.Default.TRUE }
        register("pathfinder.command.find") { default = BukkitPluginDescription.Permission.Default.TRUE }
        register("pathfinder.command.findlocation")
        register("pathfinder.command.findplayer.request")
        register("pathfinder.command.findplayer.accept")
        register("pathfinder.command.findplayer.decline")
        register("pathfinder.command.cancel_path") { default = BukkitPluginDescription.Permission.Default.TRUE }
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
        mergeServiceFiles {
            setPath("META-INF/")
            include("extensions.idx")
            exclude("*")
        }

        // "whitelist" approach, only include transitive dependencies that are truly necessary.
        // otherwise jar grows from ~8mb to ~30mb
        dependencies {
            include(project(":pathfinder-api"))
            include(project(":pathfinder-core"))
            include(project(":pathfinder-graph"))
            include(project(":pathfinder-editmode"))
            include(project(":pathfinder-scripted-visualizer"))
            include(dependency("de.cubbossa:disposables-api:.*"))
            include(dependency("de.cubbossa:disposables-bukkit:.*"))
            include(dependency("de.cubbossa:Translations:.*"))
            include(dependency("de.cubbossa:splinelib:.*"))
            include(dependency("net.kyori:.*"))
            include(dependency("net.kyori:.*"))
            include(dependency("org.bstats:.*"))
            include(dependency("xyz.xenondevs:particle:.*"))
            include(dependency("dev.jorel:commandapi-bukkit-shade:.*"))
            include(dependency("de.exlll:configlib-yaml:.*"))
            include(dependency("de.exlll:configlib-core:.*"))
            include(dependency("org.flywaydb:flyway-core:.*"))
            include(dependency("org.pf4j:pf4j:.*"))
        }

        fun relocate(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to", null)
        }

        relocate("org.bstats", "bstats")
        relocate("xyz.xenondevs.particle", "particle")
        relocate("dev.jorel.commandapi", "commandapi")
        relocate("de.cubbossa.translations", "translations")
        relocate("de.cubbossa.splinelib", "splinelib")
        relocate("de.cubbossa.disposables", "disposables")
        relocate("xyz.xenondevs.particle", "particle")
        relocate("dev.jorel.commandapi", "commandapi")
        relocate("de.exlll", "exlll")
        relocate("org.flywaydb", "flywaydb")
        relocate("org.pf4j", "pf4j")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
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
        groupId = rootProject.group.toString()
        artifactId = name
        version = rootProject.version.toString()

        from(components["java"])
    }
}