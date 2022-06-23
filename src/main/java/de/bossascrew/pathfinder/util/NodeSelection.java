package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.node.Node;

import java.util.ArrayList;
import java.util.Collection;

public class NodeSelection extends ArrayList<Node> implements Collection<Node> {

	public NodeSelection() {

	}

	public NodeSelection(Collection<Node> other) {
		super(other);
	}
}
