plugins {
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
    testImplementation(project(":pathfinder-core"))

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    // Spigot
    compileOnly("com.mojang:authlib:1.5.25")
    testImplementation("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")

    // NBT
    implementation("de.tr7zw:item-nbt-api:2.11.1")

    // UI
    implementation("de.cubbossa:MenuFramework:1.2")

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

        dependencies {
            include(dependency("de.cubbossa:MenuFramework:.*"))
            include(dependency("xyz.xenondevs:particle:.*"))
            include(dependency("de.tr7zw:item-nbt-api:.*"))
        }

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