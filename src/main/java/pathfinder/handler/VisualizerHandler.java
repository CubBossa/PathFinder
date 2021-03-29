package pathfinder.handler;

import lombok.Getter;
import pathfinder.visualisation.PathVisualizer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Managed alle Visualizer Profile, mit denen man einen Pfad darstellen kann. Roadmaps und die Art, wie ein Pfad dargestellt wird
 * sind unabhängig voneinander. Man kann also verschiedene Darstellungsprofile mit verschiedenen Partikeln anlegen und sagen, welche Roadmap mit welchem
 * Visualizer angezeigt werden soll. So könnte man Spielern später sogar die Möglichkeit geben, den Partikel-Style zu ändern.
 */
public class VisualizerHandler {

    @Getter
    private static VisualizerHandler instance;

    private Map<Integer, PathVisualizer> visualizerMap;

    public VisualizerHandler() {

        this.visualizerMap = null; //TODO lade aus datenbank
    }

    public @Nullable
    PathVisualizer getVisualizer(String name) {
        for(PathVisualizer v : visualizerMap.values()) {
            if(v.getName().equals(name))
                return v;
        }
        return null;
    }

    public @Nullable
    PathVisualizer getVisualizer(int databaseId) {
        for(PathVisualizer v : visualizerMap.values()) {
            if(v.getDatabaseId() == databaseId)
                return v;
        }
        return null;
    }
}
