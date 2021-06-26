package de.bossascrew.pathfinder.util;

import lombok.experimental.UtilityClass;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BezierUtils {

    /**
     * Berechnet eine Liste aus Vektoren, die auf der Gerade zwischen den zwei angegebenen Punkten liegen.
     *
     * @param distance Mit wie viel Abstand die Punkte auf der Geraden liegen sollen.
     * @param pointA   Der Startpunkt der Gerade
     * @param pointB   Der Schlusspunkt der Gerade
     * @return eine Liste aus Vektoren, die auf der Gerade liegen
     */
    public List<Vector> getBezierCurveDistanced(double distance, Vector pointA, Vector pointB) {
        int steps = (int) (pointA.distance(pointB) / distance);
        return getBezierCurve(steps, pointA, pointB);
    }

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
     * Berechnet eine Liste aus Vektoren, die auf der Bezierkurve zwischen Punkt A und B liegen. Es wird nur ein Kontrollpunkt
     * benötigt, es findet also eine vereinfachte Bezierberechnung statt.
     *
     * @param steps              Wie viele Punkte auf der Kurve berechnet werden sollen
     * @param pointA             Der Startpunkt der Kurve
     * @param pointB             Der Schlusspunkt der Kurve
     * @param sharedTangentPoint der Kontrollpunkt für beide Punkte.
     * @return eine Liste aus Vektoren, die auf der Kurve liegen
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
     * Bisher sehr ungenaue Annäherung, exakte Punkteverteilung mit getEvenSpacing()
     * Mathematisch gesehen wird die Menge der Punkte aus dem Punktabstand für die Länge der Gerade AB berechnet und dann auf die Kurve angewandt.
     */
    public List<Vector> getBezierCurveDistanced(double distance, Vector pointA, Vector pointB, Vector tangentA, Vector tangentB) {
        return getBezierCurve((int) (pointA.distance(pointB) / distance), pointA, pointB, tangentA, tangentB); //TODO
    }

    /**
     * Gibt eine Liste aus Vektoren, die auf der Bezierkurve zwischen den Punkten A und B mit den zugehörigen Kontrollpunkten liegen, zurück.
     *
     * @param steps    Wie viele Punkte auf der Kurve berechnet werden sollen
     * @param pointA   Der Startpunkt der Kurve
     * @param pointB   Der Schlusspunkt der Kurve
     * @param tangentA Der Kontrollpunkt für den Startpunkt der Kurve (als Punkt, nicht als Vektor)
     * @param tangentB Der Kontrollpunkt für den Schlusspunkt der Kurve (als Punkt, nicht als Vektor)
     * @return eine Liste aus Vektoren, die auf der Kurve liegen
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

    //TODO kommentar
    public List<Vector> getEvenSpacing(List<Vector> input, double distance) {

        List<Vector> ret = new ArrayList<>();
        ret.add(input.get(0));
        double left = 0;

        for (int i = 0; i < input.size() - 1; i++) { //TODO fall behandlung, falls alle größeren abstand haben und left immer negativer werden.
            Vector a = input.get(i);
            Vector b = input.get(i + 1);
            if (left > distance) {
                left = left - distance;
                continue;
            }
            left += distance - a.distance(b);
            b.clone().setX(a.getX()).setY(a.getY()).setZ(a.getZ()).add(b.clone().subtract(a).normalize().multiply(distance));
            ret.add(b);
        }
        return ret;
    }
}
