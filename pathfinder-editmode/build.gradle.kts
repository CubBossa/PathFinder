plugins {
    java
    id("io.freefair.lombok") version "6.6.2"
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

val minecraftVersion = project.property("minecraft_version") as String

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {

    compileOnly(project(":pathfinder-core"))

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    // Spigot
    compileOnly("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.5.25")

    // NBT
    compileOnly("de.tr7zw:item-nbt-api:2.11.1")

    // Adventure
    compileOnly("net.kyori:adventure-api:4.12.0")
    compileOnly("net.kyori:adventure-platform-bukkit:4.1.2")
    compileOnly("net.kyori:adventure-text-minimessage:4.12.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.12.0")

    // Configuration
    compileOnly("de.cubbossa:NBO-Core:1.0")
    compileOnly("de.cubbossa:SerializedEffects:1.0")

    // UI
    compileOnly("de.cubbossa:Translations:1.1")
    compileOnly("de.cubbossa:MenuFramework:1.2")

    // Service
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc5")
    implementation("com.google.auto.service:auto-service:1.0")

    // Precompiled Particles
    implementation("xyz.xenondevs:particle:1.8.3")

    // Client ArmorStands
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
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
    shadowJar {
        fun relocate(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to", null)
        }

        relocate("de.cubbossa.menuframework", "gui")
        relocate("xyz.xenondevs.particle", "particle")
        relocate("de.tr7zw.changeme.nbtapi", "nbtapi")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}