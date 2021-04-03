package de.bossascrew.pathfinder.visualisation;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Particle;

/**
 * stil um den editmode anzuzeigen
 * partikeltyp, Nodekopf, Edgekopf
 *
 */
@Getter
@Setter
public class EditModeVisualizer {

    private String name;
    private final int databaseId;

    private Particle particle = Particle.FLAME;
    private double particleDistance = 50;
    private int particleLimit = 10000;

    private int schedulerStartDelay = 0;
    private int schedulerPeriod = 50;

    private int nodeHeadId = 8621;
    private int edgeHeadId = 8619;

    public EditModeVisualizer(int databaseId, String name) {
        this.databaseId = databaseId;
        this.name = name;
    }

    public double getParticleDistanceSquared() {
        return Math.pow(particleDistance, 2);
    }
}
