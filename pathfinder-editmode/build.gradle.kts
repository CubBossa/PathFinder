plugins {
    id("java")
}

group = "de.cubbossa"
version = "3.0.0"

repositories {
    mavenCentral()
}

dependencies {

    compileOnly(project(":pathfinder-core"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    annotationProcessor("com.google.auto.service:auto-service:1.0-rc5")
    implementation("com.google.auto.service:auto-service:1.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}