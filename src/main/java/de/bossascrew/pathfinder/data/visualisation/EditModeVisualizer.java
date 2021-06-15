package de.bossascrew.pathfinder.data.visualisation;

import lombok.Getter;
import lombok.Setter;

/**
 * Definiert, wie der Editmode dargestellt wird. Ist eine Variable auf null gesetzt, wird der Default geladen.
 */
@Getter
@Setter
public class EditModeVisualizer extends Visualizer<EditModeVisualizer> {

    private Integer schedulerStartDelay = null;
    private Integer schedulerPeriod = null;

    private Integer nodeHeadId = null;
    private Integer edgeHeadId = null;

    public EditModeVisualizer(int databaseId, String name) {
        super(databaseId, name);
    }

    public Integer getSchedulerStartDelay() {
        if (schedulerStartDelay == null) {
            if (parent == null) {
                return null;
            }
            return parent.getSchedulerStartDelay();
        }
        return schedulerStartDelay;
    }

    public Integer getSchedulerPeriod() {
        if (schedulerPeriod == null) {
            if (parent == null) {
                return null;
            }
            return parent.getSchedulerPeriod();
        }
        return schedulerPeriod;
    }

    public Integer getNodeHeadId() {
        if (nodeHeadId == null) {
            if (parent == null) {
                return null;
            }
            return parent.getNodeHeadId();
        }
        return nodeHeadId;
    }

    public Integer getEdgeHeadId() {
        if (edgeHeadId == null) {
            if (parent == null) {
                return null;
            }
            return parent.getEdgeHeadId();
        }
        return edgeHeadId;
    }
}
