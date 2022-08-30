package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerDistanceChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerIntervalChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerNameChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerPermissionChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class PathVisualizerCommand extends CommandTree {

	public PathVisualizerCommand() {
		super("pathvisualizer");
		withPermission(PathPlugin.PERM_CMD_PV);

		then(new LiteralArgument("list")
				.withPermission(PathPlugin.PERM_CMD_PV_LIST)
				.executes((commandSender, objects) -> {
					onList(commandSender, 1);
				})
				.then(new IntegerArgument("page", 1)
						.executes((commandSender, objects) -> {
							onList(commandSender, (Integer) objects[0]);
						})));

		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_PV_CREATE)
				.then(CustomArgs.visualizerTypeArgument("type")
						.then(new StringArgument("key")
								.executes((commandSender, objects) -> {
									onCreate(commandSender, (VisualizerType<?>) objects[0],
											new NamespacedKey(PathPlugin.getInstance(), (String) objects[1]));
								}))));

		then(new LiteralArgument("delete")
				.withPermission(PathPlugin.PERM_CMD_PV_DELETE)
				.then(CustomArgs.pathVisualizerArgument("visualizer")
						.executes((commandSender, objects) -> {
							onDelete(commandSender, (PathVisualizer<?>) objects[0]);
						})));

		then(new LiteralArgument("info")
				.withPermission(PathPlugin.PERM_CMD_PV_INFO)
				.then(CustomArgs.pathVisualizerArgument("visualizer")
						.executes((commandSender, objects) -> {
							onInfo(commandSender, (PathVisualizer<?>) objects[0]);
						})));
	}

	@Override
	public void register() {

		LiteralArgument lit = new LiteralArgument("edit");
		for (VisualizerType<?> type : VisualizerHandler.getInstance().getVisualizerTypes()) {

			ArgumentTree typeArg = CustomArgs.pathVisualizerArgument("visualizer", type);
			type.appendEditCommand(typeArg, 0, 1);

			typeArg.then(new LiteralArgument("name")
					.withPermission(PathPlugin.PERM_CMD_PV_SET_NAME)
					.then(CustomArgs.miniMessageArgument("name")
							.executes((commandSender, objects) -> {
								onSetName(commandSender, (PathVisualizer<?>) objects[0], (String) objects[1]);
							})));
			typeArg.then(new LiteralArgument("permission")
					.withPermission(PathPlugin.PERM_CMD_PV_SET_PERMISSION)
					.then(new GreedyStringArgument("permission")
							.executes((commandSender, objects) -> {
								onSetPermission(commandSender, (PathVisualizer<?>) objects[0], (String) objects[1]);
							})));
			typeArg.then(new LiteralArgument("interval")
					.withPermission(PathPlugin.PERM_CMD_PV_INTERVAL)
					.then(new IntegerArgument("ticks", 1)
							.executes((commandSender, objects) -> {
								onSetInterval(commandSender, (PathVisualizer<?>) objects[0], (Integer) objects[1]);
							})));
			typeArg.then(new LiteralArgument("point-distance")
					.withPermission(PathPlugin.PERM_CMD_PV_POINT_DIST)
					.then(new FloatArgument("distance", .02f, 100)
							.executes((commandSender, objects) -> {
								onSetPointDistance(commandSender, (PathVisualizer<?>) objects[0], (Float) objects[1]);
							})));

			lit.then(new LiteralArgument(type.getCommandName()).then(typeArg));
		}
		then(lit);
		super.register();
	}

	public void onList(CommandSender sender, int page) {

		TagResolver resolver = TagResolver.builder()
				.tag("page", Tag.preProcessParsed(page + 1 + ""))
				.tag("prev-page", Tag.preProcessParsed(Integer.max(1, page + 1) + ""))
				.tag("next-page", Tag.preProcessParsed(Integer.min((int) Math.ceil(RoadMapHandler.getInstance().getRoadMaps().size() / 10.), page + 3) + ""))
				.tag("pages", Tag.preProcessParsed((int) Math.ceil(RoadMapHandler.getInstance().getRoadMaps().size() / 10.) + ""))
				.build();

		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_LIST_HEADER.format(resolver), sender);

		for (PathVisualizer<?> visualizer : CommandUtils.subList(new ArrayList<>(VisualizerHandler.getInstance().getPathVisualizerMap().values()), page, 10)) {
			TagResolver r = TagResolver.builder()
					.tag("key", Messages.formatKey(visualizer.getKey()))
					.resolver(Placeholder.component("name", visualizer.getDisplayName()))
					.resolver(Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
					.resolver(Placeholder.component("type", Component.text(visualizer.getNameFormat())))
					.build();

			TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_LIST_ENTRY.format(resolver, r), sender);
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_LIST_FOOTER.format(resolver), sender);
	}

	public void onCreate(CommandSender sender, VisualizerType<?> type, NamespacedKey key) {

		if (VisualizerHandler.getInstance().getPathVisualizer(key) != null) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_NAME_EXISTS, sender);
			return;
		}
		PathVisualizer<?> visualizer = VisualizerHandler.getInstance().createPathVisualizer(key);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_CREATE_SUCCESS.format(TagResolver.builder()
				.tag("key", Messages.formatKey(visualizer.getKey()))
				.resolver(Placeholder.component("name", visualizer.getDisplayName()))
				.resolver(Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
				.resolver(Placeholder.component("type", Component.text(visualizer.getNameFormat())))
				.build()), sender);
	}

	public void onDelete(CommandSender sender, PathVisualizer<?> visualizer) {
		if (!VisualizerHandler.getInstance().deletePathVisualizer(visualizer)) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_ERROR, sender);
			return;
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_SUCCESS
				.format(TagResolver.builder()
						.tag("key", Messages.formatKey(visualizer.getKey()))
						.resolver(Placeholder.component("name", visualizer.getDisplayName()))
						.resolver(Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
						.build()), sender);
	}

	public <T extends PathVisualizer<T>> void onInfo(CommandSender sender, PathVisualizer<T> visualizer) {

		FormattedMessage message = visualizer.getType().getInfoMessage((T) visualizer).format(TagResolver.builder()
				.tag("key", Messages.formatKey(visualizer.getKey()))
				.resolver(Placeholder.component("name", visualizer.getDisplayName()))
				.resolver(Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
				.resolver(Placeholder.component("type", Component.text(visualizer.getNameFormat())))
				.resolver(Placeholder.component("permission", Messages.formatPermission(visualizer.getPermission())))
				.resolver(Placeholder.component("interval", Component.text(visualizer.getInterval())))
				.resolver(Placeholder.component("point-distance", Component.text(visualizer.getPointDistance())))
				.build());

		TranslationHandler.getInstance().sendMessage(message, sender);
	}

	public void onSetName(CommandSender sender, PathVisualizer<?> edit, String newName) {
		String old = edit.getNameFormat();
		Component oldComp = edit.getDisplayName();
		edit.setNameFormat(newName);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_NAME.format(tags(edit,
				oldComp, edit.getDisplayName())), sender);
		Bukkit.getPluginManager().callEvent(new VisualizerNameChangedEvent(edit, old, newName));
	}

	public void onSetPermission(CommandSender sender, PathVisualizer<?> edit, String permission) {
		String old = edit.getPermission();
		edit.setPermission(permission);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_PERM.format(tags(edit,
				Messages.formatPermission(old), Messages.formatPermission(permission))), sender);
		Bukkit.getPluginManager().callEvent(new VisualizerPermissionChangedEvent(edit, old, permission));
	}

	public void onSetInterval(CommandSender sender, PathVisualizer<?> edit, int interval) {
		int old = edit.getInterval();
		edit.setInterval(interval);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_INTERVAL.format(tags(edit,
				Component.text(old), Component.text(interval))), sender);
		Bukkit.getPluginManager().callEvent(new VisualizerIntervalChangedEvent(edit, old, interval));
	}

	public void onSetPointDistance(CommandSender sender, PathVisualizer<?> edit, float distance) {
		float old = edit.getPointDistance();
		edit.setPointDistance(distance);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_DIST.format(tags(edit,
				Component.text(old), Component.text(distance))), sender);
		Bukkit.getPluginManager().callEvent(new VisualizerDistanceChangedEvent(edit, old, distance));
	}

	private <V extends ComponentLike> TagResolver tags(PathVisualizer<?> visualizer, V old, V value) {
		return TagResolver.builder()
				.tag("key", Messages.formatKey(visualizer.getKey()))
				.resolver(Placeholder.component("name", visualizer.getDisplayName()))
				.resolver(Placeholder.component("type", Component.text(visualizer.getType().getCommandName())))
				.resolver(Placeholder.component("old-value", old))
				.resolver(Placeholder.component("value", value))
				.build();
	}
}
