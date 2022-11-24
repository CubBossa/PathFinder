package de.cubbossa.pathfinder.util;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VectorUtilsTest {

	@Test
	void distancePointToLine1() {

		Assertions.assertEquals(3, VectorUtils.distancePointToLine(
				new Vector(2.78123, 3, 0),
				new Vector(0, 0, 0),
				new Vector(1, 0, 0)
		), 0.000001);
	}

	@Test
	void distancePointToLine2() {
		Assertions.assertEquals(0, VectorUtils.distancePointToLine(
				new Vector(2.78123, 0, 0),
				new Vector(0, 0, 0),
				new Vector(1, 0, 0)
		), 0.000001);
	}

	@Test
	void distancePointToLine3() {
		Assertions.assertEquals(12.1293, VectorUtils.distancePointToLine(
				new Vector(27, 12.1293, 0),
				new Vector(0, 0, 0),
				new Vector(29, 0, 0)
		), 0.000001);
	}
}