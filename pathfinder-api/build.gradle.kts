plugins {
    `java-library`
    kotlin("jvm")
    id("io.freefair.lombok") version "8.1.0"
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
    api("net.kyori:adventure-platform-bukkit:4.3.3")
    api("net.kyori:adventure-text-minimessage:4.13.0")
    api("net.kyori:adventure-text-serializer-plain:4.13.0")

  // UI
  api("de.cubbossa:TinyTranslations-common:4.5.1")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java) {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}