package de.bossascrew.pathfinder.handler;

import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.visualisation.PathVisualizer;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Managed alle Visualizer Profile, mit denen man einen Pfad darstellen kann. Roadmaps und die Art, wie ein Pfad dargestellt wird
 * sind unabhängig voneinander. Man kann also verschiedene Darstellungsprofile mit verschiedenen Partikeln anlegen und sagen, welche Roadmap mit welchem
 * Visualizer angezeigt werden soll. So könnte man Spielern später sogar die Möglichkeit geben, den Partikel-Style zu ändern.
 */
public class VisualizerHandler {

    @Getter
    private static VisualizerHandler instance;

    private final Map<Integer, PathVisualizer> pathVisualizerMap;
    private final Map<Integer, EditModeVisualizer> editVisualizerMap;

    public VisualizerHandler() {
        this.pathVisualizerMap = null;
        this.editVisualizerMap = DatabaseModel.getInstance().loadEditModeVisualizer();

        if(!pathVisualizerMap.containsKey(0)) {
            PathVisualizer vis = new PathVisualizer(0, "default");
            pathVisualizerMap.put(0, vis);
        }

        if(!editVisualizerMap.containsKey(0)) {
            EditModeVisualizer vis = new EditModeVisualizer(0, "default");
            editVisualizerMap.put(0, vis);
        }
    }

    public boolean isNameUniquePath(String name) {
        return pathVisualizerMap.values().stream()
                .map(PathVisualizer::getName)
                .anyMatch(element -> element.equalsIgnoreCase(name));
    }

    public boolean isNameUniqueEditMode(String name) {
        return editVisualizerMap.values().stream()
                .map(EditModeVisualizer::getName)
                .anyMatch(element -> element.equalsIgnoreCase(name));
    }

    public @Nullable
    PathVisualizer getPathVisualizer(String name) {
        for(PathVisualizer v : pathVisualizerMap.values()) {
            if(v.getName().equals(name))
                return v;
        }
        return null;
    }

    public @Nullable
    PathVisualizer getPathVisualizer(int databaseId) {
        for(PathVisualizer v : pathVisualizerMap.values()) {
            if(v.getDatabaseId() == databaseId)
                return v;
        }
        return null;
    }

    public @Nullable
    EditModeVisualizer getEditVisualizer(String name) {
        for(EditModeVisualizer v : editVisualizerMap.values()) {
            if(v.getName().equals(name))
                return v;
        }
        return null;
    }

    public @Nullable
    EditModeVisualizer getEditVisualizer(int databaseId) {
        for(EditModeVisualizer v : editVisualizerMap.values()) {
            if(v.getDatabaseId() == databaseId)
                return v;
        }
        return null;
    }

    public Stream<PathVisualizer> getPathVisualizers() {
        return pathVisualizerMap.values().stream();
    }

    public Stream<EditModeVisualizer> getEditModeVisualizer() {
        return editVisualizerMap.values().stream();
    }
}
