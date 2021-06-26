package de.bossascrew.pathfinder.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class VectorSpaceUtils {

    public int getRadiusStepsFromDistance(double radius, double distance) {
        return (int) (6.284 * radius / distance);
    }

    public List<Location> getEllipse(Location center, double distance, double radius, double ratio) {
        return getEllipse(center, new Vector(0, 1, 0), new Vector(0, 0, 1), getRadiusStepsFromDistance(radius, distance), radius, ratio);
    }

    public List<Location> getEllipse(Location center, int steps, double radius, double ratio) {
        return getEllipse(center, new Vector(0, 1, 0), new Vector(0, 0, 1), steps, radius, ratio);
    }

    public List<Location> getEllipse(Location center, Vector up, double distance, double radius, double ratio) {
        return getEllipse(center, up, getRadiusStepsFromDistance(radius, distance), radius, ratio);
    }

    public List<Location> getEllipse(Location center, Vector up, int steps, double radius, double ratio) {
        return getEllipse(center, up, new Vector(0, 1, 0), steps, radius, ratio);
    }

    public List<Location> getEllipse(Location center, Vector up, Vector forward, double distance, double radius, double ratio) {
        return getEllipse(center, up, forward, getRadiusStepsFromDistance(radius, distance), radius, ratio);
    }

    public List<Location> getEllipse(Location center, Vector up, Vector forward, int steps, double radius, double ratio) {
        return getEllipse(center.toVector(), up, forward, steps, radius, ratio).stream()
                .map(vector -> vector.toLocation(center.getWorld()))
                .collect(Collectors.toList());
    }

    public List<Vector> getEllipse(Vector center, double distance, double radius, double ratio) {
        return getEllipse(center, new Vector(0, 1, 0), new Vector(0, 0, 1), getRadiusStepsFromDistance(radius, distance), radius, ratio);
    }

    public List<Vector> getEllipse(Vector center, int steps, double radius, double ratio) {
        return getEllipse(center, new Vector(0, 1, 0), new Vector(0, 0, 1), steps, radius, ratio);
    }

    public List<Vector> getEllipse(Vector center, Vector up, double distance, double radius, double ratio) {
        return getEllipse(center, up, getRadiusStepsFromDistance(radius, distance), radius, ratio);
    }

    public List<Vector> getEllipse(Vector center, Vector up, int steps, double radius, double ratio) {
        return getEllipse(center, up, new Vector(0, 1, 0), steps, radius, ratio);
    }

    public List<Vector> getEllipse(Vector center, Vector up, Vector forward, double distance, double radius, double ratio) {
        return getEllipse(center, up, forward, getRadiusStepsFromDistance(radius, distance), radius, ratio);
    }

    public List<Location> getCircle(Location center, double distance, double radius) {
        return getCircle(center, new Vector(0, 1, 0), new Vector(0, 0, 1), getRadiusStepsFromDistance(radius, distance), radius);
    }

    public List<Location> getCircle(Location center, int steps, double radius) {
        return getCircle(center, new Vector(0, 1, 0), new Vector(0, 0, 1), steps, radius);
    }

    public List<Location> getCircle(Location center, Vector up, double distance, double radius) {
        return getCircle(center, up, getRadiusStepsFromDistance(radius, distance), radius);
    }

    public List<Location> getCircle(Location center, Vector up, int steps, double radius) {
        return getCircle(center, up, new Vector(0, 1, 0), steps, radius);
    }

    public List<Location> getCircle(Location center, Vector up, Vector forward, double distance, double radius) {
        return getCircle(center, up, forward, getRadiusStepsFromDistance(radius, distance), radius);
    }

    public List<Location> getCircle(Location center, Vector up, Vector forward, int steps, double radius) {
        return getEllipse(center.toVector(), up, forward, steps, radius, 1).stream()
                .map(vector -> vector.toLocation(center.getWorld()))
                .collect(Collectors.toList());
    }

    public List<Vector> getCircle(Vector center, double distance, double radius) {
        return getCircle(center, getRadiusStepsFromDistance(radius, distance), radius);
    }

    public List<Vector> getCircle(Vector center, int steps, double radius) {
        return getEllipse(center, new Vector(0, 1, 0), new Vector(0, 0, 1), steps, radius, 1);
    }

    public List<Vector> getCircle(Vector center, Vector up, double distance, double radius) {
        return getCircle(center, up, getRadiusStepsFromDistance(radius, distance), radius);
    }

    public List<Vector> getCircle(Vector center, Vector up, int steps, double radius) {
        return getEllipse(center, up, new Vector(0, 1, 0), steps, radius, 1);
    }

    public List<Vector> getCircle(Vector center, Vector up, Vector forward, double distance, double radius) {
        return getEllipse(center, up, forward, getRadiusStepsFromDistance(radius, distance), radius, 1);
    }

    public List<Vector> getEllipse(Vector center, Vector up, Vector forward, int steps, double radius, double ratio) {
        List<Vector> ret = new ArrayList<>();

        Vector a = forward.clone().normalize().multiply(radius);
        Vector b = a.clone().crossProduct(up).normalize().multiply(radius * ratio);

        for (int i = 0; i < steps; i++) {
            double degree = 6.2831 / steps * i;
            ret.add(center.clone().add(a.clone().multiply(Math.sin(degree))).add(b.clone().multiply(Math.cos(degree))));
        }
        return ret;
    }
}
