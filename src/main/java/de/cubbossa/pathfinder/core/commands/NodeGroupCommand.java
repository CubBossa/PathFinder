package de.cubbossa.pathfinder.core.commands;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.pathfinder.util.StringUtils;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class NodeGroupCommand extends CommandTree {

	public NodeGroupCommand(int offset) {
		super("nodegroup");
		withPermission(PathPlugin.PERM_CMD_NG);

		then(new LiteralArgument("list")
				.withPermission(PathPlugin.PERM_CMD_NG_LIST)
				.executes((sender, objects) -> {
					listGroups(sender, 1);
				})
				.then(new IntegerArgument("page", 1)
						.executes((sender, objects) -> {
							listGroups(sender, (int) objects[offset]);
						})));

		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_NG_CREATE)
				.then(new StringArgument("name")
						.executes((sender, args) -> {
							createGroup(sender, new NamespacedKey(PathPlugin.getInstance(), args[offset].toString()));
						})));

		then(new LiteralArgument("delete")
				.withPermission(PathPlugin.PERM_CMD_NG_DELETE)
				.then(CustomArgs.nodeGroupArgument("group")
						.executes((sender, objects) -> {
							deleteGroup(sender, (NodeGroup) objects[offset]);
						})));

		then(new LiteralArgument("search-terms")
				.then(new LiteralArgument("add")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_ADD)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(CustomArgs.suggestCommaSeparatedList("search-terms")
										.executes((sender, objects) -> {
											searchTermsAdd(sender, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										}))))
				.then(new LiteralArgument("remove")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_REMOVE)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(CustomArgs.suggestCommaSeparatedList("search-terms")
										.executes((sender, objects) -> {
											searchTermsRemove(sender, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										}))))
				.then(new LiteralArgument("list")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_LIST)
						.then(CustomArgs.nodeGroupArgument("group")
								.executes((sender, objects) -> {
									searchTermsList(sender, (NodeGroup) objects[offset]);
								}))));
		then(new LiteralArgument("edit")
				.then(CustomArgs.nodeGroupArgument("group")
						.then(new LiteralArgument("name")
								.withPermission(PathPlugin.PERM_CMD_NG_SET_NAME)
								.then(CustomArgs.miniMessageArgument("name", i -> Lists.newArrayList(((NodeGroup) i.previousArgs()[0]).getNameFormat()))
										.executes((sender, objects) -> {
											renameGroup(sender, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										})
								))
						.then(new LiteralArgument("permission")
								.withPermission(PathPlugin.PERM_CMD_NG_SET_PERM)
								.then(new GreedyStringArgument("permission")
										.executes((sender, objects) -> {
											setGroupPermission(sender, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										})
								))
						.then(new LiteralArgument("navigable")
								.withPermission(PathPlugin.PERM_CMD_NG_SET_NAVIGABLE)
								.then(new BooleanArgument("value")
										.executes((sender, objects) -> {
											setGroupNavigable(sender, (NodeGroup) objects[offset], (Boolean) objects[offset + 1]);
										})
								))
						.then(new LiteralArgument("discoverable")
								.withPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVERABLE)
								.then(new BooleanArgument("value")
										.executes((sender, objects) -> {
											setGroupDiscoverable(sender, (NodeGroup) objects[offset], (Boolean) objects[offset + 1]);
										})))
						.then(new LiteralArgument("find-distance")
								.withPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVER_DIST)
								.then(new FloatArgument("value", 0.01f)
										.executes((sender, objects) -> {
											setGroupDiscoverDist(sender, (NodeGroup) objects[offset], (Float) objects[offset + 1]);
										})))));
	}

	public void searchTermsList(CommandSender sender, NodeGroup group) {
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_LIST.format(TagResolver.builder()
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("values", toList(group.getSearchTerms())))
				.build()), sender);
	}

	public void searchTermsAdd(CommandSender sender, NodeGroup group, String commaSeparatedList) {
		Collection<String> toAdd = Arrays.stream(commaSeparatedList.split(","))
				.map(String::trim)
				.map(String::toLowerCase)
				.toList();
		group.getSearchTerms().addAll(toAdd);
		Bukkit.getPluginManager().callEvent(new NodeGroupSearchTermsChangedEvent(
				group, NodeGroupSearchTermsChangedEvent.Action.ADD, toAdd
		));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_ADD.format(TagResolver.builder()
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("values", toList(toAdd)))
				.build()), sender);
	}

	public void searchTermsRemove(CommandSender sender, NodeGroup group, String commaSeparatedList) {
		Collection<String> toRemove = Arrays.stream(commaSeparatedList.split(","))
				.map(String::trim)
				.map(String::toLowerCase)
				.toList();
		group.getSearchTerms().removeAll(toRemove);

		Bukkit.getPluginManager().callEvent(new NodeGroupSearchTermsChangedEvent(
				group, NodeGroupSearchTermsChangedEvent.Action.REMOVE, toRemove
		));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_REMOVE.format(TagResolver.builder()
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("values", toList(toRemove)))
				.build()), sender);
	}

	private Component toList(Collection<String> tags) {
		return Component.join(JoinConfiguration.separator(Component.text(",", NamedTextColor.GRAY)), tags.stream()
				.map(Component::text).collect(Collectors.toList()));
	}

	public void renameGroup(CommandSender sender, NodeGroup group, String newName) {

		Component oldName = group.getDisplayName();
		NodeGroupHandler.getInstance().setNodeGroupName(group, newName);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_NAME.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", oldName))
				.resolver(Placeholder.component("new-name", group.getDisplayName()))
				.resolver(Placeholder.component("value", Component.text(group.getNameFormat())))
				.build()), sender);
	}

	public void setGroupPermission(CommandSender sender, NodeGroup group, String permission) {

		permission = permission.equalsIgnoreCase("null") || permission.equalsIgnoreCase("none") ? null : permission;

		@Nullable
		String oldValue = group.getPermission();
		NodeGroupHandler.getInstance().setNodeGroupPermission(group, permission);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_PERM.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("old-value", Messages.formatPermission(oldValue)))
				.resolver(Placeholder.component("value", Messages.formatPermission(group.getPermission())))
				.build()), sender);
	}

	public void setGroupNavigable(CommandSender sender, NodeGroup group, boolean value) {

		boolean oldValue = group.isNavigable();
		NodeGroupHandler.getInstance().setNodeGroupNavigable(group, value);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_NAVIGABLE.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("old-value", Messages.formatBool(oldValue)))
				.resolver(Placeholder.component("value", Messages.formatBool(group.isNavigable())))
				.build()), sender);
	}

	public void setGroupDiscoverable(CommandSender sender, NodeGroup group, boolean value) {

		boolean oldValue = group.isDiscoverable();
		NodeGroupHandler.getInstance().setNodeGroupDiscoverable(group, value);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_DISCOVERABLE.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("old-value", Messages.formatBool(oldValue).asComponent(sender)))
				.resolver(Placeholder.component("value", Messages.formatBool(group.isDiscoverable()).asComponent(sender)))
				.build()), sender);
	}

	public void setGroupDiscoverDist(CommandSender sender, NodeGroup group, float value) {

		float oldValue = group.getFindDistance();
		NodeGroupHandler.getInstance().setNodeGroupFindDistance(group, value);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_FIND_DIST.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("old-value", Component.text(oldValue)))
				.resolver(Placeholder.component("value", Component.text(group.getFindDistance())))
				.build()), sender);
	}

	public void deleteGroup(CommandSender sender, NodeGroup group) {

		NodeGroupHandler.getInstance().deleteNodeGroup(group);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), sender);
	}

	public void createGroup(CommandSender sender, NamespacedKey key) {

		try {
			NodeGroup group = NodeGroupHandler.getInstance().createNodeGroup(key, StringUtils.insertInRandomHexString(StringUtils.capizalize(key.getKey())));
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), sender);
		} catch (IllegalArgumentException e) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS
					.format(TagResolver.resolver("name", Tag.inserting(Component.text(key.toString())))), sender);
		} catch (Exception e) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE_FAIL, sender);
			e.printStackTrace();
		}
	}

	/**
	 * @param page first page is 1, not 0!
	 */
	public void listGroups(CommandSender sender, int page) {

		CommandUtils.printList(
				sender,
				page,
				10,
				new ArrayList<>(NodeGroupHandler.getInstance().getNodeGroups()),
				group -> {
					TagResolver r = TagResolver.builder()
							.resolver(Placeholder.component("key", Component.text(group.getKey().toString())))
							.resolver(Placeholder.component("name", group.getDisplayName()))
							.resolver(Placeholder.component("size", Component.text(group.size())))
							.resolver(Placeholder.component("findable", Component.text(group.isDiscoverable())))
							.build();
					TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_LINE.format(r), sender);
				},
				Messages.CMD_NG_LIST_HEADER,
				Messages.CMD_NG_LIST_FOOTER);
	}
}
