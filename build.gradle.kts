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
version = "5.0.0"

subprojects {

    apply {
        plugin("java")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.pf4j:pf4j:3.11.0")
        annotationProcessor("org.pf4j:pf4j:3.11.0")
        testImplementation("org.pf4j:pf4j:3.11.0")
        testAnnotationProcessor("org.pf4j:pf4j:3.11.0")
    }
}