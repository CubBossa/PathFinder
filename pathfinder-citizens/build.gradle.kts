plugins {
    `java-library`
    id("io.freefair.lombok") version "6.6.2"
}

val minecraftVersion = project.property("minecraft_version") as String

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.citizensnpcs.co/repo")
    maven("https://libraries.minecraft.net")
    maven("https://repo.minebench.de/")
}

dependencies {

    compileOnly(project(":pathfinder-bukkit"))
    testImplementation(project(":pathfinder-bukkit"))
    implementation("de.cubbossa:disposables-api:1.3")

    // Spigot
    implementation("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    // Citizens
    implementation("net.citizensnpcs:citizens-main:2.0.33-SNAPSHOT")
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}