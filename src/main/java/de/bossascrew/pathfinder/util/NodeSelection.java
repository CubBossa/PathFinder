package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.core.node.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class NodeSelection extends ArrayList<Node> implements Collection<Node> {

	public NodeSelection(Node... nodes) {
		super(Arrays.asList(nodes));
	}

	public NodeSelection(Collection<Node> other) {
		super(other);
	}
}
