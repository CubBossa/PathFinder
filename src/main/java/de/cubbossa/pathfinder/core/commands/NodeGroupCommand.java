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
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class NodeGroupCommand extends CommandTree {

	public NodeGroupCommand(int offset) {
		super("nodegroup");

		then(new LiteralArgument("list")
				.withPermission(PathPlugin.PERM_CMD_NG_LIST)
				.executesPlayer((player, objects) -> {
					listGroups(player, 0);
				})
				.then(new IntegerArgument("page")
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
				.then(new LiteralArgument("findable")
						.withPermission(PathPlugin.PERM_CMD_NG_SET_FINDABLE)
						.then(CustomArgs.nodeGroupArgument("group")
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
		group.setDiscoverable(findable);

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

		NodeGroup group = NodeGroupHandler.getInstance().createNodeGroup(key, true, StringUtils.getRandHexString() + key.getKey());
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
					.tag("id", Tag.inserting(Component.text(group.getKey().toString())))
					.tag("name", Tag.inserting(group.getDisplayName()))
					.tag("size", Tag.inserting(Component.text(group.size())))
					.tag("findable", Tag.inserting(Component.text(group.isDiscoverable())))
					.build();
			TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_LINE.format(resolver, r), player);
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_NG_LIST_FOOTER.format(resolver), player);
	}
}
