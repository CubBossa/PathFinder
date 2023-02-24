plugins {
    antlr
    idea
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.freefair.lombok") version "6.6.2"
    id("xyz.jpenilla.run-paper") version "2.0.0"
}

group = "de.cubbossa"
version = "2.1.0"

val minecraftVersion: String by project

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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
