package de.bossascrew.pathfinder.module.visualizing;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.module.visualizing.visualizer.DebugVisualizer;
import de.bossascrew.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.bossascrew.pathfinder.module.visualizing.visualizer.SimpleCurveVisualizer;
import de.bossascrew.pathfinder.util.HashedRegistry;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
public class VisualizerHandler {

	@Getter
	private static VisualizerHandler instance;

	private final PathVisualizer defaultSimpleCurveVisualizer;
	private final HashedRegistry<PathVisualizer> pathVisualizerMap;

	// Map<Player, Map<RoadMap, PathVisualizer>>
	private final Map<UUID, Map<NamespacedKey, PathVisualizer>> playerVisualizers;
	private final Map<Integer, HashedRegistry<SimpleCurveVisualizer>> roadmapVisualizers;


	public VisualizerHandler() {

		instance = this;
		defaultSimpleCurveVisualizer = new DebugVisualizer(new NamespacedKey(PathPlugin.getInstance(), "debug"));

		this.pathVisualizerMap = new HashedRegistry<>();
		pathVisualizerMap.put(defaultSimpleCurveVisualizer);
		this.playerVisualizers = new HashMap<>();
		this.roadmapVisualizers = new HashMap<>();
	}

	public @Nullable PathVisualizer getPathVisualizer(NamespacedKey key) {
		return pathVisualizerMap.get(key);
	}

	public PathVisualizer createPathVisualizer(NamespacedKey key) {
		return null; //TODO
	}

	public PathVisualizer createPathVisualizer(NamespacedKey key,
											   String nameFormat,
											   ParticleBuilder particle,
											   ItemStack displayItem,
											   double particleDistance,
											   int particleSteps,
											   int schedulerPeriod) {

		if (pathVisualizerMap.containsKey(key)) {
			throw new IllegalArgumentException("Could not insert new path visualizer, another visualizer with key '" + key + "' already exists.");
		}
		PathVisualizer visualizer = PathPlugin.getInstance().getDatabase().newPathVisualizer(key, nameFormat,
				particle, displayItem, particleDistance, particleSteps, schedulerPeriod, 3);
		pathVisualizerMap.put(visualizer);
		return visualizer;
	}

	public boolean deletePathVisualizer(PathVisualizer visualizer) {
		PathPlugin.getInstance().getDatabase().deletePathVisualizer(visualizer);
		return pathVisualizerMap.remove(visualizer.getKey()) != null;
	}

	public Stream<PathVisualizer> getPathVisualizerStream() {
		return pathVisualizerMap.values().stream();
	}
}
