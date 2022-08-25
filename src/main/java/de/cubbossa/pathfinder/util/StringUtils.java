package de.cubbossa.pathfinder.util;

import java.awt.*;
import java.util.Random;

public class StringUtils {

	public static String getRandHexString() {
		return "<#" + Integer.toHexString(Color.getHSBColor(new Random().nextInt(360) / 360.f, 73 / 100.f, 96 / 100.f).getRGB()).substring(2) + ">";
	}

	public static String capizalize(String in) {
		if (in.length() < 1) {
			throw new IllegalArgumentException("String must not be empty");
		}
		return in.substring(0, 1).toUpperCase() + in.substring(1);
	}
}
