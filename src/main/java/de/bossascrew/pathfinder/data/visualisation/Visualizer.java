package de.bossascrew.pathfinder.data.visualisation;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.Particle;

import javax.annotation.Nullable;

@Getter
@Setter
public abstract class Visualizer<T extends Visualizer> {

    private String name;
    private final int databaseId;
    protected T parent;

    private Particle particle = null;
    private Double particleDistance = null;
    private Integer particleLimit = null;

    public Visualizer(int databaseId, String name) {
        this.databaseId = databaseId;
        this.name = name;
    }

    public void setParent(Visualizer<T> parent) {
        if (parent.hasParent(this)) {
            return;
        }
        this.parent = (T) parent;
    }

    public boolean hasParent(Visualizer<T> toCheck) {
        if (parent == null) {
            return false;
        }
        if (parent.getDatabaseId() != toCheck.getDatabaseId()) {
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
    Integer getParticleLimit() {
        if (particleLimit == null) {
            if (parent == null) {
                return null;
            }
            return parent.getParticleLimit();
        }
        return particleLimit;
    }
}
