package de.bossascrew.pathfinder.data.visualisation;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.SqlStorage;
import de.bossascrew.pathfinder.util.SubscribtionHandler;
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

    private final SubscribtionHandler<Integer, Integer> nodeHeadSubscribers;
    private final SubscribtionHandler<Integer, Integer> edgeHeadSubscribers;

    public EditModeVisualizer(int databaseId, String name, @Nullable Integer parentId) {
        super(databaseId, name, parentId);

        nodeHeadSubscribers = new SubscribtionHandler<>();
        edgeHeadSubscribers = new SubscribtionHandler<>();
    }

    public Integer getNodeHeadId() {
        if (nodeHeadId == null) {
            if (parent == null) {
                try {
                    throw new VisualizerParentException();
                } catch (VisualizerParentException e) {
                    e.printStackTrace();
                }
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
                try {
                    throw new VisualizerParentException();
                } catch (VisualizerParentException e) {
                    e.printStackTrace();
                }
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
        callNodeHeadSubscriber(this);
    }

    public void callNodeHeadSubscriber(EditModeVisualizer visualizer) {
        visualizer.nodeHeadSubscribers.perform(getNodeHeadId());
        for (EditModeVisualizer child : children) {
            if (child.getUnsafeNodeHeadId() != null) {
                continue;
            }
            visualizer.callNodeHeadSubscriber(child);
        }
    }

    public void setAndSaveEdgeHeadId(@Nullable Integer edgeHeadId) {
        this.edgeHeadId = edgeHeadId;
        saveData();
        callEdgeHeadSubscriber(this);
    }

    public void callEdgeHeadSubscriber(EditModeVisualizer visualizer) {
        visualizer.edgeHeadSubscribers.perform(getEdgeHeadId());
        for (EditModeVisualizer child : children) {
            if (child.getUnsafeEdgeHeadId() != null) {
                continue;
            }
            visualizer.callEdgeHeadSubscriber(child);
        }
    }

    public void saveData() {
        PluginUtils.getInstance().runAsync(() -> SqlStorage.getInstance().updateEditModeVisualizer(this));
    }
}
