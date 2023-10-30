plugins {
    idea
    java
    eclipse
    checkstyle
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "de.cubbossa"
version = "4.5.0"

checkstyle {
    toolVersion = "10.7.0"
    configFile = file("${project.rootDir}/google_checks.xml")
    isShowViolations = true
}

subprojects {

    apply {
        plugin("java")
        plugin("checkstyle")
    }
    checkstyle {
        toolVersion = "10.7.0"
        config =  rootProject.resources.text.fromFile("google_checks.xml")
        isShowViolations = true
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