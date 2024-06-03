
plugins {
    idea
    java
    eclipse
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "de.cubbossa"
version = "5.1.0"

tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java) {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

subprojects {

    apply {
        plugin("java")
        plugin("com.google.devtools.ksp")
    }

    repositories {
        mavenCentral()
    }

//    dependencies.ksp("care.better.pf4j:pf4j-kotlin-symbol-processing:2.0.0-1.0.1")

    dependencies {
        val pf4jVer = "3.11.0"
        implementation("org.pf4j:pf4j:${pf4jVer}")

//        ksp("care.better.pf4j:pf4j-kotlin-symbol-processing:2.0.0-1.0.1")
        annotationProcessor("org.pf4j:pf4j:${pf4jVer}")
        testImplementation("org.pf4j:pf4j:${pf4jVer}")
        testAnnotationProcessor("org.pf4j:pf4j:${pf4jVer}")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.16.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.16.0")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}