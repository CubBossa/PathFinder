package de.bossascrew.pathfinder.core.commands;

import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.core.commands.argument.CustomArgs;
import de.bossascrew.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.bossascrew.pathfinder.core.node.NodeGroup;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.StringUtils;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class NodeGroupCommand extends LiteralArgument {

	public NodeGroupCommand(int offset) {
		super("nodegroups");

		then(new LiteralArgument("list")
				.withPermission(PathPlugin.PERM_CMD_NG_LIST)
				.executesPlayer((player, objects) -> {
					listGroups(player, null, 0);
				})
				.then(new IntegerArgument("page")
						.executesPlayer((player, objects) -> {
							listGroups(player, objects[0] == null ? null : (RoadMap) objects[0], (int) objects[offset]);
						})));

		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_NG_CREATE)
				.then(new NamespacedKeyArgument("name")
						.executesPlayer((player, objects) -> {
							createGroup(player, objects[0] == null ? null : (RoadMap) objects[0], (NamespacedKey) objects[offset]);
						})));

		then(new LiteralArgument("delete")
				.withPermission(PathPlugin.PERM_CMD_NG_DELETE)
				.then(CustomArgs.nodeGroupArgument("group", offset == 0 ? null : 0)
						.executesPlayer((player, objects) -> {
							deleteGroup(player, objects[0] == null ? null : (RoadMap) objects[0], (NodeGroup) objects[offset]);
						})));
		then(new LiteralArgument("rename")
				.withPermission(PathPlugin.PERM_CMD_NG_RENAME)
				.then(CustomArgs.nodeGroupArgument("group", offset == 0 ? null : 0)
						.then(CustomArgs.miniMessageArgument("name", i -> Lists.newArrayList(((NodeGroup) i.previousArgs()[0]).getNameFormat()))
								.executesPlayer((player, objects) -> {
									renameGroup(player, (NodeGroup) objects[offset], (String) objects[offset + 1]);
								})
						)));
		then(new LiteralArgument("search-terms")
				.then(new LiteralArgument("add")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_ADD)
						.then(CustomArgs.nodeGroupArgument("group", offset == 0 ? null : 0)
								.then(CustomArgs.suggestCommaSeparatedList("search-terms")
										.executesPlayer((player, objects) -> {
											searchTermsAdd(player, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										}))))
				.then(new LiteralArgument("remove")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_REMOVE)
						.then(CustomArgs.nodeGroupArgument("group", offset == 0 ? null : 0)
								.then(CustomArgs.suggestCommaSeparatedList("search-terms")
										.executesPlayer((player, objects) -> {
											searchTermsRemove(player, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										}))))
				.then(new LiteralArgument("list")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_LIST)
						.then(CustomArgs.nodeGroupArgument("group", offset == 0 ? null : 0)
								.executesPlayer((player, objects) -> {
									searchTermsList(player, (NodeGroup) objects[offset]);
								}))));
		then(new LiteralArgument("set")
				.then(new LiteralArgument("findable")
						.withPermission(PathPlugin.PERM_CMD_NG_SET_FINDABLE)
						.then(CustomArgs.nodeGroupArgument("group", offset == 0 ? null : 0)
								.then(new BooleanArgument("value")
										.executesPlayer((player, objects) -> {
											setFindable(player, (NodeGroup) objects[offset], (Boolean) objects[offset + 1]);
										})))));
	}

	public void searchTermsList(Player player, NodeGroup group) {
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_LIST.format(TagResolver.builder()
				.tag("name", Tag.inserting(group.getDisplayName()))
				.tag("values", Tag.inserting(toList(group.getSearchTerms())))
				.build()), player);
	}

	public void searchTermsAdd(Player player, NodeGroup group, String commaSeparatedList) {
		Collection<String> toAdd = Arrays.stream(commaSeparatedList.split(","))
				.map(String::trim)
				.map(String::toLowerCase)
				.toList();
		group.getSearchTerms().addAll(toAdd);
		Bukkit.getPluginManager().callEvent(new NodeGroupSearchTermsChangedEvent(
				group, NodeGroupSearchTermsChangedEvent.Action.ADD, toAdd
		));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_ADD.format(TagResolver.builder()
				.tag("name", Tag.inserting(group.getDisplayName()))
				.tag("values", Tag.inserting(toList(toAdd)))
				.build()), player);
	}

	public void searchTermsRemove(Player player, NodeGroup group, String commaSeparatedList) {
		Collection<String> toRemove = Arrays.stream(commaSeparatedList.split(","))
				.map(String::trim)
				.map(String::toLowerCase)
				.toList();
		group.getSearchTerms().removeAll(toRemove);

		Bukkit.getPluginManager().callEvent(new NodeGroupSearchTermsChangedEvent(
				group, NodeGroupSearchTermsChangedEvent.Action.REMOVE, toRemove
		));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_REMOVE.format(TagResolver.builder()
				.tag("name", Tag.inserting(group.getDisplayName()))
				.tag("values", Tag.inserting(toList(toRemove)))
				.build()), player);
	}

	private Component toList(Collection<String> tags) {
		return Component.join(JoinConfiguration.separator(Component.text(",", NamedTextColor.GRAY)), tags.stream()
				.map(Component::text).collect(Collectors.toList()));
	}

	public void setFindable(Player player, NodeGroup group, boolean findable) {
		group.setFindable(findable);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_FINDABLE.format(TagResolver.builder()
				.tag("name", Tag.inserting(group.getDisplayName()))
				.tag("value", Tag.inserting(Component.text(findable)))
				.build()), player);
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

	public void deleteGroup(Player player, @Nullable RoadMap roadMap, NodeGroup group) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(player);
		}

		roadMap.removeNodeGroup(group);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), player);
	}

	public void createGroup(Player player, @Nullable RoadMap roadMap, NamespacedKey key) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(player);
		}

		if (roadMap.getNodeGroup(key) != null) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS
					.format(TagResolver.resolver("name", Tag.inserting(Component.text(key.toString())))), player);
			return;
		}

		NodeGroup group = roadMap.createNodeGroup(key, true, StringUtils.getRandHexString() + key.getKey());
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), player);
	}

	public void listGroups(Player player, @Nullable RoadMap roadMap, int pageInput) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(player);
		}

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
