# GPS - PathFinder

---

## What is it

Pathfinder is a plugin for minecraft servers, that allows administrators to set up graphs of waypoints and connecting edges. This roadmap can then be used to visualize shortest paths, discover points of interest and more.

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


## Project Structure

### pathfinder-core

The main module that handles RoadMaps, Waypoints, Nodegroups and the according logic.
It is to be used as API for now. Interesting classes might be `NodeType`, `VisualizerType`, `PathVisualizer`
and the matching handlers `NodeTypeHandler` and `VisualizerHandler`. More information on using the
API will be added to the docs soon.

### pathfinder-graphs

The logic for path solving on graphs. It is a dependency of `pathfinder-core` and has to be
shaded into the core module, otherwise exceptions will occur.

### pathfinder-editmode

Adds an ingame editor for RoadMaps. It uses clientside armorstands and particles to display
waypoints and edges and requires [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/).
It is not necessary to shade `pathfinder-editmode` into core. The module registeres itself to core
as a service once it is shaded. If it is not, the ingame command `/roadmap editmode <roadmap>` will not work.

You can also use your own editmode visualizer by implementing the RoadMapEditor interface
and registering a RoadMapEditorFactory service class.

### pathfinder-scripted-visualizer

Adds a pathvisualizer to the core module that uses javascript to define particle behaviour.
Particles can be shifted by using complex math expressions. The visualizer requires a heavy
script engine service to be shaded into the jar. Therefore, it is a separate module that is again
not necessary for core to function. It implements the PathPluginExtension Service interface and
registers the VisualizerType implementation to the VisualizerHandler from core.