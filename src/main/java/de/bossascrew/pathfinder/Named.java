package de.bossascrew.pathfinder;

import net.kyori.adventure.text.Component;

public interface Named {

	String getNameFormat();

	void setNameFormat(String name);

	Component getDisplayName();
}
