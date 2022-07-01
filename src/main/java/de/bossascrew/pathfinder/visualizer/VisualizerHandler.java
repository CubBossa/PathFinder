package de.bossascrew.pathfinder.visualizer;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.util.HashedRegistry;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.particle.ParticleBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
public class VisualizerHandler {

	@Getter
	private static VisualizerHandler instance;

	private final SimpleCurveVisualizer defaultSimpleCurveVisualizer;
	private final HashedRegistry<PathVisualizer> pathVisualizerMap;

	// Map<Player, Map<RoadMap, PathVisualizer>>
	private final Map<UUID, Map<NamespacedKey, PathVisualizer>> playerVisualizers;
	private final Map<Integer, HashedRegistry<SimpleCurveVisualizer>> roadmapVisualizers;


	public VisualizerHandler() {

		instance = this;
		defaultSimpleCurveVisualizer = new SimpleCurveVisualizer(new NamespacedKey(PathPlugin.getInstance(), "default"), "<gray>Default Particles</gray>");

		this.pathVisualizerMap = new HashedRegistry<>();
		pathVisualizerMap.put(defaultSimpleCurveVisualizer);
		this.playerVisualizers = new HashMap<>();
		this.roadmapVisualizers = new HashMap<>();
	}

	public SimpleCurveVisualizer createPathVisualizer(NamespacedKey key,
													  String nameFormat,
													  ParticleBuilder particle,
													  ItemStack displayItem,
													  double particleDistance,
													  int particleSteps,
													  int schedulerPeriod) {

		if (pathVisualizerMap.containsKey(key)) {
			throw new IllegalArgumentException("Could not insert new path visualizer, another visualizer with key '" + key + "' already exists.");
		}
		SimpleCurveVisualizer visualizer = PathPlugin.getInstance().getDatabase().newPathVisualizer(key, nameFormat,
				particle, displayItem, particleDistance, particleSteps, schedulerPeriod, 3);
		pathVisualizerMap.put(visualizer);
		return visualizer;
	}

	public boolean removePathVisualizer(SimpleCurveVisualizer visualizer) {
		PathPlugin.getInstance().getDatabase().deletePathVisualizer(visualizer);
		return pathVisualizerMap.remove(visualizer.getKey()) != null;
	}

	public Stream<PathVisualizer> getPathVisualizerStream() {
		return pathVisualizerMap.values().stream();
	}
}
