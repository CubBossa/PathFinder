package de.bossascrew.pathfinder.data.visualisation;

public class VisualizerParentException extends Exception {

    public VisualizerParentException() {
        super("Fehler bei Parent-Child Hirarchie eines Visualizers.");
    }

    public VisualizerParentException(String message) {
        super(message);
    }
}
