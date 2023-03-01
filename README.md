# GPS - PathFinder

---

## What is it

Pathfinder is a plugin for minecraft servers, that allows administrators to setup graphs of waypoints and connecting edges. This roadmap can then be used to visualize shortest paths, discover points of interest and so on.

For more details check the [docs](https://docs.leonardbausenwein.de/getting_started/introduction.html).

## Contribution
### Building
`./gradlew build`
### Running
`./gradlew runServer`
### Publishing
`./gradlew publish`

## Dependency

The pathfinder artifact can be found in a nexus repository:
```kotlin
repositories {
    maven("https://nexus.leonardbausenwein.de/repository/maven-public/")
}

dependencies {
    implementation("de.cubbossa:pathfinder-core:[VERSION]")
}
```

``` xml
<repository>
    <id>cubbossa</id>
    <url>https://nexus.leonardbausenwein.de/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>de.cubbossa</groupId>
    <artifactId>pathfinder-core</artifactId>
    <version>[VERSION]</version>
</dependency>
```
