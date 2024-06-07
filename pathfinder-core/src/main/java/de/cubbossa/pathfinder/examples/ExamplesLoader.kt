package de.cubbossa.pathfinder.examples

import de.cubbossa.pathfinder.examples.ExamplesFileReader.ExampleFile
import de.cubbossa.pathfinder.misc.NamespacedKey.Companion.fromString
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerType
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistry
import lombok.Getter
import org.bukkit.configuration.file.YamlConfiguration
import java.io.StringReader
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

class ExamplesLoader @JvmOverloads constructor(
    private val registry: VisualizerTypeRegistry,
    private val url: String = COMMON_REPO
) {

    private val files: MutableList<ExampleFile> = ArrayList()
    private val reader = ExamplesFileReader()

    @Getter
    private var cached = false

    val exampleFiles: CompletableFuture<Collection<ExampleFile>>
        get() = if (!cached) {
            reader.getExamples(url).thenApply { exampleFiles: Collection<ExampleFile>? ->
                files.addAll(
                    exampleFiles!!
                )
                cached = true
                HashSet(this.files)
            }
        } else {
            CompletableFuture.completedFuture(HashSet(this.files))
        }

    val examples: CompletableFuture<Collection<PathVisualizer<*, *>>>
        get() = exampleFiles
            .thenApply { exampleFiles: Collection<ExampleFile> ->
                exampleFiles.stream().parallel()
                    .map<CompletableFuture<Map.Entry<PathVisualizer<*, *>, VisualizerType<PathVisualizer<*, *>>>>> {
                        this.loadVisualizer(it)
                    }
                    .map { it.join() }
                    .map { it.key }
                    .collect(Collectors.toSet())
            }

    fun <V : PathVisualizer<*, *>>
            loadVisualizer(file: ExampleFile): CompletableFuture<Map.Entry<V, VisualizerType<V>>> {
        return reader.read(file.fetchUrl).thenApply { s: String? ->
            val values = YamlConfiguration.loadConfiguration(StringReader(s)).getValues(false)
            val typeString = values["type"] as String
            val type: VisualizerType<V>? = registry.getType(
                fromString(typeString)
            )
            if (type == null) {
                throw RuntimeException(
                    "Could not load visualizer of type '$typeString'. Make sure that required PathFinder extensions are installed."
                )
            }
            AbstractMap.SimpleEntry(parse(file, type, values), type)
        }
    }

    private fun <VisualizerT : PathVisualizer<*, *>> parse(
        file: ExampleFile,
        type: VisualizerType<VisualizerT>,
        values: Map<String, Any>
    ): VisualizerT {
        val name = fromString(
            file.name
                .replace(".yml", "")
                .replace("$", ":")
        )

        if (type is AbstractVisualizerType<*>) {
            val visualizer = type.createVisualizer(name)
            type.deserialize(visualizer, values)
            return visualizer as VisualizerT
        } else {
            throw RuntimeException("Only visualizers of a type that extends 'AbstractVisualizerType' can be loaded from yml files.")
        }
    }

    companion object {
        private const val COMMON_REPO =
            "https://api.github.com/repos/CubBossa/PathFinder/contents/examples"
    }
}
