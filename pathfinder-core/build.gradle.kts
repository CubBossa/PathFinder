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
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    // Antlr
    antlr("org.antlr:antlr4:4.12.0") { isTransitive = true }

    // Configuration
    api("com.github.Exlll.ConfigLib:configlib-yaml:v4.2.0")

    // Commands
    api("dev.jorel:commandapi-bukkit-shade:9.0.0")

    // Other
    api("org.jooq:jooq:3.18.0")
    jooqGenerator("org.xerial:sqlite-jdbc:3.41.0.0")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("io.r2dbc:r2dbc-spi:1.0.0.RELEASE")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Particles
    api("de.cubbossa:splinelib:1.0")

    // Plugins
    runtimeOnly(project(":pathfinder-editmode"))
    runtimeOnly(project(":pathfinder-scripted-visualizer"))
    api(project(":pathfinder-api"))
    implementation(project(":pathfinder-graph"))

    // Statistics
    implementation("org.bstats:bstats-bukkit:3.0.1")

    // Spigot
    compileOnlyApi("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnlyApi("com.mojang:brigadier:1.0.18")
    testImplementation("com.mojang:brigadier:1.0.18")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    // UI
    api("de.cubbossa:Translations:2.2")

    // Utility
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.6")
    implementation(files("generated/plugin-yml/Bukkit/plugin.yml"))
}

tasks {
    build {
        dependsOn(checkstyleMain)
        dependsOn(shadowJar)
    }
    generateGrammarSource {
        // Tell ANTLR to generate visitor classes
        arguments.plusAssign("-visitor")
    }
    shadowJar {

        archiveFileName.set("PathFinder-${parent?.version}.jar")
        mergeServiceFiles()

        // "whitelist" approach, only include transitive dependencies that are truly necessary.
        // otherwise jar grows from ~8mb to ~30mb
        dependencies {
            include(project(":pathfinder-api"))
            include(project(":pathfinder-graph"))
            include(dependency("net.kyori:.*"))
            include(dependency("de.cubbossa:MenuFramework:.*"))
            include(dependency("de.cubbossa:Translations:.*"))
            include(dependency("de.cubbossa:splinelib:.*"))
            include(dependency("org.openjdk.nashorn:nashorn-core:.*"))
            include(dependency("org.ow2.asm:asm:.*"))
            include(dependency("org.ow2.asm:asm-util:.*"))
            include(dependency("org.antlr:antlr4-runtime:.*"))
            include(dependency("com.github.Exlll.ConfigLib:configlib-yaml:.*"))
            include(dependency("com.github.Exlll.ConfigLib:configlib-core:.*"))
            include(dependency("org.snakeyaml:snakeyaml-engine:.*"))
            include(dependency("org.jooq:jooq:.*"))
            include(dependency("org.reactivestreams:reactive-streams:.*"))
            include(dependency("io.r2dbc:r2dbc-spi:.*"))
            include(dependency("com.zaxxer:HikariCP:.*"))
            include(dependency("com.github.ben-manes.caffeine:caffeine:.*"))
        }

        fun relocate(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to", null)
        }

        relocate("com.zaxxer.hikari", "hikari")
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
        groupId = rootProject.group.toString()
        artifactId = name
        version = rootProject.version.toString()

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
                                userType = "de.cubbossa.pathapi.misc.NamespacedKey"
                                converter =
                                    "de.cubbossa.pathfinder.storage.misc.NamespacedKeyConverter"
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