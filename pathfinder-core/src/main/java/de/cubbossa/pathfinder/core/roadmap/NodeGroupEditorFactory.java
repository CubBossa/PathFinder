package de.cubbossa.pathfinder.core.roadmap;

import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import java.util.function.Function;

public interface NodeGroupEditorFactory extends Function<NodeGroup, NodeGroupEditor> {
}
