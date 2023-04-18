package de.cubbossa.pathfinder.api.editor;

import de.cubbossa.pathfinder.api.group.NodeGroup;

import java.util.function.Function;

public interface NodeGroupEditorFactory extends Function<NodeGroup, NodeGroupEditor<?>> {
}
