package de.cubbossa.pathfinder.core.commands;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
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
import org.bukkit.entity.Player;

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
				.executesPlayer((player, objects) -> {
					listGroups(player, 0);
				})
				.then(new IntegerArgument("page", 1)
						.executesPlayer((player, objects) -> {
							listGroups(player, (int) objects[offset]);
						})));

		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_NG_CREATE)
				.then(new StringArgument("name")
						.executesPlayer((player, args) -> {
							createGroup(player, new NamespacedKey(PathPlugin.getInstance(), args[offset].toString()));
						})));

		then(new LiteralArgument("delete")
				.withPermission(PathPlugin.PERM_CMD_NG_DELETE)
				.then(CustomArgs.nodeGroupArgument("group")
						.executesPlayer((player, objects) -> {
							deleteGroup(player, (NodeGroup) objects[offset]);
						})));

		then(new LiteralArgument("search-terms")
				.then(new LiteralArgument("add")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_ADD)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(CustomArgs.suggestCommaSeparatedList("search-terms")
										.executesPlayer((player, objects) -> {
											searchTermsAdd(player, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										}))))
				.then(new LiteralArgument("remove")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_REMOVE)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(CustomArgs.suggestCommaSeparatedList("search-terms")
										.executesPlayer((player, objects) -> {
											searchTermsRemove(player, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										}))))
				.then(new LiteralArgument("list")
						.withPermission(PathPlugin.PERM_CMD_NG_ST_LIST)
						.then(CustomArgs.nodeGroupArgument("group")
								.executesPlayer((player, objects) -> {
									searchTermsList(player, (NodeGroup) objects[offset]);
								}))));
		then(new LiteralArgument("set")
				.then(new LiteralArgument("name")
						.withPermission(PathPlugin.PERM_CMD_NG_SET_NAME)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(CustomArgs.miniMessageArgument("name", i -> Lists.newArrayList(((NodeGroup) i.previousArgs()[0]).getNameFormat()))
										.executesPlayer((player, objects) -> {
											renameGroup(player, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										})
								)))
				.then(new LiteralArgument("permission")
						.withPermission(PathPlugin.PERM_CMD_NG_SET_PERM)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(new GreedyStringArgument("permission")
										.executesPlayer((player, objects) -> {
											setGroupPermission(player, (NodeGroup) objects[offset], (String) objects[offset + 1]);
										})
								)))
				.then(new LiteralArgument("navigable")
						.withPermission(PathPlugin.PERM_CMD_NG_SET_NAVIGABLE)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(new BooleanArgument("value")
										.executesPlayer((player, objects) -> {
											setGroupNavigable(player, (NodeGroup) objects[offset], (Boolean) objects[offset + 1]);
										})
								)))
				.then(new LiteralArgument("discoverable")
						.withPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVERABLE)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(new BooleanArgument("value")
										.executesPlayer((player, objects) -> {
											setGroupDiscoverable(player, (NodeGroup) objects[offset], (Boolean) objects[offset + 1]);
										}))))
				.then(new LiteralArgument("find-distance")
						.withPermission(PathPlugin.PERM_CMD_NG_SET_DISCOVER_DIST)
						.then(CustomArgs.nodeGroupArgument("group")
								.then(new FloatArgument("value", 0.01f)
										.executesPlayer((player, objects) -> {
											setGroupDiscoverDist(player, (NodeGroup) objects[offset], (Float) objects[offset + 1]);
										})))));
	}

	public void searchTermsList(Player player, NodeGroup group) {
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_TERMS_LIST.format(TagResolver.builder()
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("values", toList(group.getSearchTerms())))
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
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("values", toList(toAdd)))
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
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("values", toList(toRemove)))
				.build()), player);
	}

	private Component toList(Collection<String> tags) {
		return Component.join(JoinConfiguration.separator(Component.text(",", NamedTextColor.GRAY)), tags.stream()
				.map(Component::text).collect(Collectors.toList()));
	}

	public void renameGroup(Player player, NodeGroup group, String newName) {

		Component oldName = group.getDisplayName();
		NodeGroupHandler.getInstance().setNodeGroupName(group, newName);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_NAME.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", oldName))
				.resolver(Placeholder.component("new-name", group.getDisplayName()))
				.resolver(Placeholder.component("value", Component.text(group.getNameFormat())))
				.build()), player);
	}

	public void setGroupPermission(Player player, NodeGroup group, String permission) {

		permission = permission.equalsIgnoreCase("null") || permission.equalsIgnoreCase("none") ? null : permission;

		@Nullable
		String oldValue = group.getPermission();
		NodeGroupHandler.getInstance().setNodeGroupPermission(group, permission);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_PERM.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("old-value", Messages.formatPermission(oldValue)))
				.resolver(Placeholder.component("value", Messages.formatPermission(group.getPermission())))
				.build()), player);
	}

	public void setGroupNavigable(Player player, NodeGroup group, boolean value) {

		boolean oldValue = group.isNavigable();
		NodeGroupHandler.getInstance().setNodeGroupNavigable(group, value);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_NAVIGABLE.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("old-value", Messages.formatBool(oldValue)))
				.resolver(Placeholder.component("value", Messages.formatBool(group.isNavigable())))
				.build()), player);
	}

	public void setGroupDiscoverable(Player player, NodeGroup group, boolean value) {

		boolean oldValue = group.isDiscoverable();
		NodeGroupHandler.getInstance().setNodeGroupDiscoverable(group, value);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_DISCOVERABLE.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("old-value", Messages.formatBool(oldValue).asComponent(player)))
				.resolver(Placeholder.component("value", Messages.formatBool(group.isDiscoverable()).asComponent(player)))
				.build()), player);
	}

	public void setGroupDiscoverDist(Player player, NodeGroup group, float value) {

		float oldValue = group.getFindDistance();
		NodeGroupHandler.getInstance().setNodeGroupFindDistance(group, value);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_SET_FIND_DIST.format(TagResolver.builder()
				.tag("key", Messages.formatKey(group.getKey()))
				.resolver(Placeholder.component("name", group.getDisplayName()))
				.resolver(Placeholder.component("old-value", Component.text(oldValue)))
				.resolver(Placeholder.component("value", Component.text(group.getFindDistance())))
				.build()), player);
	}

	public void deleteGroup(Player player, NodeGroup group) {

		NodeGroupHandler.getInstance().removeNodeGroup(group);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_DELETE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), player);
	}

	public void createGroup(Player player, NamespacedKey key) {

		if (NodeGroupHandler.getInstance().getNodeGroup(key) != null) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_ALREADY_EXISTS
					.format(TagResolver.resolver("name", Tag.inserting(Component.text(key.toString())))), player);
			return;
		}

		NodeGroup group = NodeGroupHandler.getInstance().createNodeGroup(key, StringUtils.getRandHexString() + key.getKey());
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_CREATE.format(TagResolver.resolver("name", Tag.inserting(group.getDisplayName()))), player);
	}

	public void listGroups(Player player, int page) {

		TagResolver resolver = TagResolver.builder()
				.tag("page", Tag.preProcessParsed(page + 1 + ""))
				.tag("prev-page", Tag.preProcessParsed(Integer.max(1, page + 1) + ""))
				.tag("next-page", Tag.preProcessParsed(Integer.min((int) Math.ceil(RoadMapHandler.getInstance().getRoadMaps().size() / 10.), page + 3) + ""))
				.tag("pages", Tag.preProcessParsed((int) Math.ceil(RoadMapHandler.getInstance().getRoadMaps().size() / 10.) + ""))
				.build();

		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_HEADER.format(resolver), player);

		for (NodeGroup group : CommandUtils.subList(new ArrayList<>(NodeGroupHandler.getInstance().getNodeGroups()), page, 10)) {

			TagResolver r = TagResolver.builder()
					.resolver(Placeholder.component("key", Component.text(group.getKey().toString())))
					.resolver(Placeholder.component("name", group.getDisplayName()))
					.resolver(Placeholder.component("size", Component.text(group.size())))
					.resolver(Placeholder.component("findable", Component.text(group.isDiscoverable())))
					.build();
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_LINE.format(resolver, r), player);
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_FOOTER.format(resolver), player);
	}
}
