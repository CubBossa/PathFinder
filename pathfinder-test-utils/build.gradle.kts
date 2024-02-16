plugins {
    id("java")
    id("io.freefair.lombok") version "6.6.2"
}

group = "de.cubbossa"
version = "4.5.0"

repositories {
    mavenCentral()
  mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":pathfinder-api"))
    compileOnly(project(":pathfinder-core"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    implementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    implementation("org.xerial:sqlite-jdbc:3.41.2.1")
    implementation("com.h2database:h2:2.1.214")
}

tasks.test {
    useJUnitPlatform()
}