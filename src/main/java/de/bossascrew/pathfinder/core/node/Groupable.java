package de.bossascrew.pathfinder.core.node;

import java.util.Collection;

public interface Groupable {

	Collection<NodeGroup> getGroups();

	void addGroup(NodeGroup group);

	void removeGroup(NodeGroup group);

	void clearGroups();
}
