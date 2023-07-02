plugins {
    idea
    java
    eclipse
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "de.cubbossa"
version = "3.0.5"

subprojects {

    apply {
        plugin("java")
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        // Service
        implementation("com.google.auto.service:auto-service:1.0")
        annotationProcessor("com.google.auto.service:auto-service:1.0-rc5")
    }
}