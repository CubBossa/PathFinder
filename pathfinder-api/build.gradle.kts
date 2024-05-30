plugins {
    `java-library`
    id("io.freefair.lombok") version "6.6.2"
    kotlin("jvm")
}

group = "de.cubbossa"

repositories {
    mavenCentral()
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
}

dependencies {
    api(project(":pathfinder-graph"))
    api("de.cubbossa:disposables-api:1.3")
    implementation("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    // Adventure
    api("net.kyori:adventure-api:4.13.0")
    api("net.kyori:adventure-platform-bukkit:4.3.0")
    api("net.kyori:adventure-text-minimessage:4.13.0")
    api("net.kyori:adventure-text-serializer-plain:4.13.0")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}