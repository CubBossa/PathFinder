package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.command.PathVisualizerCommand;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerDistanceChangedEvent;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

public abstract class BezierVisualizerType<T extends BezierPathVisualizer<T>> extends VisualizerType<T> {

	public BezierVisualizerType(NamespacedKey key) {
		super(key);
	}

	@Override
	public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
		return tree
				.then(new LiteralArgument("point-distance")
						.withPermission(PathPlugin.PERM_CMD_PV_POINT_DIST)
						.then(new FloatArgument("distance", .02f, 100)
								.executes((commandSender, objects) -> {
									onSetPointDistance(commandSender, (BezierPathVisualizer<?>) objects[0], (Float) objects[1]);
								})))
				.then(new LiteralArgument("sample-rate")
						.withPermission(PathPlugin.PERM_CMD_PV_SAMPLE_RATE)
						.then(new IntegerArgument("sample-rate", 1, 64)
								.executes((commandSender, objects) -> {

								})));
	}

	public void onSetPointDistance(CommandSender sender, BezierPathVisualizer<?> edit, float distance) {
		float old = edit.getPointDistance();
		edit.setPointDistance(distance);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_DIST.format(PathVisualizerCommand.tags(edit,
				Component.text(old), Component.text(distance))), sender);
		Bukkit.getPluginManager().callEvent(new VisualizerDistanceChangedEvent(edit, old, distance));
	}

	public void onSetSampleRate(CommandSender sender, BezierPathVisualizer<?> edit, int rate) {
		int old = edit.getBezierSamplingRate();
		edit.setBezierSamplingRate(rate);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_SAMPLE.format(PathVisualizerCommand.tags(edit,
				Component.text(old), Component.text(rate))), sender);
		Bukkit.getPluginManager().callEvent(new VisualizerDistanceChangedEvent(edit, old, rate));
	}
}
