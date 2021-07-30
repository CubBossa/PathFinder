package de.bossascrew.pathfinder.data.visualisation;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * Visualisiert einen Pfad aus Locations mit Partikeln
 */
@Getter
@Setter
public class PathVisualizer extends Visualizer<PathVisualizer> {

    private Integer particleSteps = null;

    public PathVisualizer(int databaseId, String name, @Nullable Integer parentId) {
        super(databaseId, name, parentId);
    }

    public Integer getParticleSteps() {
        if (particleSteps == null) {
            if (parent == null) {
                try {
                    throw new VisualizerParentException();
                } catch (VisualizerParentException e) {
                    e.printStackTrace();
                }
            }
            return parent.getParticleSteps();
        }
        return particleSteps;
    }

    public @Nullable
    Integer getUnsafeParticleSteps() {
        return particleSteps;
    }

    public void setAndSaveParticleSteps(int particleSteps) {
        if (particleSteps < 1) {
            particleSteps = 1;
        }
        if (particleSteps > 100) {
            particleSteps = 100;
        }
        this.particleSteps = particleSteps;
        saveData();
        callParticleStepsSubscribers(this);
    }

    private void callParticleStepsSubscribers(PathVisualizer vis) {
        vis.updateParticle.perform(null);
        for (PathVisualizer child : children) {
            if (child.getUnsafeParticle() != null) {
                continue;
            }
            child.updateParticle.perform(null);
            vis.callParticleStepsSubscribers(child);
        }
    }

    public void saveData() {
        PluginUtils.getInstance().runAsync(() -> DatabaseModel.getInstance().updatePathVisualizer(this));
    }
}
