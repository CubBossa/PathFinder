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
version = "5.3.0"

subprojects {

    apply {
        plugin("java")
    }

    repositories {
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        implementation("org.pf4j:pf4j:3.11.0")
        annotationProcessor("org.pf4j:pf4j:3.11.0")
        testImplementation("org.pf4j:pf4j:3.11.0")

        testAnnotationProcessor("org.pf4j:pf4j:3.11.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
//        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

        testImplementation("com.pholser:junit-quickcheck-core:1.0")
        testImplementation("com.pholser:junit-quickcheck-generators:1.0")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}