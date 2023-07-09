plugins {
    `java-library`
    id("io.freefair.lombok") version "6.6.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val minecraftVersion = project.property("minecraft_version") as String

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.0.0")

    api(project(":pathfinder-core"))
    api("com.google.guava:guava:31.1-jre") // this shouldn't be needed

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

    compileOnlyApi("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
