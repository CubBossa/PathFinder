package de.bossascrew.pathfinder.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

	public String replaceSpaces(String string) {
		return string.replaceAll("_", " ");
	}

	public String replaceBlanks(String string) {
		return string.replaceAll(" ", "_");
	}
}
