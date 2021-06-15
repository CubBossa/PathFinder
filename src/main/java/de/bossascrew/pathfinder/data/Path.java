package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * Enthält alle wichtigen Informationen zum Anzeigen des Pfades gecached und läuft die Repeating Tasks
 */
@Getter
public class Path extends ArrayList<Vector> {

    private final RoadMap roadMap;
    @Setter
    private boolean active;
    @Setter
    private PathVisualizer visualizer;

    public Path(RoadMap roadMap) {
        this.roadMap = roadMap;
        this.active = false;
    }

    public void run() {
        if (active) {
            return;
        }
        this.active = true;

        System.out.println("showing path");

        //TODO scheduler starten
        //oder alternativ einen scheduler für alle paths, je nach dem was performanter ist
    }

    public void cancel() {
        if (!active) {
            return;
        }
        this.active = false;

        System.out.println("cancelling path");
        //TODO scheduler stoppen
    }
}
