package de.cubbossa.pathapi.editor;

import de.cubbossa.pathapi.group.NodeGroup;

import java.util.function.Function;

public interface NodeGroupEditorFactory extends Function<NodeGroup, NodeGroupEditor<?>> {
}
