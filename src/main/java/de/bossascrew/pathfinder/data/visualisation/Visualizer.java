package de.bossascrew.pathfinder.data.visualisation;


import de.bossascrew.pathfinder.util.SubscribtionHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Particle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

@Getter
@Setter
public abstract class Visualizer<T extends Visualizer> {

    private String name;
    private final int databaseId;
    private @Nullable
    Integer parentId;
    protected @Nullable
    T parent = null;
    protected Collection<T> children;

    private Particle particle = null;
    private Double particleDistance = null;
    private Integer particleLimit = null;
    private Integer schedulerPeriod = null;

    private final SubscribtionHandler<Integer, Integer> updateParticle;

    public Visualizer(int databaseId, String name, @Nullable Integer parentId) {
        this.databaseId = databaseId;
        this.name = name;
        this.parentId = parentId;
        this.children = new ArrayList<>();

        updateParticle = new SubscribtionHandler<>();
    }

    public void setParent(@Nullable Visualizer<T> parent) {
        if (parent == null) {
            this.parent = null;
            return;
        }
        if (parent.hasParent(this)) {
            return;
        }
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = (T) parent;
        this.parentId = parent.getDatabaseId();
        if (parent != null) {
            parent.children.add((T) this);
        }
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

    public Particle getParticle() {
        if (particle == null) {
            if (parent == null) {
                try {
                    throw new VisualizerParentException();
                } catch (VisualizerParentException e) {
                    e.printStackTrace();
                }
            }
            return parent.getParticle();
        }
        return particle;
    }

    public @Nullable
    Particle getUnsafeParticle() {
        return particle;
    }

    public Double getParticleDistance() {
        if (particleDistance == null) {
            if (parent == null) {
                try {
                    throw new VisualizerParentException();
                } catch (VisualizerParentException e) {
                    e.printStackTrace();
                }
            }
            return parent.getParticleDistance();
        }
        return particleDistance;
    }

    public @Nullable
    Double getUnsafeParticleDistance() {
        return particleDistance;
    }

    public Integer getParticleLimit() {
        if (particleLimit == null) {
            if (parent == null) {
                try {
                    throw new VisualizerParentException();
                } catch (VisualizerParentException e) {
                    e.printStackTrace();
                }
            }
            return parent.getParticleLimit();
        }
        return particleLimit;
    }

    public @Nullable
    Integer getUnsafeParticleLimit() {
        return particleLimit;
    }

    public Integer getSchedulerPeriod() {
        if (schedulerPeriod == null) {
            if (parent == null) {
                try {
                    throw new VisualizerParentException();
                } catch (VisualizerParentException e) {
                    e.printStackTrace();
                }
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
        return parent == null ? (parentId == null ? null : parentId) : (Integer) parent.getDatabaseId();
    }

    public void setAndSaveName(String name) {
        this.name = name;
        saveData();
    }

    public void setAndSaveParticle(@Nullable Particle particle) {
        this.particle = particle;
        updateParticle.perform();
        saveData();
        callParticleSubscribers(this);
    }

    public void setAndSaveParticleLimit(@Nullable Integer particleLimit) {
        this.particleLimit = particleLimit;
        updateParticle.perform();
        saveData();
        callParticleLimitSubscribers(this);
    }

    public void setAndSaveParticleDistance(@Nullable Double particleDistance) {
        this.particleDistance = particleDistance;
        updateParticle.perform();
        saveData();
        callParticleDistanceSubscribers(this);
    }

    public void setAndSaveSchedulerPeriod(@Nullable Integer schedulerPeriod) {
        this.schedulerPeriod = schedulerPeriod;
        updateParticle.perform();
        saveData();
        callSchedulerPeriodSubscribers(this);
    }

    private <A> void callParticleSubscribers(Visualizer vis) {
        vis.updateParticle.perform(null);
        for (Visualizer child : children) {
            if (child.getUnsafeParticle() != null) {
                continue;
            }
            child.updateParticle.perform(null);
            vis.callParticleSubscribers(child);
        }
    }

    private void callParticleDistanceSubscribers(Visualizer vis) {
        vis.updateParticle.perform(null);
        for (Visualizer child : children) {
            if (child.getUnsafeParticleDistance() != null) {
                continue;
            }
            child.updateParticle.perform(null);
            vis.callParticleDistanceSubscribers(child);
        }
    }

    private void callParticleLimitSubscribers(Visualizer vis) {
        vis.updateParticle.perform(null);
        for (Visualizer child : children) {
            if (child.getUnsafeParticleLimit() != null) {
                continue;
            }
            child.updateParticle.perform(null);
            vis.callParticleLimitSubscribers(child);
        }
    }

    private void callSchedulerPeriodSubscribers(Visualizer vis) {
        vis.updateParticle.perform(null);
        for (Visualizer child : children) {
            if (child.getUnsafeSchedulerPeriod() != null) {
                continue;
            }
            child.updateParticle.perform(null);
            vis.callSchedulerPeriodSubscribers(child);
        }
    }

    public abstract void saveData();
}
