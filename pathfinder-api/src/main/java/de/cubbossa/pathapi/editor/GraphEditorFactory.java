package de.cubbossa.pathapi.editor;

import de.cubbossa.pathapi.group.NodeGroup;
import java.util.function.Function;
import org.pf4j.ExtensionPoint;

/**
 * ExtensionPoint that allows to introduce
 */
public interface GraphEditorFactory extends ExtensionPoint {

  GraphEditor<?> createGraphEditor(NodeGroup scope);
}
