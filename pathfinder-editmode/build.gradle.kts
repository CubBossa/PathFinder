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
    maven("https://repo.codemc.org/repository/maven-snapshots/")
}

val minecraftVersion = project.property("minecraft_version") as String

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://libraries.minecraft.net/")
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {

    compileOnly(project(":pathfinder-bukkit"))
    testImplementation(project(":pathfinder-bukkit"))

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
    implementation("xyz.xenondevs:particle:1.8.4")

    // Client ArmorStands
    implementation("de.cubbossa:ClientEntities:1.0.1")
    implementation("com.github.retrooper.packetevents:spigot:2.0.2")
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
            include(dependency("de.cubbossa:ClientEntities:.*"))
            include(dependency("xyz.xenondevs:particle:.*"))
            include(dependency("de.tr7zw:item-nbt-api:.*"))
            include(dependency("de.item-nbt-api:.*"))
            include(dependency("com.github.retrooper.packetevents:spigot:.*"))
            include(dependency("com.github.retrooper.packetevents:api:.*"))
        }

        fun relocate(from: String, to: String) {
            relocate(from, "de.cubbossa.pathfinder.lib.$to", null)
        }

        relocate("de.cubbossa.menuframework", "gui")
        relocate("de.cubbossa.cliententities", "cliententities")
        relocate("xyz.xenondevs.particle", "particle")
        relocate("de.tr7zw.changeme.nbtapi", "nbtapi")
        relocate("com.github.retrooper.packetevents", "packetevents.api")
        relocate("io.github.retrooper.packetevents", "packetevents.impl")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}