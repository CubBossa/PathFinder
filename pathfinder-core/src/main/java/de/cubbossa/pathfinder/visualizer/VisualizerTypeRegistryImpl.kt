package de.cubbossa.pathfinder.visualizer

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.misc.KeyedRegistry
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.util.ExtensionPoint
import de.cubbossa.pathfinder.util.HashedRegistry
import lombok.Getter

@Getter
class VisualizerTypeRegistryImpl(pathFinder: PathFinder) : VisualizerTypeRegistry {

    private val EXTENSION_POINT: ExtensionPoint<VisualizerType<*>> = ExtensionPoint(
        VisualizerType::class.java
    )

    override var defaultType: VisualizerType<PathVisualizer<*, *>>
    private val visualizerTypes = HashedRegistry<VisualizerType<out PathVisualizer<*, *>>>()
    override val types: KeyedRegistry<VisualizerType<out PathVisualizer<*, *>>>
        get() = HashedRegistry(visualizerTypes)

    init {
        pathFinder.disposer.register(pathFinder, this)

        EXTENSION_POINT.extensions.forEach {
            this.registerVisualizerType(it)
        }

        if (!visualizerTypes.isEmpty()) {
            defaultType = visualizerTypes.values.stream()
                .filter { it.key.key == "particle" }
                .findFirst()
                .orElse(
                    visualizerTypes.values.iterator().next()
                ) as VisualizerType<PathVisualizer<*, *>>
        } else {
            throw IllegalStateException("No path visualizers found.")
        }
    }

    override fun <T : PathVisualizer<*, *>> getType(typeKey: NamespacedKey): VisualizerType<T>? {
        return visualizerTypes[typeKey] as VisualizerType<T>
    }

    override fun <T : PathVisualizer<*, *>> registerVisualizerType(type: VisualizerType<T>) {
        visualizerTypes.put(type)
        PathFinder.get().disposer.register(this, type)
    }

    override fun unregisterVisualizerType(type: VisualizerType<out PathVisualizer<*, *>>) {
        PathFinder.get().disposer.unregister(type)
        visualizerTypes.remove(type.key)
    }
}
