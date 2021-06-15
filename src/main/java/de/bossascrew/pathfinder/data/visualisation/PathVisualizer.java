package de.bossascrew.pathfinder.data.visualisation;

import lombok.Getter;
import lombok.Setter;

/**
 * Visualisiert einen Pfad aus Locations mit Partikeln
 */
@Getter
@Setter
public class PathVisualizer extends Visualizer<PathVisualizer> {

    public PathVisualizer(int databaseId, String name) {
        super(databaseId, name);
    }
}
