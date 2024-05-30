package de.cubbossa.pathfinder.editmode

import de.cubbossa.cliententities.lib.packetevents.api.PacketEvents
import de.cubbossa.cliententities.lib.packetevents.impl.factory.spigot.SpigotPacketEventsBuilder
import de.cubbossa.menuframework.GUIHandler
import de.cubbossa.pathfinder.*
import de.cubbossa.pathfinder.misc.NamespacedKey
import org.pf4j.Extension

@Extension(points = [PathFinderExtension::class])
class RoadMapEditorExtension : PathFinderExtensionBase(), PathFinderExtension {
    override fun getKey(): NamespacedKey {
        return AbstractPathFinder.pathfinder("group-editor")
    }

    override fun onLoad(pathPlugin: PathFinder) {
        if (pathPlugin is BukkitPathFinder) {
            PacketEvents.setAPI(
                SpigotPacketEventsBuilder
                    .build(pathPlugin.javaPlugin)
            )
            com.github.retrooper.packetevents.PacketEvents.setAPI(
                io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder.build(
                    pathPlugin.javaPlugin
                )
            )
            com.github.retrooper.packetevents.PacketEvents.getAPI().settings
                .checkForUpdates(true)
                .bStats(true)
            com.github.retrooper.packetevents.PacketEvents.getAPI().load()
        }
    }

    override fun onEnable(plugin: PathFinder) {
        com.github.retrooper.packetevents.PacketEvents.getAPI().init()

        GUIHandler(PathFinderPlugin.getInstance())
        GUIHandler.getInstance().enable()
    }

    override fun onDisable(plugin: PathFinder) {
        GUIHandler.getInstance().disable()

        com.github.retrooper.packetevents.PacketEvents.getAPI().terminate()
    }
}
