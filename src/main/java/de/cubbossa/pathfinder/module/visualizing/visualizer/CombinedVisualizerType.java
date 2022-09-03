package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.bukkit.NamespacedKey;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinedVisualizerType extends VisualizerType<CombinedVisualizer> {

	public CombinedVisualizerType(NamespacedKey key) {
		super(key);
	}

	@Override
	public CombinedVisualizer create(NamespacedKey key, String nameFormat) {
		return new CombinedVisualizer(key, nameFormat);
	}

	@Override
	public Message getInfoMessage(CombinedVisualizer element) {
		return null;
	}

	@Override
	public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
		return tree
				.then(new LiteralArgument("add")
						.then(CustomArgs.pathVisualizerArgument("child")
								.executes((sender, objects) -> {
									((CombinedVisualizer) objects[0]).addVisualizer((PathVisualizer<?, ?>) objects[1]);
								})))
				.then(new LiteralArgument("list")
						.executes((sender, objects) -> {
							sender.sendMessage(((CombinedVisualizer) objects[0]).getVisualizers().stream()
									.map(PathVisualizer::getKey)
									.map(NamespacedKey::toString)
									.collect(Collectors.joining(", ")));
						}))
				.then(new LiteralArgument("remove")
						.then(CustomArgs.pathVisualizerArgument("child")
								.executes((sender, objects) -> {
									((CombinedVisualizer) objects[0]).removeVisualizer((PathVisualizer<?, ?>) objects[1]);
								})))
				.then(new LiteralArgument("clear")
						.executes((commandSender, objects) -> {
							((CombinedVisualizer) objects[0]).clearVisualizers();
						}));
	}

	@Override
	public Map<String, Object> serialize(CombinedVisualizer visualizer) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("children", visualizer.getVisualizers().stream()
				.map(PathVisualizer::getKey)
				.map(NamespacedKey::toString)
				.collect(Collectors.toList()));
		return map;
	}

	@Override
	public void deserialize(CombinedVisualizer visualizer, Map<String, Object> values) {
		List<String> val = (List<String>) values.get("children");
		if (val == null) {
			return;
		}
		val.stream()
				.map(NamespacedKey::fromString)
				.map(k -> VisualizerHandler.getInstance().getPathVisualizer(k))
				.forEach(visualizer::addVisualizer);
	}
}
