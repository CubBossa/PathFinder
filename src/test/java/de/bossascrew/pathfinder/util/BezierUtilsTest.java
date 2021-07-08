package de.bossascrew.pathfinder.util;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BezierUtilsTest extends TestCase {

    public void testGetBezierCurve() {
        List<Vector> vectorList = BezierUtils.getBezierCurve(10, new Vector(0, 0, 0), new Vector(10, 10, 10));
        for (Vector v : vectorList) {
            //System.out.println(v);
        }
    }

    public void testTestGetBezierCurve() {
        List<Vector> vectorList = BezierUtils.getBezierCurve(10, new Vector(0, 0, 0), new Vector(0, 10, 0), new Vector(20, 0, 0));
        for (Vector v : vectorList) {
            //System.out.println(v);
        }
    }

    public void testTestGetBezierCurve1() {
        StopWatch sw = new StopWatch();
        sw.start();
        List<Vector> vectorList = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            vectorList.addAll(BezierUtils.getBezierCurve(100, new Vector(123, 124198, -1248), new Vector(-12309, 123, 1238),
                    new Vector(120, -1110, 1921), new Vector(1238, 1239, -1000)));
        }
        System.out.println(vectorList.size());
        sw.stop();
        System.out.println(sw.getTime() + "ms");
    }

    public void testGetEvenSpacing() {

        /*List<Vector> bezier = BezierUtils.getBezierCurve(20, new Vector(0, 0, 0), new Vector(0, 0, 10),
                new Vector(0, 0, 0));

        List<Vector> smoothed = BezierUtils.getEvenSpacing2(bezier, 2);

        for(int i = 0; i < smoothed.size() - 1; i++) {
            System.out.println("A " + bezier.get(i) + " DISTANCE: " + bezier.get(i).distance(bezier.get(i + 1)));
            System.out.println("B " + smoothed.get(i) + " DISTANCE: " + smoothed.get(i).distance(smoothed.get(i + 1)));
        }*/
        Vector a = new Vector(0, 0, 0);
        Vector b = new Vector(10, 0, 0);
        Vector c = new Vector(10, 10, 0);

        BezierUtils.getEvenSpacing2(Lists.newArrayList(a, b, c), 15).forEach(System.out::println);
    }
}