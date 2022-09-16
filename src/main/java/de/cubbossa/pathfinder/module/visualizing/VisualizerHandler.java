package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.visualizing.events.ParticleVisualizerStepsChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.visualizer.*;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.StringUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
public class VisualizerHandler {

	public static final VisualizerType<ParticleVisualizer> PARTICLE_VISUALIZER_TYPE = new ParticleVisualizerType(new NamespacedKey(PathPlugin.getInstance(), "particle"));
	public static final VisualizerType<AdvancedParticleVisualizer> ADV_PARTICLE_VISUALIZER_TYPE = new AdvancedParticleVisualizerType(new NamespacedKey(PathPlugin.getInstance(), "advanced-particle"));
	public static final VisualizerType<CombinedVisualizer> COMBINED_VISUALIZER_TYPE = new CombinedVisualizerType(new NamespacedKey(PathPlugin.getInstance(), "combined"));

	@Getter
	private static VisualizerHandler instance;

	private final HashedRegistry<VisualizerType<?>> visualizerTypes;

	private final HashedRegistry<PathVisualizer<?, ?>> pathVisualizerMap;

	// Map<Player, Map<RoadMap, PathVisualizer>>
	private final Map<UUID, Map<NamespacedKey, PathVisualizer<?, ?>>> playerVisualizers;
	private final Map<Integer, HashedRegistry<PathVisualizer<?, ?>>> roadmapVisualizers;


	public VisualizerHandler() {
		instance = this;

		this.visualizerTypes = new HashedRegistry<>();
		visualizerTypes.put(PARTICLE_VISUALIZER_TYPE);
		visualizerTypes.put(COMBINED_VISUALIZER_TYPE);
		visualizerTypes.put(ADV_PARTICLE_VISUALIZER_TYPE);

		this.pathVisualizerMap = new HashedRegistry<>();
		pathVisualizerMap.putAll(PathPlugin.getInstance().getDatabase().loadPathVisualizer());
		this.playerVisualizers = new HashMap<>();
		this.roadmapVisualizers = new HashMap<>();
		this.pathVisualizerMap.put(new SerializableAdvancedParticleVisualizer(new NamespacedKey(PathPlugin.getInstance(), "advtest"), "lul"));
	}

	public @Nullable <T extends PathVisualizer<T, ?>> VisualizerType<T> getVisualizerType(NamespacedKey key) {
		return (VisualizerType<T>) visualizerTypes.get(key);
	}

	public <T extends PathVisualizer<T, ?>> void registerVisualizerType(VisualizerType<T> type) {
		visualizerTypes.put(type);
	}

	public void unregisterVisualizerType(VisualizerType<?> type) {
		visualizerTypes.remove(type.getKey());
	}

	public void setSteps(ParticleVisualizer visualizer, int value) {
		int old = visualizer.getSchedulerSteps();
		visualizer.setSchedulerSteps(value);
		Bukkit.getPluginManager().callEvent(new ParticleVisualizerStepsChangedEvent(visualizer, old, value));
	}

	public @Nullable PathVisualizer<?, ?> getPathVisualizer(NamespacedKey key) {
		return pathVisualizerMap.get(key);
	}

	public <T extends PathVisualizer<T, ?>> T createPathVisualizer(VisualizerType<T> type, NamespacedKey key) {
		return createPathVisualizer(type, key, StringUtils.insertInRandomHexString(StringUtils.capizalize(key.getKey())));
	}

	public <T extends PathVisualizer<T, ?>> T createPathVisualizer(VisualizerType<T> type, NamespacedKey key, String nameFormat) {

		if (pathVisualizerMap.containsKey(key)) {
			throw new IllegalArgumentException("Could not insert new path visualizer, another visualizer with key '" + key + "' already exists.");
		}
		T visualizer = type.create(key, nameFormat);
		PathPlugin.getInstance().getDatabase().updatePathVisualizer(visualizer);
		pathVisualizerMap.put(visualizer);
		return visualizer;
	}

	public boolean deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
		PathPlugin.getInstance().getDatabase().deletePathVisualizer(visualizer);
		return pathVisualizerMap.remove(visualizer.getKey()) != null;
	}

	public Stream<PathVisualizer<?, ?>> getPathVisualizerStream() {
		return pathVisualizerMap.values().stream();
	}
}
