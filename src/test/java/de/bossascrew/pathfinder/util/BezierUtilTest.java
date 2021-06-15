package de.bossascrew.pathfinder.util;

import junit.framework.TestCase;
import org.bukkit.util.Vector;

import java.util.List;

public class BezierUtilTest extends TestCase {

    public void testGetBezierCurve() {
        List<Vector> vectorList = BezierUtil.getBezierCurve(10, new Vector(0, 0, 0), new Vector(10, 10, 10));
        for (Vector v : vectorList) {
            System.out.println(v);
        }
    }

    public void testTestGetBezierCurve() {
        List<Vector> vectorList = BezierUtil.getBezierCurve(10, new Vector(0, 0, 0), new Vector(0, 10, 0), new Vector(20, 0, 0));
        for (Vector v : vectorList) {
            System.out.println(v);
        }
    }

    public void testTestGetBezierCurve1() {
        List<Vector> vectorList = BezierUtil.getBezierCurve(20, new Vector(0, 0, 0), new Vector(20, 0, 0),
                new Vector(5, -10, 0), new Vector(15, 10, 0));
        for (Vector v : vectorList) {
            System.out.println(v);
        }
    }
}