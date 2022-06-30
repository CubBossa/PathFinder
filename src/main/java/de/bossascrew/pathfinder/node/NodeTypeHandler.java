package de.bossascrew.pathfinder.node;

import de.bossascrew.pathfinder.util.HashedRegistry;
import lombok.Getter;
import org.bukkit.NamespacedKey;

public class NodeTypeHandler {

	@Getter
	private static NodeTypeHandler instance;

	@Getter
	private final HashedRegistry<NodeType<?>> types;

	public NodeTypeHandler() {
		instance = this;
		this.types = new HashedRegistry<>();
	}

	public <T extends Node> NodeType<T> getNodeType(NamespacedKey key) {
		return (NodeType<T>) types.get(key);
	}

	public void registerNodeType(NodeType<?> type) {
		types.put(type);
	}

	public void unregisterNodeType(NodeType<?> type) {
		types.remove(type.getKey());
	}

	public void unregisterNodeType(NamespacedKey key) {
		types.remove(key);
	}
}
