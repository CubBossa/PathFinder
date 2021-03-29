package pathfinder.util;

import lombok.Getter;
import lombok.Setter;
import pathfinder.visualisation.PathVisualizer;

/**
 * Enthält alle wichtigen Informationen zum Anzeigen des Pfades gecached und läuft die Repeating Tasks
 */
public class Path {

    @Getter
    private int roadMapId;
    @Getter
    private boolean active;

    PathVisualizer visualizer;

    public Path(int roadMapId) {
        this.roadMapId = roadMapId;
        this.active = false;
    }

    public void run() {
        assert !active;
        this.active = true;

        //TODO scheduler starten
        //oder alternativ einen scheduler für alle paths, je nach dem was performanter ist
    }

    public void cancel() {
        assert active;
        this.active = false;

        //TODO scheduler stoppen
    }
}
