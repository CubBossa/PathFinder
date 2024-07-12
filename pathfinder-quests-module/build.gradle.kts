plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
    maven("https://jitpack.io")
}

dependencies {
    implementation("me.pikamug.quests:quests-core:5.0.5")
    implementation("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    implementation(project(":pathfinder-api"))
    implementation(project(":pathfinder-bukkit"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}