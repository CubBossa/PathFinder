plugins {
    `java-library`
    kotlin("jvm")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.0.0")

    api("com.google.guava:guava:31.1-jre") // this shouldn't be needed

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}
repositories {
    mavenCentral()
}