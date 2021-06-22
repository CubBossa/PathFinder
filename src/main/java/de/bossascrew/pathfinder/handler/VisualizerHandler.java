package de.bossascrew.pathfinder.handler;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import lombok.Getter;
import org.bukkit.Particle;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Managed alle Visualizer Profile, mit denen man einen Pfad darstellen kann. Roadmaps und die Art, wie ein Pfad dargestellt wird
 * sind unabhängig voneinander. Man kann also verschiedene Darstellungsprofile mit verschiedenen Partikeln anlegen und sagen, welche Roadmap mit welchem
 * Visualizer angezeigt werden soll. So könnte man Spielern später sogar die Möglichkeit geben, den Partikel-Style zu ändern.
 */
public class VisualizerHandler {

    @Getter
    private static VisualizerHandler instance;

    @Getter
    private final PathVisualizer defaultPathVisualizer;
    @Getter
    private final EditModeVisualizer defaultEditModeVisualizer;


    private Map<Integer, PathVisualizer> pathVisualizerMap;
    private Map<Integer, EditModeVisualizer> editVisualizerMap;

    public VisualizerHandler() {

        instance = this;

        //Lade Maps aus Datenbank
        this.pathVisualizerMap = DatabaseModel.getInstance().loadPathVisualizer();
        this.editVisualizerMap = DatabaseModel.getInstance().loadEditModeVisualizer();

        //Fehlerbehandlung
        if (pathVisualizerMap == null) {
            this.pathVisualizerMap = new ConcurrentHashMap<>();
            PathPlugin.getInstance().getLogger().log(Level.SEVERE, "PfadVisualisierermap fehlerhaft.");
        }
        if (editVisualizerMap == null) {
            this.editVisualizerMap = new ConcurrentHashMap<>();
            PathPlugin.getInstance().getLogger().log(Level.SEVERE, "Editmode-Visualisierermap fehlerhaft.");
        }

        //Lade default Visualizer, um null-Parameter zu ergänzen und dynamisch ändern zu können.
        defaultPathVisualizer = pathVisualizerMap.getOrDefault(1, createPathVisualizer("default", null, Particle.SPELL_WITCH,
                20d, 10, 3, 10));

        defaultEditModeVisualizer = editVisualizerMap.getOrDefault(1, createEditModeVisualizer(
                "default", null, Particle.FLAME, 50d, 10000, 20,
                8621, 8619));
    }

    /**
     * Erstellt einen Pfadvisualizer mit angegebenen Parametern. Wird ein Parameter auf "null" gesetzt, lädt der Visualizer den Wert des parents.
     * Ist kein Parent angegeben, wird der Default-Editmode-Visualizer als Parent verwendet.
     *
     * @param name             Der Name des Visualisierers, den Anwender beim Setzen und Auswählen sehen.
     * @param parent           Der PathVisualizer, von dem alle mit null belegten Parameter geladen werden.
     * @param particle         Der Partikeleffekt, zum Beispiel FLAME.
     * @param particleDistance Alle wie viel Einheiten ein Partikel gespawnt werden soll, Einheit in Blöcken.
     * @param particleLimit    Wie viele Partikel höchstens angezeigt werden sollen.
     * @param particleSteps    Wie viele Punkte versetzt dargestellt werden sollen. (Bei 3 Steps wird ein Partikel alle 3 Zeitintervalle angezeigt, sein Nachbarpunkt zeitlich eins versetzt)
     * @param schedulerPeriod  Wie viele Ticks der Scheduler warten soll, bevor er neue Partikel generiert.
     * @return den Visualisierer mit allen gesetzten Parametern.
     */
    public PathVisualizer createPathVisualizer(String name, @Nullable PathVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance,
                                               @Nullable Integer particleLimit, @Nullable Integer particleSteps, @Nullable Integer schedulerPeriod) {

        if (!isNameUniquePath(name)) {
            return null;
        }
        if(parent == null) {
            parent = defaultPathVisualizer;
        }
        PathVisualizer vis = DatabaseModel.getInstance().newPathVisualizer(name, parent, particle, particleDistance, particleLimit, particleSteps, schedulerPeriod);
        if (vis == null) {
            return null;
        }
        pathVisualizerMap.put(vis.getDatabaseId(), vis);
        return vis;
    }

    /**
     * Erstellt einen Editmode-Visualizer mit angegebenen Parametern. Wird ein Parameter auf "null" gesetzt, lädt der Visualizer den Wert des parents.
     * Ist kein Parent angegeben, wird der Default-Editmode-Visualizer als Parent verwendet.
     *
     * @param name            Der Name des Visualisierers, den Anwender beim Setzen und Auswählen sehen.
     * @param parent           Der PathVisualizer, von dem alle mit null belegten Parameter geladen werden.
     * @param particle        Der Partikeleffekt, zum Beispiel FLAME.
     * @param particleLimit   Wie viele Partikel höchstens angezeigt werden sollen.
     * @param schedulerPeriod Alle wie viel Ticks soll der Visualisierer die Partikel darstellen.
     * @param nodeHeadId      Wegpunkte, die als Armorstands dargestellt werden, tragen als Kopf den CustomHead aus der HDB mit der angegebenen ID.
     * @param edgeHeadId      Kanten, die als Armorstands dargestellt werden, tragen als Kopf den CustomHead aus der HDB mit der angegebenen ID.
     * @return den Visualisierer mit allen gesetzten Parametern.
     */
    public EditModeVisualizer createEditModeVisualizer(String name, @Nullable EditModeVisualizer parent, @Nullable Particle particle, @Nullable Double particleDistance, @Nullable Integer particleLimit,
                                                       @Nullable Integer schedulerPeriod, @Nullable Integer nodeHeadId, @Nullable Integer edgeHeadId) {
        if (!isNameUniqueEditMode(name)) {
            return null;
        }
        if(parent == null) {
            parent = defaultEditModeVisualizer;
        }

        EditModeVisualizer vis = DatabaseModel.getInstance().newEditModeVisualizer(name, parent, particle, particleDistance, particleLimit,
                schedulerPeriod, nodeHeadId, edgeHeadId);
        if (vis == null) {
            return null;
        }
        editVisualizerMap.put(vis.getDatabaseId(), vis);
        return vis;
    }

    public boolean deletePathVisualizer(PathVisualizer visualizer) {
        for(PathVisualizer vis : pathVisualizerMap.values()) {
            if(vis.getParent() != null && vis.getParent().getDatabaseId() == visualizer.getDatabaseId()) {
                vis.setParent(visualizer.getParent());
            }
        }
        if(this.pathVisualizerMap.remove(visualizer.getDatabaseId()) != null) {
            DatabaseModel.getInstance().deletePathVisualizer(visualizer);
            return true;
        }
        return false;
    }

    public boolean deleteEditModeVisualizer(EditModeVisualizer visualizer) {
        for(EditModeVisualizer vis : editVisualizerMap.values()) {
            if(vis.getParent() != null && vis.getParent().getDatabaseId() == visualizer.getDatabaseId()) {
                vis.setParent(visualizer.getParent());
            }
        }
        if(this.editVisualizerMap.remove(visualizer.getDatabaseId()) != null) {
            DatabaseModel.getInstance().deleteEditModeVisualizer(visualizer);
            return true;
        }
        return false;
    }

    public boolean isNameUniquePath(String name) {
        return pathVisualizerMap.values().stream()
                .map(PathVisualizer::getName)
                .noneMatch(element -> element.equalsIgnoreCase(name));
    }

    public boolean isNameUniqueEditMode(String name) {
        return editVisualizerMap.values().stream()
                .map(EditModeVisualizer::getName)
                .noneMatch(element -> element.equalsIgnoreCase(name));
    }

    public @Nullable
    PathVisualizer getPathVisualizer(String name) {
        return getPathVisualizerStream()
                .filter(pathVisualizer -> pathVisualizer.getName().equals(name))
                .findAny().orElse(null);
    }

    public @Nullable
    PathVisualizer getPathVisualizer(int databaseId) {
        return getPathVisualizerStream()
                .filter(pathVisualizer -> pathVisualizer.getDatabaseId() == databaseId)
                .findAny().orElse(null);
    }

    public @Nullable
    EditModeVisualizer getEditModeVisualizer(String name) {
        return getEditModeVisualizerStream()
                .filter(editModeVisualizer -> editModeVisualizer.getName().equals(name))
                .findAny().orElse(null);
    }

    public @Nullable
    EditModeVisualizer getEditModeVisualizer(int databaseId) {
        return getEditModeVisualizerStream()
                .filter(editModeVisualizer -> editModeVisualizer.getDatabaseId() == databaseId)
                .findAny().orElse(null);
    }

    public Stream<PathVisualizer> getPathVisualizerStream() {
        return pathVisualizerMap.values().stream();
    }

    public Stream<EditModeVisualizer> getEditModeVisualizerStream() {
        return editVisualizerMap.values().stream();
    }

    public Collection<PathVisualizer> getPathVisualizers() {
        return pathVisualizerMap.values();
    }

    public Collection<EditModeVisualizer> getEditModeVisualizers() {
        return editVisualizerMap.values();
    }
}
