package de.bossascrew.pathfinder.data.visualisation;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * Definiert, wie der Editmode dargestellt wird. Ist eine Variable auf null gesetzt, wird der Default geladen.
 */
@Getter
@Setter
public class EditModeVisualizer extends Visualizer<EditModeVisualizer> {

    private Integer nodeHeadId = null;
    private Integer edgeHeadId = null;

    public EditModeVisualizer(int databaseId, String name, @Nullable Integer parentId) {
        super(databaseId, name, parentId);
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

    public @Nullable
    Integer getUnsafeNodeHeadId() {
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

    public @Nullable
    Integer getUnsafeEdgeHeadId() {
        return edgeHeadId;
    }

    public void setAndSaveNodeHeadId(@Nullable Integer nodeHeadId) {
        this.nodeHeadId = nodeHeadId;
        saveData();
    }

    public void setAndSaveEdgeHeadId(@Nullable Integer edgeHeadId) {
        this.edgeHeadId = edgeHeadId;
        saveData();
    }

    public void saveData() {
        PluginUtils.getInstance().runAsync(() -> DatabaseModel.getInstance().updateEditModeVisualizer(this));
    }
}
