package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinedVisualizer extends Visualizer<CombinedVisualizer, CombinedVisualizer.CombinedData> {

	public record CombinedData(Map<NamespacedKey, Object> childData) {
	}

	private final List<PathVisualizer<?, ?>> visualizers;

	public void addVisualizer(PathVisualizer<?, ?> visualizer) {
		this.visualizers.add(visualizer);
	}

	public void removeVisualizer(PathVisualizer<?, ?> visualizer) {
		this.visualizers.remove(visualizer);
	}

	public void clearVisualizers() {
		this.visualizers.clear();
	}

	public List<PathVisualizer<?, ?>> getVisualizers() {
		return new ArrayList<>(visualizers);
	}

	public CombinedVisualizer(NamespacedKey key, String nameFormat) {
		super(key, nameFormat);
		visualizers = new ArrayList<>();
	}

	@Override
	public VisualizerType<CombinedVisualizer> getType() {
		return VisualizerHandler.COMBINED_VISUALIZER_TYPE;
	}

	@Override
	public CombinedData prepare(List<Node> nodes, Player player) {
		return new CombinedData(visualizers.stream()
				.collect(Collectors.toMap(Keyed::getKey, v -> v.prepare(nodes, player))));
	}

	@Override
	public void play(VisualizerContext<CombinedData> context) {
		visualizers.forEach(visualizer -> visualizer.play(new VisualizerContext(context.players(), context.interval(), context.time(), context.data().childData().get(visualizer.getKey()))));
	}
}
