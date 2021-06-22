package de.bossascrew.pathfinder.data.visualisation;


import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Particle;
import org.bukkit.block.data.type.Bed;

import javax.annotation.Nullable;

@Getter
@Setter
public abstract class Visualizer<T extends Visualizer> {

    private String name;
    private final int databaseId;
    private Integer parentId;
    protected T parent;

    private Particle particle = null;
    private Double particleDistance = null;
    private Integer particleLimit = null;
    private Integer schedulerPeriod = null;

    public Visualizer(int databaseId, String name, @Nullable Integer parentId) {
        this.databaseId = databaseId;
        this.name = name;
        this.parentId = parentId;
    }

    public void setParent(@Nullable Visualizer<T> parent) {
        if(parent == null) {
            this.parent = null;
            return;
        }
        if (parent.hasParent(this)) {
            return;
        }
        this.parent = (T) parent;
        this.parentId = parent.getDatabaseId();
    }

    public boolean hasParent(Visualizer<T> toCheck) {
        if (parent == null) {
            return false;
        }
        if (parent.getDatabaseId() == toCheck.getDatabaseId()) {
            return true;
        }
        return parent.hasParent(toCheck);
    }

    public @Nullable
    Particle getParticle() {
        if (particle == null) {
            if (parent == null) {
                return null;
            }
            return parent.getParticle();
        }
        return particle;
    }

    public @Nullable
    Particle getUnsafeParticle() {
        return particle;
    }

    public @Nullable
    Double getParticleDistance() {
        if (particleDistance == null) {
            if (parent == null) {
                return null;
            }
            return parent.getParticleDistance();
        }
        return particleDistance;
    }

    public @Nullable
    Double getUnsafeParticleDistance() {
        return particleDistance;
    }

    public @Nullable
    Integer getParticleLimit() {
        if (particleLimit == null) {
            if (parent == null) {
                return null;
            }
            return parent.getParticleLimit();
        }
        return particleLimit;
    }

    public @Nullable
    Integer getUnsafeParticleLimit() {
        return particleLimit;
    }

    public @Nullable
    Integer getSchedulerPeriod() {
        if (schedulerPeriod == null) {
            if (parent == null) {
                return null;
            }
            return parent.getSchedulerPeriod();
        }
        return schedulerPeriod;
    }

    public @Nullable
    Integer getUnsafeSchedulerPeriod() {
        return schedulerPeriod;
    }

    public @Nullable
    Integer getParentId() {
        return parent == null ? null : parent.getDatabaseId();
    }

    public void setAndSaveName(String name) {
        this.name = name;
        saveData();
    }

    public void setAndSaveParticle(@Nullable Particle particle) {
        this.particle = particle;
        saveData();
    }

    public void setAndSaveParticleLimit(@Nullable Integer particleLimit) {
        this.particleLimit = particleLimit;
        saveData();
    }

    public void setAndSaveParticleDistance(@Nullable Double particleDistance) {
        this.particleDistance = particleDistance;
        saveData();
    }

    public void setAndSaveSchedulerPeriod(@Nullable Integer schedulerPeriod) {
        this.schedulerPeriod = schedulerPeriod;
        saveData();
    }

    public abstract void saveData();
}
