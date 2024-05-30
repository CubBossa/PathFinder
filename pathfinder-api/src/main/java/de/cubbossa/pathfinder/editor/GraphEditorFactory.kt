package de.cubbossa.pathfinder.editor

import de.cubbossa.pathfinder.group.NodeGroup
import org.pf4j.ExtensionPoint

/**
 * ExtensionPoint that allows to introduce a custom editor for the graph.
 */
interface GraphEditorFactory : ExtensionPoint {
    /**
     * Create a new GraphEditor instance.
     * @param scope The NodeGroup to edit with the provided editor.
     * @return The GraphEditor instance.
     */
    fun createGraphEditor(scope: NodeGroup): GraphEditor<*>
}
