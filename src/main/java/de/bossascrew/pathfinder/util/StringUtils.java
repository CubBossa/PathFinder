package de.bossascrew.pathfinder.util;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.Random;

@UtilityClass
public class StringUtils {

	public String getRandHexString() {
		return "&" + Integer.toHexString(Color.getHSBColor(new Random().nextInt(360), 73 / 100.f, 96 / 100.f).getRGB());
	}
}
