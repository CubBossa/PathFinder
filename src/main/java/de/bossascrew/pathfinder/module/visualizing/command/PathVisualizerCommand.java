package de.bossascrew.pathfinder.module.visualizing.command;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.core.commands.argument.CustomArgs;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.core.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.module.visualizing.VisualizerHandler;
import de.bossascrew.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class PathVisualizerCommand extends CommandTree {

	public PathVisualizerCommand(int offset) {
		super("pathvisualizer");

		then(new LiteralArgument("list")
				.withPermission("pathfinder.command.visualizer.list")
				.executes((commandSender, objects) -> {
					onList(commandSender, 1);
				})
				.then(new IntegerArgument("page", 1)
						.executes((commandSender, objects) -> {
							onList(commandSender, (Integer) objects[offset]);
						})));

		then(new LiteralArgument("create")
				.withPermission("pathfinder.command.visualizer.create")
				.then(new NamespacedKeyArgument("key")
						.executes((commandSender, objects) -> {
							onCreate(commandSender, (NamespacedKey) objects[offset]);
						})));

		then(new LiteralArgument("delete")
				.withPermission("pathfinder.command.visualizer.delete")
				.then(CustomArgs.pathVisualizerArgument("visualizer")
						.executes((commandSender, objects) -> {
							onDelete(commandSender, (PathVisualizer) objects[offset]);
						})));

		then(new LiteralArgument("info")
				.withPermission("pathfinder.command.visualizer.info")
				.then(CustomArgs.pathVisualizerArgument("visualizer")
						.executes((commandSender, objects) -> {
							onInfo(commandSender, (PathVisualizer) objects[offset]);
						})));

		then(new LiteralArgument("edit")
				.then(CustomArgs.pathVisualizerArgument("visualizer")
						.then(new LiteralArgument("set")
								.then(new LiteralArgument("name")
										.withPermission("pathfinder.command.visualizer.set_name")
										.then(CustomArgs.miniMessageArgument("name")
												.executes((commandSender, objects) -> {
													onSetName(commandSender, (PathVisualizer) objects[offset], (String) objects[offset + 1]);
												})))
								.then(new LiteralArgument("permission")
										.withPermission("pathfinder.command.visualizer.set_permission")
										.then(new GreedyStringArgument("permission")
												.executes((commandSender, objects) -> {

												}))))));
	}

	public void onList(CommandSender sender, int page) {

		TagResolver resolver = TagResolver.builder()
				.tag("page", Tag.preProcessParsed(page + 1 + ""))
				.tag("prev-page", Tag.preProcessParsed(Integer.max(1, page + 1) + ""))
				.tag("next-page", Tag.preProcessParsed(Integer.min((int) Math.ceil(RoadMapHandler.getInstance().getRoadMaps().size() / 10.), page + 3) + ""))
				.tag("pages", Tag.preProcessParsed((int) Math.ceil(RoadMapHandler.getInstance().getRoadMaps().size() / 10.) + ""))
				.build();

		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_LIST_HEADER.format(resolver), sender);

		for (RoadMap roadMap : CommandUtils.subList(new ArrayList<>(RoadMapHandler.getInstance().getRoadMaps().values()), page, 10)) {
			TagResolver r = TagResolver.builder()
					.tag("id", Tag.preProcessParsed(roadMap.getKey().getKey() + ""))
					.tag("name", Tag.inserting(roadMap.getDisplayName()))
					.tag("name-format", Tag.inserting(Component.text(roadMap.getNameFormat())))
					.build();

			TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_LIST_ENTRY.format(resolver, r), sender);
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_LIST_FOOTER.format(resolver), sender);

	}

	public void onCreate(CommandSender sender, NamespacedKey key) {

		if (VisualizerHandler.getInstance().getPathVisualizer(key) != null) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_NAME_EXISTS, sender);
			return;
		}
		PathVisualizer visualizer = VisualizerHandler.getInstance().createPathVisualizer(key);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_CREATE_SUCCESS.format(TagResolver.builder()
				.tag("key", Tag.inserting(Messages.formatKey(visualizer.getKey())))
				.tag("name", Tag.inserting(visualizer.getDisplayName()))
				.tag("name-format", Tag.inserting(Component.text(visualizer.getNameFormat())))
				.build()), sender);
	}

	public void onDelete(CommandSender sender, PathVisualizer visualizer) {
		if (!VisualizerHandler.getInstance().deletePathVisualizer(visualizer)) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_ERROR, sender);
			return;
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_SUCCESS, sender);
	}

	public void onInfo(CommandSender sender, PathVisualizer visualizer) {

		FormattedMessage message = Messages.CMD_VIS_INFO.format(TagResolver.builder()
				.tag("id", Tag.preProcessParsed(visualizer.getKey() + ""))
				.tag("name", Tag.inserting(visualizer.getDisplayName()))
				.tag("name-format", Tag.inserting(Component.text(visualizer.getNameFormat())))
				.build());

		TranslationHandler.getInstance().sendMessage(message, sender);
	}

	public void onSetName(CommandSender sender, PathVisualizer edit, String newName) {
		edit.setNameFormat(newName); //TODO event
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_NAME, sender);
	}
}
