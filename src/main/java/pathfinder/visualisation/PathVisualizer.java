package pathfinder.visualisation;

import lombok.Getter;
import org.bukkit.Particle;

/**
 * Visualisiert einen Pfad aus Locations mit Partikeln
 */
@Getter
public class PathVisualizer {

    int databaseId;
    String name;

    Particle particle;

    public PathVisualizer(String name) {
        this.name = name;
    }

}
