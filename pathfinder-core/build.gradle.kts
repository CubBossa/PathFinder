import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.ForcedType

plugins {
    antlr
    `java-library`
    `maven-publish`
    `java-test-fixtures`
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("io.freefair.lombok") version "6.6.2"
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
    maven("https://repo.codemc.org/repository/maven-snapshots/")
    maven("https://libraries.minecraft.net/")
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    // Antlr
    antlr("org.antlr:antlr4:4.12.0") { isTransitive = true }

    // Configuration
    api("de.exlll:configlib-yaml:4.5.0")

    // Commands
    api("dev.jorel:commandapi-bukkit-shade:9.5.1")

    // Other
    api("org.jooq:jooq:3.18.4")
    jooqGenerator("org.xerial:sqlite-jdbc:3.41.2.1")
    implementation("org.xerial:sqlite-jdbc:3.41.2.1")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("io.r2dbc:r2dbc-spi:1.0.0.RELEASE")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("de.cubbossa:disposables-api:1.2")
    api("org.flywaydb:flyway-core:8.0.0")

    // Particles
    api("de.cubbossa:splinelib:1.0")

    // Plugins
    api(project(":pathfinder-api"))
    api(project(":pathfinder-graph"))

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
    testImplementation("org.xerial:sqlite-jdbc:3.41.2.1")
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation(project(":pathfinder-test-utils"))

    // UI
    api("de.cubbossa:Translations:2.3")

    // Utility
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.6")
    implementation(files("generated/plugin-yml/Bukkit/plugin.yml"))
}

tasks {
    generateGrammarSource {
        // Tell ANTLR to generate visitor classes
        arguments.plusAssign("-visitor")
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
                                userType = "de.cubbossa.pathfinder.misc.NamespacedKey"
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