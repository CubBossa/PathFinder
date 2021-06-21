package de.bossascrew.pathfinder.data.visualisation;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Particle;

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

    public @Nullable
    Integer getParticleSteps() {
        if (particleSteps == null) {
            if (parent == null) {
                return null;
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
        this.particleSteps = particleSteps;
        saveData();
    }

    public void saveData() {
        PluginUtils.getInstance().runAsync(() -> DatabaseModel.getInstance().updatePathVisualizer(this));
    }
}
