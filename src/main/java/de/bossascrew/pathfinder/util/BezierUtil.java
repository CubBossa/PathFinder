package de.bossascrew.pathfinder.util;

import lombok.experimental.UtilityClass;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BezierUtil {

    /**
     * Berechnet eine Liste aus Vektoren, die auf der Gerade zwischen den zwei angegebenen Punkten liegen.
     *
     * @param steps  Wie viele Punkte auf der Kurve berechnet werden sollen
     * @param pointA Der Startpunkt der Gerade
     * @param pointB Der Schlusspunkt der Gerade
     * @return eine Liste aus Vektoren, die auf der Gerade liegen
     */
    public List<Vector> getBezierCurve(int steps, Vector pointA, Vector pointB) {
        List<Vector> ret = new ArrayList<>();

        Vector AB = pointB.clone().subtract(pointA);

        for (int step = 0; step < steps; step++) {
            double t = (double) step / steps;
            ret.add(pointA.clone().add(AB.clone().multiply(t)));
        }
        return ret;
    }

    /**
     * Berechnet eine Liste aus Vektoren, die auf der Tangentenkurve zwischen Punkt A und B liegen. Es wird nur ein Tangentenpunkt
     * entgegen genommen, es findet also eine vereinfachte Bezierberechnung statt.
     *
     * @param steps              Wie viele Punkte auf der Kurve berechnet werden sollen
     * @param pointA             Der Startpunkt der Kurve
     * @param pointB             Der Schlusspunkt der Kurve
     * @param sharedTangentPoint der Tangentenpunkt für beide Punkte.
     * @return eine Liste aus Vektoren, die auf der Tangente liegen
     * @see <a href="https://javascript.info/bezier-curve">bezier curve</a>
     */
    public List<Vector> getBezierCurve(int steps, Vector pointA, Vector pointB, Vector sharedTangentPoint) {
        List<Vector> ret = new ArrayList<>();

        for (int step = 0; step < steps; step++) {
            double t = (double) step / steps;

            ret.add(pointA.clone().multiply(Math.pow(1 - t, 2))
                    .add(sharedTangentPoint.clone().multiply(2 * (1 - t) * t))
                    .add(pointB.clone().multiply(Math.pow(t, 2))));
        }
        return ret;

    }

    /**
     * Gibt eine Liste aus Vektoren, die auf der Bezierkurve zwischen den Punkten A und B mit den zugehörigen Tangentengreifern liegen, zurück.
     *
     * @param steps    Wie viele Punkte auf der Kurve berechnet werden sollen
     * @param pointA   Der Startpunkt der Kurve
     * @param pointB   Der Schlusspunkt der Kurve
     * @param tangentA Der Tangentengreifer für den Startpunkt der Kurve (als Punkt, nicht als Vektor)
     * @param tangentB Der Tangentengreifer für den Schlusspunkt der Kurve (als Punkt, nicht als Vektor)
     * @return eine Liste aus Vektoren, die auf der Tangente liegen
     * @see <a href="https://javascript.info/bezier-curve">bezier curve</a>
     */
    public List<Vector> getBezierCurve(int steps, Vector pointA, Vector pointB, Vector tangentA, Vector tangentB) {
        List<Vector> ret = new ArrayList<>();

        for (int step = 0; step < steps; step++) {
            double t = (double) step / steps;

            ret.add(pointA.clone().multiply(Math.pow(1 - t, 3))
                    .add(tangentA.clone().multiply(Math.pow(1 - t, 2) * t * 3))
                    .add(tangentB.clone().multiply(Math.pow(t, 2) * (1 - t) * 3))
                    .add(pointB.clone().multiply(Math.pow(t, 3))));
        }
        return ret;
    }
}
