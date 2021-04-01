package pathfinder.visualisation;

import lombok.Getter;
import org.bukkit.Particle;

/**
 * stil um den editmode anzuzeigen
 * partikeltyp, Nodekopf, Edgekopf
 *
 */
@Getter
public class EditModeVisualizer {

    Particle particle;
    double particleDistance;

    int schedulerStartDelay;
    int schedulerPeriod;

    int nodeHeadId;
    int edgeHeadId;



}
