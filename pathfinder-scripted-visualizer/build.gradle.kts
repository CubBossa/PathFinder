plugins {
    java
    id("io.freefair.lombok") version "6.6.2"
    id("com.github.johnrengelman.shadow") version "8.1.0"
    kotlin("jvm")
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

    compileOnly(project(":pathfinder-bukkit"))

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    // Service
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc5")
    implementation("com.google.auto.service:auto-service:1.0")

    // JavaScript
    implementation("org.openjdk.nashorn:nashorn-core:15.4")
    implementation("org.snakeyaml:snakeyaml-engine:2.0")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}