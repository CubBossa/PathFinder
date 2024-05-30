package de.cubbossa.pathfinder.editmode

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathFinderPlugin
import de.cubbossa.pathfinder.editmode.renderer.*
import de.cubbossa.pathfinder.editor.GraphEditor
import de.cubbossa.pathfinder.editor.GraphEditorFactory
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.util.Version
import org.bukkit.entity.Player
import org.pf4j.Extension

@Extension(points = [GraphEditorFactory::class])
class DefaultGraphEditorFactory : GraphEditorFactory {
    override fun createGraphEditor(group: NodeGroup): GraphEditor<Player> {
        val pathFinder = PathFinder.get()
        val editor = DefaultGraphEditor(group)
        val plugin = PathFinderPlugin.getInstance()

        editor.renderers.add(ParticleEdgeRenderer(pathFinder.configuration.editMode))
        try {
            if (Version(
                    PathFinderPlugin.getInstance().server.bukkitVersion.split("-".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                ).compareTo(Version("1.19.4")) >= 0
            ) {
                editor.addRenderer(NodeGroupListRenderer(plugin, 15.0, 8.0))
                editor.addRenderer(NodeEntityRenderer(plugin))
                editor.addRenderer(EdgeEntityRenderer(plugin))
            } else {
                editor.addRenderer(NodeArmorStandRenderer(plugin))
                editor.addRenderer(EdgeArmorStandRenderer(plugin))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return editor
    }
}
