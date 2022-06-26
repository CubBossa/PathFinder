package de.bossascrew.pathfinder.commands;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.commands.argument.CustomArgs;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.StringUtils;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CNodeGroupCommand extends CommandAPICommand {

	public CNodeGroupCommand() {
		super("nodegroup");

		withSubcommand(new CommandAPICommand("list")
				.withPermission(PathPlugin.PERM_CMD_NG_LIST)
				.executesPlayer((player, objects) -> {
					listGroups(player, 0);
				}));
		withSubcommand(new CommandAPICommand("list")
				.withPermission(PathPlugin.PERM_CMD_NG_LIST)
				.withArguments(new IntegerArgument("page"))
				.executesPlayer((player, objects) -> {
					listGroups(player, (int) objects[0]);
				}));
		withSubcommand(new CommandAPICommand("create")
				.withPermission(PathPlugin.PERM_CMD_NG_CREATE)
				.withArguments(new NamespacedKeyArgument("name"))
				.executesPlayer((player, objects) -> {
					createGroup(player, (NamespacedKey) objects[0]);
				}));
		withSubcommand(new CommandAPICommand("delete")
				.withPermission(PathPlugin.PERM_CMD_NG_DELETE)
				.withArguments(CustomArgs.nodeGroupArgument("group"))
				.executesPlayer((player, objects) -> {
					deleteGroup(player, (NodeGroup) objects[0]);
				}));
		withSubcommand(new CommandAPICommand("rename")
				.withPermission(PathPlugin.PERM_CMD_NG_RENAME)
				.withArguments(
						CustomArgs.nodeGroupArgument("group"),
						CustomArgs.miniMessageArgument("new name")
				)
				.executesPlayer((player, objects) -> {
					renameGroup(player, (NodeGroup) objects[0], (String) objects[1]);
				}));

		register();
	}

	public void renameGroup(Player player, NodeGroup group, String newName) {
		Component oldName = group.getDisplayName();
		group.setNameFormat(newName);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_NAME.format(TagResolver.builder()
				.resolver(Placeholder.component("id", Messages.formatKey(group.getKey())))
				.resolver(Placeholder.component("name", oldName))
				.tag("new-name", Tag.inserting(group.getDisplayName()))
				.tag("value", Tag.inserting(PathPlugin.getInstance().getMiniMessage().deserialize(newName)))
				.build()), player);
	}

	public void deleteGroup(Player player, NodeGroup group) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

		roadMap.removeNodeGroup(group);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), player);
	}

	public void createGroup(Player player, NamespacedKey key) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

		if (roadMap.getNodeGroup(key) != null) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS
					.format(TagResolver.resolver("name", Tag.inserting(Component.text(key.toString())))), player);
			return;
		}

		NodeGroup group = roadMap.createNodeGroup(key, true, StringUtils.getRandHexString() + key.getKey());
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), player);
	}

	public void listGroups(Player player, int pageInput) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

		int pages = (int) Math.ceil(roadMap.getGroups().size() / 10.);
		pageInput = Integer.max(0, Integer.min(pageInput, pages));

		TagResolver resolver = TagResolver.builder()
				.tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
				.tag("page", Tag.preProcessParsed(pageInput + ""))
				.tag("pages", Tag.preProcessParsed(pageInput + ""))
				.tag("prev-page", Tag.preProcessParsed(Integer.max(0, pageInput - 1) + ""))
				.tag("next-page", Tag.preProcessParsed(pageInput + 1 + ""))
				.build();

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_HEADER.format(resolver), player);

		for (NodeGroup group : CommandUtils.subList(new ArrayList<>(roadMap.getGroups().values()), pageInput, 10)) {

			TagResolver r = TagResolver.builder()
					.tag("id", Tag.inserting(Component.text(group.getKey().toString())))
					.tag("name", Tag.inserting(group.getDisplayName()))
					.tag("size", Tag.inserting(Component.text(group.size())))
					.tag("findable", Tag.inserting(Component.text(group.isFindable())))
					.build();
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_LINE.format(resolver, r), player);
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_FOOTER.format(resolver), player);
	}
}
