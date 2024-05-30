pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.0"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.4.0")
}

rootProject.name = "pathfinder"

sequenceOf(
        "api",
        "core",
        "bukkit",
        "graph",
        "editmode",
        "papi",
        "scripted-visualizer",
).forEach {
    val name = "${rootProject.name}-$it"
    include(name)
    project(":$name").projectDir = file(name)
}
include("pathfinder-test-utils")
