package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.core.node.Node;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.*;

public class NodeSelection extends ArrayList<Node> implements Collection<Node> {

	public record Meta(String selector, Map<String, String> arguments) {
	}

	@Getter
	@Setter
	private @Nullable Meta meta = null;

	public NodeSelection(Node... nodes) {
		super(Arrays.asList(nodes));
	}

	public NodeSelection(Collection<Node> other) {
		super(other);
	}
}
