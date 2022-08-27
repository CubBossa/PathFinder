package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerDistanceChangedEvent;
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
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class PathVisualizerCommand extends CommandTree {

	public PathVisualizerCommand() {
		super("pathvisualizer");

		then(new LiteralArgument("list")
				.withPermission("pathfinder.command.visualizer.list")
				.executes((commandSender, objects) -> {
					onList(commandSender, 1);
				})
				.then(new IntegerArgument("page", 1)
						.executes((commandSender, objects) -> {
							onList(commandSender, (Integer) objects[0]);
						})));

		then(new LiteralArgument("create")
				.withPermission("pathfinder.command.visualizer.create")
				.then(CustomArgs.visualizerTypeArgument("type")
						.then(new StringArgument("key")
								.executes((commandSender, objects) -> {
									onCreate(commandSender, (VisualizerType<?>) objects[0],
											new NamespacedKey(PathPlugin.getInstance(), (String) objects[1]));
								}))));

		then(new LiteralArgument("delete")
				.withPermission("pathfinder.command.visualizer.delete")
				.then(CustomArgs.pathVisualizerArgument("visualizer")
						.executes((commandSender, objects) -> {
							onDelete(commandSender, (PathVisualizer) objects[0]);
						})));

		then(new LiteralArgument("info")
				.withPermission("pathfinder.command.visualizer.info")
				.then(CustomArgs.pathVisualizerArgument("visualizer")
						.executes((commandSender, objects) -> {
							onInfo(commandSender, (PathVisualizer) objects[0]);
						})));
	}

	@Override
	public void register() {

		LiteralArgument lit = new LiteralArgument("edit");
		for (VisualizerType<?> type : VisualizerHandler.getInstance().getVisualizerTypes()) {

			ArgumentTree typeArg = CustomArgs.pathVisualizerArgument("visualizer", type);
			type.appendEditCommand(typeArg, 0, 1);

			typeArg.then(new LiteralArgument("name")
					.withPermission("pathfinder.command.visualizer.set_name")
					.then(CustomArgs.miniMessageArgument("name")
							.executes((commandSender, objects) -> {
								onSetName(commandSender, (PathVisualizer) objects[0], (String) objects[1]);
							})));
			typeArg.then(new LiteralArgument("permission")
					.withPermission("pathfinder.command.visualizer.set_permission")
					.then(new GreedyStringArgument("permission")
							.executes((commandSender, objects) -> {
								onSetPermission(commandSender, (PathVisualizer) objects[0], (String) objects[1]);
							})));
			typeArg.then(new LiteralArgument("interval")
					.withPermission("pathfinder.command.visualizer.set_interval")
					.then(new IntegerArgument("ticks", 1)
							.executes((commandSender, objects) -> {
								//TODO
							})));
			typeArg.then(new LiteralArgument("point-distance")
					.withPermission("pathfinder.command.visualizer.set_distance")
					.then(new FloatArgument("distance", .02f, 100)
							.executes((commandSender, objects) -> {
								onSetPointDistance(commandSender, (PathVisualizer) objects[0], (Float) objects[1]);
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
					.tag("key", Tag.preProcessParsed(visualizer.getKey().getKey() + ""))
					.tag("name", Tag.inserting(visualizer.getDisplayName()))
					.tag("name-format", Tag.inserting(Component.text(visualizer.getNameFormat())))
					.tag("type", Tag.inserting(Component.text(visualizer.getNameFormat())))
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
		PathVisualizer visualizer = VisualizerHandler.getInstance().createPathVisualizer(key);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_CREATE_SUCCESS.format(TagResolver.builder()
				.tag("key", Tag.inserting(Messages.formatKey(visualizer.getKey())))
				.tag("name", Tag.inserting(visualizer.getDisplayName()))
				.tag("name-format", Tag.inserting(Component.text(visualizer.getNameFormat())))
				.tag("type", Tag.inserting(Component.text(visualizer.getNameFormat())))
				.build()), sender);
	}

	public void onDelete(CommandSender sender, PathVisualizer visualizer) {
		if (!VisualizerHandler.getInstance().deletePathVisualizer(visualizer)) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_ERROR, sender);
			return;
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_SUCCESS
				.format(TagResolver.resolver("key", Tag.inserting(Messages.formatKey(visualizer.getKey()))),
						TagResolver.resolver("name", Tag.inserting(visualizer.getDisplayName())),
						TagResolver.resolver("name-format", Tag.inserting(Component.text(visualizer.getNameFormat())))), sender);
	}

	public <T extends PathVisualizer<T>> void onInfo(CommandSender sender, PathVisualizer<T> visualizer) {

		FormattedMessage message = visualizer.getType().getInfoMessage((T) visualizer).format(TagResolver.builder()
				.tag("id", Tag.preProcessParsed(visualizer.getKey().toString()))
				.tag("name", Tag.inserting(visualizer.getDisplayName()))
				.tag("name-format", Tag.inserting(Component.text(visualizer.getNameFormat())))
				.tag("type", Tag.inserting(Component.text(visualizer.getNameFormat())))
				.tag("permission", Tag.inserting(Messages.formatPermission(visualizer.getPermission())))
				.tag("interval", Tag.inserting(Component.text(visualizer.getTickDelay())))
				.tag("point-distance", Tag.inserting(Component.text(visualizer.getPointDistance())))
				.build());

		TranslationHandler.getInstance().sendMessage(message, sender);
	}

	public void onSetName(CommandSender sender, PathVisualizer edit, String newName) {
		String old = edit.getNameFormat();
		edit.setNameFormat(newName);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_NAME, sender);
		Bukkit.getPluginManager().callEvent(new VisualizerNameChangedEvent(edit, old, newName));
	}

	public void onSetPermission(CommandSender sender, PathVisualizer edit, String permission) {
		String old = edit.getPermission();
		edit.setPermission(permission);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_PERM, sender);
		Bukkit.getPluginManager().callEvent(new VisualizerPermissionChangedEvent(edit, old, permission));
	}

	public void onSetPointDistance(CommandSender sender, PathVisualizer edit, float distance) {
		float old = edit.getPointDistance();
		edit.setPointDistance(distance);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_DIST, sender);
		Bukkit.getPluginManager().callEvent(new VisualizerDistanceChangedEvent(edit, old, distance));
	}
}
