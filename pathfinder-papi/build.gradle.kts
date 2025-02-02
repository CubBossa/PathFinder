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
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
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

    compileOnly(project(":pathfinder-bukkit"))

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    // Service
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc5")
    implementation("com.google.auto.service:auto-service:1.0")

    // PAPI
    compileOnly("me.clip:placeholderapi:2.11.6")
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

        relocate("org.openjdk.nashorn", "nashorn")
    }
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}