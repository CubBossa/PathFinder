# GPS - PathFinder

---

## What is it

Pathfinder is a plugin for minecraft servers, that allows administrators to setup graphs of waypoints and connecting edges. This roadmap can then be used to visualize shortest paths, discover points of interest and so on.

For more details check the [docs](https://docs.leonardbausenwein.de/pathfinder/1.0/what_are_roadmaps). (WIP)

## Build

To build the pathfinder, clone the repository and run `maven clean install`

## Depencency

The pathfinder artifact can be found in a nexus repository:
``` xml
<repository>
    <id>cubbossa</id>
    <url>https://nexus.leonardbausenwein.de/repository/maven-public</url>
</repository>

<dependency>
    <groupId>de.cubbossa</groupId>
    <artifactId>PathFinder</artifactId>
    <version>[VERSION]</version>
</dependency>
```