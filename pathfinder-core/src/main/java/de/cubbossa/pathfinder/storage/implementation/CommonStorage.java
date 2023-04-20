package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.*;
import de.cubbossa.pathapi.storage.NodeDataStorage;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathfinder.storage.Storage;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class CommonStorage implements StorageImplementation, WaypointDataStorage {

	final NodeTypeRegistry nodeTypeRegistry;
	@Getter
	@Setter
	private @Nullable Logger logger;

	@Override
	public <N extends Node<N>> N createAndLoadNode(NodeType<N> type, Location location) {
		debug(" > Storage Implementation: 'createAndLoadNode(" + type.getKey() + ", " + location + ")'");
		N node = type.createAndLoadNode(new NodeDataStorage.Context(location));
		saveNodeType(node.getNodeId(), type);
		return node;
	}

	@Override
	public <N extends Node<N>> Optional<N> loadNode(UUID id) {
		debug(" > Storage Implementation: 'loadNode(" + id + ")'");
		Optional<NodeType<N>> type = loadNodeType(id);
		if (type.isPresent()) {
			return type.get().loadNode(id);
		}
		throw new IllegalStateException("No type found for node with UUID '" + id + "'.");
	}

	@Override
	public Collection<Node<?>> loadNodes() {
		debug(" > Storage Implementation: 'loadNodes()'");
		return nodeTypeRegistry.getTypes().stream()
				.flatMap(nodeType -> nodeType.loadAllNodes().stream())
				.collect(Collectors.toList());
	}

	@Override
	public Collection<Node<?>> loadNodes(Collection<UUID> ids) {
		debug(" > Storage Implementation: 'loadNodes(" + ids.stream()
				.map(UUID::toString).collect(Collectors.joining(", ")) + ")'");
		return nodeTypeRegistry.getTypes().stream()
				.flatMap(nodeType -> nodeType.loadNodes(ids).stream())
				.collect(Collectors.toList());
	}

	@Override
	public void saveNode(Node<?> node) {
		debug(" > Storage Implementation: 'saveNode(" + node.getNodeId() + ")'");
		saveNodeTyped(node);
	}

	private <N extends Node<N>> void saveNodeTyped(Node<?> node) {
		NodeType<N> type = (NodeType<N>) node.getType();
		N before = type.loadNode(node.getNodeId()).orElseThrow();
		type.saveNode((N) node);

		if (before instanceof Groupable<?> gBefore && node instanceof Groupable<?> gAfter) {
			Storage.ComparisonResult<NodeGroup> cmp = Storage.ComparisonResult.compare(gBefore.getGroups(), gAfter.getGroups());
			cmp.toInsertIfPresent(nodeGroups -> assignToGroups(nodeGroups, List.of(node.getNodeId())));
			cmp.toDeleteIfPresent(nodeGroups -> unassignFromGroups(nodeGroups, List.of(node.getNodeId())));
		}
		Storage.ComparisonResult<Edge> cmp = Storage.ComparisonResult.compare(before.getEdges(), node.getEdges());
		cmp.toInsertIfPresent(edges -> {
			for (Edge edge : edges) {
				createAndLoadEdge(edge.getStart(), edge.getEnd(), edge.getWeight());
			}
		});
		cmp.toDeleteIfPresent(edges -> edges.forEach(this::deleteEdge));
	}

	protected void debug(String message) {
	  if (logger != null) logger.log(Level.INFO, message);
	}

	@Override
	public void deleteNodes(Collection<Node<?>> nodes) {
		Map<UUID, NodeType<?>> types = loadNodeTypes(nodes.stream().map(Node::getNodeId).toList());
		for (Node<?> node : nodes) {
			deleteNode(node, types.get(node.getNodeId()));
		}
	}

	void deleteNode(Node node, NodeType type) {
		type.deleteNode(node);
	}
}
