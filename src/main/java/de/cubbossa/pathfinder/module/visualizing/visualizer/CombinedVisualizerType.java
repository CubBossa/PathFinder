package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.events.CombinedVisualizerChangedEvent;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.*;
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
									CombinedVisualizer vis = (CombinedVisualizer) objects[0];
									PathVisualizer<?, ?> target = (PathVisualizer<?, ?>) objects[1];
									vis.addVisualizer(target);
									Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () ->
											Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
													CombinedVisualizerChangedEvent.Action.ADD, Collections.singleton(target))));
									TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_COMBINED_ADD, sender);
								})))
				.then(new LiteralArgument("list")
						.executes((sender, objects) -> {
							TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_COMBINED_LIST, sender);
						}))
				.then(new LiteralArgument("remove")
						.then(CustomArgs.pathVisualizerArgument("child")
								.executes((sender, objects) -> {
									CombinedVisualizer vis = (CombinedVisualizer) objects[0];
									PathVisualizer<?, ?> target = (PathVisualizer<?, ?>) objects[1];
									vis.removeVisualizer(target);
									Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () ->
											Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
													CombinedVisualizerChangedEvent.Action.REMOVE, Collections.singleton(target))));
									TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_COMBINED_REMOVE, sender);
								})))
				.then(new LiteralArgument("clear")
						.executes((commandSender, objects) -> {
							CombinedVisualizer vis = (CombinedVisualizer) objects[0];
							Collection<PathVisualizer<?, ?>> targets = vis.getVisualizers();
							vis.clearVisualizers();
							Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () ->
									Bukkit.getPluginManager().callEvent(new CombinedVisualizerChangedEvent(vis,
											CombinedVisualizerChangedEvent.Action.REMOVE, targets)));
							TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_COMBINED_CLEAR, commandSender);
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
