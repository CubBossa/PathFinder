package pathfinder.handler;

import lombok.Getter;

/**
 * Managed alle Visualizer Profile, mit denen man einen Pfad darstellen kann. Roadmaps und die Art, wie ein Pfad dargestellt wird
 * sind unabhängig voneinander. Man kann also verschiedene Darstellungsprofile mit verschiedenen Partikeln anlegen und sagen, welche Roadmap mit welchem
 * Visualizer angezeigt werden soll. So könnte man Spielern später sogar die Möglichkeit geben, den Partikel-Style zu ändern.
 */
public class VisualizerHandler {

    @Getter
    private static VisualizerHandler instance;

    public VisualizerHandler() {

    }

}
