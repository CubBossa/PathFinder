package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;

public class RoadMapCommand extends CommandTree {

	public RoadMapCommand() {
		super("roadmap");
		withPermission(PathPlugin.PERM_CMD_RM);

		withAliases("rm");

		then(new LiteralArgument("info")
				.withPermission(PathPlugin.PERM_CMD_RM_INFO)
				.then(CustomArgs.roadMapArgument("roadmap")
						.executes((commandSender, args) -> {
							onInfo(commandSender, (RoadMap) args[0]);
						})));

		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_RM_CREATE)
				.then(new StringArgument("key")
						.executes((player, args) -> {
							onCreate(player, new NamespacedKey(PathPlugin.getInstance(), (String) args[0]));
						})));

		then(new LiteralArgument("delete")
				.withPermission(PathPlugin.PERM_CMD_RM_DELETE)
				.then(CustomArgs.roadMapArgument("roadmap")
						.executes((commandSender, args) -> {
							onDelete(commandSender, (RoadMap) args[0]);
						})));

		then(new LiteralArgument("editmode")
				.withPermission(PathPlugin.PERM_CMD_RM_EDITMODE)
				.executesPlayer((player, args) -> {
					if (RoadMapHandler.getInstance().getRoadMaps().size() != 1) {
						TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_PROVIDE_RM, player);
						return;
					}
					onEdit(player, RoadMapHandler.getInstance().getRoadMaps().values().iterator().next());
				})
				.then(CustomArgs.roadMapArgument("roadmap")
						.executesPlayer((player, objects) -> {
							onEdit(player, (RoadMap) objects[0]);
						})));

		then(new LiteralArgument("list")
				.withPermission(PathPlugin.PERM_CMD_RM_LIST)
				.executes((commandSender, args) -> {
					onList(commandSender, 1);
				})
				.then(new IntegerArgument("page", 1)
						.executes((commandSender, args) -> {
							onList(commandSender, (Integer) args[0]);
						})));

		then(new LiteralArgument("forcefind")
				.withPermission(PathPlugin.PERM_CMD_RM_FORCEFIND)
				.then(CustomArgs.roadMapArgument("roadmap")
						.then(new PlayerArgument("player")
								.then(CustomArgs.discoverableArgument("discovering")
										.executes((commandSender, args) -> {
											onForceFind(commandSender, (Player) args[1], (Discoverable) args[2]);
										})))));
		then(new LiteralArgument("forceforget")
				.withPermission(PathPlugin.PERM_CMD_RM_FORCEFORGET)
				.then(CustomArgs.roadMapArgument("roadmap")
						.then(new PlayerArgument("player")
								.then(CustomArgs.discoverableArgument("discovering")
										.executes((commandSender, args) -> {
											onForceForget(commandSender, (Player) args[1], (Discoverable) args[2]);
										})))));

		then(new LiteralArgument("edit")
				.then(CustomArgs.roadMapArgument("roadmap")
						.then(new LiteralArgument("visualizer")
								.withPermission(PathPlugin.PERM_CMD_RM_SET_VIS)
								.then(CustomArgs.pathVisualizerArgument("visualizer")
										.executes((commandSender, args) -> {
											onStyle(commandSender, (RoadMap) args[0], (PathVisualizer<?, ?>) args[1]);
										})))

						.then(new LiteralArgument("name")
								.withPermission(PathPlugin.PERM_CMD_RM_SET_NAME)
								.then(CustomArgs.miniMessageArgument("name")
										.executes((commandSender, args) -> {
											onRename(commandSender, (RoadMap) args[0], (String) args[1]);
										})))
						.then(new LiteralArgument("curve-length")
								.withPermission(PathPlugin.PERM_CMD_RM_SET_CURVE)
								.then(new DoubleArgument("curvelength", 0)
										.executes((commandSender, args) -> {
											onChangeTangentStrength(commandSender, (RoadMap) args[0], (Double) args[1]);
										})))));
	}

	public void onInfo(CommandSender sender, RoadMap roadMap) {

		FormattedMessage message = Messages.CMD_RM_INFO.format(TagResolver.builder()
				.tag("key", Messages.formatKey(roadMap.getKey()))
				.resolver(Placeholder.component("name", roadMap.getDisplayName()))
				.resolver(Placeholder.component("name-format", Component.text(roadMap.getNameFormat())))
				.resolver(Placeholder.component("nodes", Messages.formatNodeSelection(sender, roadMap.getNodes())))
				.resolver(Placeholder.component("groups", Messages.formatNodeGroups(sender, NodeGroupHandler.getInstance().getNodeGroups(roadMap))))
				.resolver(Placeholder.unparsed("curve-length", roadMap.getDefaultCurveLength() + ""))
				.resolver(Placeholder.component("path-visualizer", roadMap.getVisualizer() == null ? Messages.GEN_NULL.asComponent() : roadMap.getVisualizer().getDisplayName()))
				.build());

		TranslationHandler.getInstance().sendMessage(message, sender);
	}


	public void onCreate(CommandSender sender, NamespacedKey key) {

		try {
			RoadMap roadMap = RoadMapHandler.getInstance().createRoadMap(PathPlugin.getInstance(), key.getKey());
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_SUCCESS
					.format(TagResolver.resolver("name", Tag.inserting(roadMap.getDisplayName()))), sender);

		} catch (IllegalArgumentException e) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_DUPLICATE_KEY, sender);
		} catch (Exception e) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_FAIL, sender);
			e.printStackTrace();
		}
	}

	public void onDelete(CommandSender sender, RoadMap roadMap) throws WrapperCommandSyntaxException {

		RoadMapHandler.getInstance().deleteRoadMap(roadMap);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_DELETE.format(TagResolver.resolver("roadmap", Tag.inserting(roadMap.getDisplayName()))), sender);
	}

	public void onEdit(Player player, RoadMap roadMap) {

		TagResolver r = TagResolver.resolver("roadmap", Tag.inserting(roadMap.getDisplayName()));
		if (RoadMapHandler.getInstance().getRoadMapEditor(roadMap.getKey()).toggleEditMode(player.getUniqueId())) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_ACTIVATED.format(r), player);
		} else {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_DEACTIVATED.format(r), player);
		}
	}

	/**
	 * @param page first page is 1, not 0!
	 */
	public void onList(CommandSender sender, Integer page) {

		CommandUtils.printList(
				sender,
				page,
				10,
				new ArrayList<>(RoadMapHandler.getInstance().getRoadMaps().values()),
				roadMap -> {
					TagResolver r = TagResolver.builder()
							.tag("key", Messages.formatKey(roadMap.getKey()))
							.resolver(Placeholder.component("name", roadMap.getDisplayName()))
							.resolver(Placeholder.unparsed("curve-length", roadMap.getDefaultCurveLength() + ""))
							.resolver(Placeholder.component("path-visualizer", roadMap.getVisualizer() == null ? Messages.GEN_NULL.asComponent() : roadMap.getVisualizer().getDisplayName()))
							.build();

					TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_LIST_ENTRY.format(r), sender);
				},
				Messages.CMD_RM_LIST_HEADER,
				Messages.CMD_RM_LIST_FOOTER);
	}

	public void onForceFind(CommandSender sender, Player target, Discoverable discoverable) {

		DiscoverHandler.getInstance().discover(target.getUniqueId(), discoverable, new Date());

		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_FORCE_FIND.format(TagResolver.builder()
				.resolver(Placeholder.component("name", PathPlugin.getInstance().getAudiences().player(target).getOrDefault(Identity.DISPLAY_NAME, Component.text(target.getName()))))
				.tag("discovery", Tag.inserting(discoverable.getDisplayName())).build()), sender);
	}

	public void onForceForget(CommandSender sender, Player target, Discoverable discoverable) {

		DiscoverHandler.getInstance().forget(target.getUniqueId(), discoverable);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_FORCE_FORGET.format(TagResolver.builder()
				.resolver(Placeholder.component("name", PathPlugin.getInstance().getAudiences().player(target).getOrDefault(Identity.DISPLAY_NAME, Component.text(target.getName()))))
				.tag("discovery", Tag.inserting(discoverable.getDisplayName())).build()), sender);
	}

	public void onStyle(CommandSender sender, RoadMap roadMap, PathVisualizer<?, ?> visualizer) throws WrapperCommandSyntaxException {
		PathVisualizer<?, ?> old = roadMap.getVisualizer();

		if (!RoadMapHandler.getInstance().setRoadMapVisualizer(roadMap, visualizer)) {
			return;
		}

		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_VISUALIZER.format(TagResolver.builder()
				.tag("key", Messages.formatKey(roadMap.getKey()))
				.resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
				.resolver(Placeholder.component("old-value", old == null ? Messages.GEN_NULL.asComponent(sender) : old.getDisplayName()))
				.resolver(Placeholder.component("value", roadMap.getVisualizer().getDisplayName()))
				.build()), sender);
	}

	public void onRename(CommandSender sender, RoadMap roadMap, String nameNew) throws WrapperCommandSyntaxException {
		Component old = roadMap.getDisplayName();

		if (!RoadMapHandler.getInstance().setRoadMapName(roadMap, nameNew)) {
			return;
		}

		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_NAME.format(TagResolver.builder()
				.tag("key", Messages.formatKey(roadMap.getKey()))
				.resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
				.resolver(Placeholder.component("old-value", old))
				.resolver(Placeholder.unparsed("name-format", roadMap.getNameFormat()))
				.resolver(Placeholder.component("value", roadMap.getDisplayName()))
				.build()), sender);
	}

	public void onChangeTangentStrength(CommandSender sender, RoadMap roadMap, double strength) throws WrapperCommandSyntaxException {
		double old = roadMap.getDefaultCurveLength();

		if (!RoadMapHandler.getInstance().setRoadMapCurveLength(roadMap, strength)) {
			return;
		}

		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_CURVED.format(TagResolver.builder()
				.tag("key", Messages.formatKey(roadMap.getKey()))
				.resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
				.resolver(Placeholder.component("old-value", Component.text(old)))
				.resolver(Placeholder.component("value", Component.text(roadMap.getDefaultCurveLength())))
				.build()), sender);
	}
}