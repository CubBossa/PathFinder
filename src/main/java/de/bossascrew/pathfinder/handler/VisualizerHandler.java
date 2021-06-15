package de.bossascrew.pathfinder.handler;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import lombok.Getter;
import org.bukkit.Particle;

import javax.annotation.Nullable;
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

        //Lade Maps aus Datenbank
        this.pathVisualizerMap = new ConcurrentHashMap<>(); //TODO databasemodel
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
        defaultPathVisualizer = pathVisualizerMap.getOrDefault(0, createPathVisualizer("default", Particle.SPELL_WITCH,
                20d, 10));

        defaultEditModeVisualizer = editVisualizerMap.getOrDefault(0, createEditModeVisualizer(
                "default", Particle.FLAME, 50d, 10000, 20, 20,
                8621, 8619));


        //TODO bei laden aus der Datenbank den parent ablesen und setzen
        for (EditModeVisualizer visualizer : editVisualizerMap.values()) {
            if (visualizer.getDatabaseId() == 0) {
                continue;
            }
            visualizer.setParent(defaultEditModeVisualizer);
        }
    }

    /**
     * Erstellt einen Pfadvisualizer mit angegebenen Parametern. Wird ein Parameter auf "null" gesetzt, lädt der Visualizer den Wert des parents.
     * Ist kein Parent angegeben, wird der Default-Editmode-Visualizer als Parent verwendet.
     *
     * @param name             Der Name des Visualisierers, den Anwender beim Setzen und Auswählen sehen.
     * @param particle         Der Partikeleffekt, zum Beispiel FLAME.
     * @param particleDistance Alle wie viel Einheiten ein Partikel gespawnt werden soll, Einheit in Blöcken.
     * @param particleLimit    Wie viele Partikel höchstens angezeigt werden sollen.
     * @return den Visualisierer mit allen gesetzten Parametern.
     */
    public PathVisualizer createPathVisualizer(String name, @Nullable Particle particle, @Nullable Double particleDistance,
                                               @Nullable Integer particleLimit) {

        if (!isNameUniquePath(name)) {
            return null;
        }

        return null;
    }

    /**
     * Erstellt einen Editmode-Visualizer mit angegebenen Parametern. Wird ein Parameter auf "null" gesetzt, lädt der Visualizer den Wert des parents.
     * Ist kein Parent angegeben, wird der Default-Editmode-Visualizer als Parent verwendet.
     *
     * @param name                Der Name des Visualisierers, den Anwender beim Setzen und Auswählen sehen.
     * @param particle            Der Partikeleffekt, zum Beispiel FLAME.
     * @param particleDistance    Alle wie viel Einheiten ein Partikel gespawnt werden soll, Einheit in Blöcken.
     * @param particleLimit       Wie viele Partikel höchstens angezeigt werden sollen.
     * @param schedulerStartDelay Wie viele Ticks soll der Scheduler warten, bevor die Visualisierung beginnt.
     * @param schedulerPeriod     Alle wie viel Ticks soll der Visualisierer die Partikel darstellen.
     * @param nodeHeadId          Wegpunkte, die als Armorstands dargestellt werden, tragen als Kopf den CustomHead aus der HDB mit der angegebenen ID.
     * @param edgeHeadId          Kanten, die als Armorstands dargestellt werden, tragen als Kopf den CustomHead aus der HDB mit der angegebenen ID.
     * @return den Visualisierer mit allen gesetzten Parametern.
     */
    public EditModeVisualizer createEditModeVisualizer(String name, @Nullable Particle particle, @Nullable Double particleDistance,
                                                       @Nullable Integer particleLimit, @Nullable Integer schedulerStartDelay,
                                                       @Nullable Integer schedulerPeriod, @Nullable Integer nodeHeadId,
                                                       @Nullable Integer edgeHeadId) {
        if (!isNameUniqueEditMode(name)) {
            return null;
        }

        EditModeVisualizer vis = DatabaseModel.getInstance().newEditModeVisualizer(name, particle, particleDistance, particleLimit,
                schedulerStartDelay, schedulerPeriod, nodeHeadId, edgeHeadId);
        if (vis == null) {
            return null;
        }
        editVisualizerMap.put(vis.getDatabaseId(), vis);
        return vis;
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
        return getPathVisualizers()
                .filter(pathVisualizer -> pathVisualizer.getName().equals(name))
                .findAny().orElse(null);
    }

    public @Nullable
    PathVisualizer getPathVisualizer(int databaseId) {
        return getPathVisualizers()
                .filter(pathVisualizer -> pathVisualizer.getDatabaseId() == databaseId)
                .findAny().orElse(null);
    }

    public @Nullable
    EditModeVisualizer getEditVisualizer(String name) {
        return getEditModeVisualizer()
                .filter(editModeVisualizer -> editModeVisualizer.getName().equals(name))
                .findAny().orElse(null);
    }

    public @Nullable
    EditModeVisualizer getEditVisualizer(int databaseId) {
        return getEditModeVisualizer()
                .filter(editModeVisualizer -> editModeVisualizer.getDatabaseId() == databaseId)
                .findAny().orElse(null);
    }

    public Stream<PathVisualizer> getPathVisualizers() {
        return pathVisualizerMap.values().stream();
    }

    public Stream<EditModeVisualizer> getEditModeVisualizer() {
        return editVisualizerMap.values().stream();
    }
}
