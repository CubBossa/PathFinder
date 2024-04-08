package de.cubbossa.pathfinder.editor;

import de.cubbossa.pathfinder.group.NodeGroup;
import org.pf4j.ExtensionPoint;

/**
 * ExtensionPoint that allows to introduce
 */
public interface GraphEditorFactory extends ExtensionPoint {

  GraphEditor<?> createGraphEditor(NodeGroup scope);
}
