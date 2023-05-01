plugins {
    `java-library`
    id("io.freefair.lombok") version "6.6.2"
}

group = "de.cubbossa"


val minecraftVersion = project.property("minecraft_version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(project(":pathfinder-core"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Spigot
    compileOnlyApi("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:$minecraftVersion-R0.1-SNAPSHOT")

    api("de.cubbossa:SerializedEffects:1.0")

    // Commands
    api("dev.jorel:commandapi-bukkit-shade:9.0.0")

    // Statistics
    implementation("org.bstats:bstats-bukkit:3.0.1")

    // Tests
    testImplementation("com.github.seeseemelk:MockBukkit-v1.19:2.145.0")
    testImplementation("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    // Utility
    implementation(files("generated/plugin-yml/Bukkit/plugin.yml"))
}

tasks.test {
    useJUnitPlatform()
}