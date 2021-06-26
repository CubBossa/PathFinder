package de.bossascrew.pathfinder.util;

import junit.framework.TestCase;
import org.bukkit.util.Vector;

public class VectorSpaceUtilsTest extends TestCase {

    public void testCircle() {

        for(Vector v : VectorSpaceUtils.getEllipse(new Vector(0, 0, 0), 1., 10, 0.05)) {
            System.out.println(v);
        }
    }
}