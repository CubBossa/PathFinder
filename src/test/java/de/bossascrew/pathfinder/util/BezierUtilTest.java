package de.bossascrew.pathfinder.util;

import junit.framework.TestCase;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BezierUtilTest extends TestCase {

    public void testGetBezierCurve() {
        List<Vector> vectorList = BezierUtil.getBezierCurve(10, new Vector(0, 0, 0), new Vector(10, 10, 10));
        for (Vector v : vectorList) {
            //System.out.println(v);
        }
    }

    public void testTestGetBezierCurve() {
        List<Vector> vectorList = BezierUtil.getBezierCurve(10, new Vector(0, 0, 0), new Vector(0, 10, 0), new Vector(20, 0, 0));
        for (Vector v : vectorList) {
            //System.out.println(v);
        }
    }

    public void testTestGetBezierCurve1() {
        StopWatch sw = new StopWatch();
        sw.start();
        List<Vector> vectorList = new ArrayList<>();
        for(int i = 0; i < 10000; i++) {
            vectorList.addAll(BezierUtil.getBezierCurve(100, new Vector(123, 124198, -1248), new Vector(-12309, 123, 1238),
                    new Vector(120, -1110, 1921), new Vector(1238, 1239, -1000)));
        }
        System.out.println(vectorList.size());
        sw.stop();
        System.out.println(sw.getTime() + "ms");
    }
}