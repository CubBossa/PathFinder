package pathfinder.visualisation;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Particle;

/**
 * Visualisiert einen Pfad aus Locations mit Partikeln
 */
@Getter
@Setter
public class PathVisualizer {

    private final int databaseId;
    private String name;

    Particle particle;

    public PathVisualizer(int databaseId, String name) {
        this.databaseId = databaseId;
        this.name = name;
    }

}
