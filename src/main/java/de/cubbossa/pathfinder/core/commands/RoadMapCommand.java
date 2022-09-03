package de.cubbossa.pathfinder.core.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.argument.CustomArgs;
import de.cubbossa.pathfinder.core.events.roadmap.RoadmapSelectEvent;
import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.data.PathPlayer;
import de.cubbossa.pathfinder.data.PathPlayerHandler;
import de.cubbossa.pathfinder.module.discovering.DiscoverHandler;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Predicate;

public class RoadMapCommand extends CommandTree implements Listener {

	public RoadMapCommand() {
		super("roadmap");
		withPermission(PathPlugin.PERM_CMD_RM);

		// to update the roadmap selected requirement
		Bukkit.getPluginManager().registerEvents(this, PathPlugin.getInstance());

		withAliases("rm");

		then(new LiteralArgument("info")
				.withPermission(PathPlugin.PERM_CMD_RM_INFO)
				.executes((commandSender, objects) -> {
					onInfo(commandSender, null);
				})
				.then(CustomArgs.roadMapArgument("roadmap")
						.executes((commandSender, args) -> {
							onInfo(commandSender, (RoadMap) args[0]);
						})));

		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_RM_CREATE)
				.then(new StringArgument("key")
						.executesPlayer((player, args) -> {
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
				.withRequirement(hasRoadMapSelected())
				.executesPlayer((player, args) -> {
					RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(
							PathPlayerHandler.getInstance().getPlayer(player).getSelectedRoadMap());
					onEdit(player, roadMap);
				})
		);

		then(new LiteralArgument("editmode")
				.withPermission(PathPlugin.PERM_CMD_RM_EDITMODE)
				.then(CustomArgs.roadMapArgument("roadmap")
						.executesPlayer((player, args) -> {
							onEdit(player, (RoadMap) args[0]);
						})));

		then(new LiteralArgument("list")
				.withPermission(PathPlugin.PERM_CMD_RM_LIST)
				.executes((commandSender, args) -> {
					onList(commandSender, 0);
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

		then(new LiteralArgument("select")
				.withPermission(PathPlugin.PERM_CMD_RM_SELECT)
				.then(CustomArgs.roadMapArgument("roadmap")
						.executes((commandSender, objects) -> {
							onSelect(commandSender, (RoadMap) objects[0]);
						})));
		then(new LiteralArgument("deselect")
				.withRequirement(hasRoadMapSelected())
				.withPermission(PathPlugin.PERM_CMD_RM_DESELECT)
				.executes((commandSender, args) -> {
					onDeselect(commandSender);
				}));

		then(new LiteralArgument("set")
				.withRequirement(hasRoadMapSelected())
				.then(new LiteralArgument("visualizer")
						.withPermission(PathPlugin.PERM_CMD_RM_SET_VIS)
						.then(CustomArgs.pathVisualizerArgument("visualizer")
								.executes((commandSender, args) -> {
									onStyle(commandSender, null, (PathVisualizer) args[0]);
								})))

				.then(new LiteralArgument("name")
						.withPermission(PathPlugin.PERM_CMD_RM_SET_NAME)
						.then(CustomArgs.miniMessageArgument("name")
								.executes((commandSender, args) -> {
									onRename(commandSender, null, (String) args[0]);
								})))
				.then(new LiteralArgument("curve-length")
						.withPermission(PathPlugin.PERM_CMD_RM_SET_CURVE)
						.then(new DoubleArgument("curvelength", 0)
								.executes((commandSender, args) -> {
									onChangeTangentStrength(commandSender, null, (Double) args[0]);
								}))));

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

	private Predicate<CommandSender> hasRoadMapSelected() {
		return sender -> PathPlayerHandler.getInstance().getPlayer(sender).getSelectedRoadMap() != null;
	}

	public void onInfo(CommandSender sender, @Nullable RoadMap roadMap) throws WrapperCommandSyntaxException {
		if (roadMap == null) {
			roadMap = CustomArgs.resolveRoadMapWrappedException(sender);
		}

		FormattedMessage message = Messages.CMD_RM_INFO.format(TagResolver.builder()
				.tag("key", Messages.formatKey(roadMap.getKey()))
				.resolver(Placeholder.component("name", roadMap.getDisplayName()))
				.resolver(Placeholder.component("name-format", Component.text(roadMap.getNameFormat())))
				.resolver(Placeholder.component("nodes", Messages.formatNodeSelection(sender, roadMap.getNodes())))
				.resolver(Placeholder.component("groups", Messages.formatNodeGroups(sender, NodeGroupHandler.getInstance().getNodeGroups(roadMap))))
				.resolver(Placeholder.unparsed("curve-length", roadMap.getDefaultBezierTangentLength() + ""))
				.resolver(Placeholder.component("path-visualizer", roadMap.getVisualizer() == null ? Messages.GEN_NULL.asComponent() : roadMap.getVisualizer().getDisplayName()))
				.build());

		TranslationHandler.getInstance().sendMessage(message, sender);
	}


	public void onCreate(CommandSender sender, NamespacedKey key) {

		try {
			RoadMap roadMap = RoadMapHandler.getInstance().createRoadMap(key);
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_SUCCESS
					.format(TagResolver.resolver("name", Tag.inserting(roadMap.getDisplayName()))), sender);

			PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);
			pathPlayer.setSelectedRoadMap(roadMap.getKey());

		} catch (Exception e) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_FAIL, sender);
			e.printStackTrace();
		}
	}

	public void onDelete(CommandSender sender, @Nullable RoadMap roadMap) throws WrapperCommandSyntaxException {
		if (roadMap == null) {
			roadMap = CustomArgs.resolveRoadMapWrappedException(sender);
		}

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

		PathPlayer player = PathPlayerHandler.getInstance().getPlayer(sender);
		NamespacedKey selection = player.getSelectedRoadMap();

		CommandUtils.printList(
				sender,
				page,
				10,
				new ArrayList<>(RoadMapHandler.getInstance().getRoadMaps().values()),
				roadMap -> {
					TagResolver r = TagResolver.builder()
							.tag("key", Messages.formatKey(roadMap.getKey()))
							.resolver(Placeholder.component("name", roadMap.getDisplayName()))
							.resolver(Placeholder.unparsed("curve-length", roadMap.getDefaultBezierTangentLength() + ""))
							.resolver(Placeholder.component("path-visualizer", roadMap.getVisualizer() == null ? Messages.GEN_NULL.asComponent() : roadMap.getVisualizer().getDisplayName()))
							.build();

					TranslationHandler.getInstance().sendMessage(
							(roadMap.getKey().equals(selection) ? Messages.CMD_RM_LIST_SELECTED : Messages.CMD_RM_LIST_ENTRY)
									.format(r),
							sender);
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

	public void onSelect(CommandSender sender, RoadMap roadMap) {
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);

		pathPlayer.setSelectedRoadMap(roadMap.getKey());
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SELECT.format(TagResolver.resolver("name", Tag.inserting(roadMap.getDisplayName()))), sender);
	}

	public void onDeselect(CommandSender sender) {
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);

		pathPlayer.deselectRoadMap();
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_DESELECT, sender);
	}

	public void onStyle(CommandSender sender, @Nullable RoadMap roadMap, PathVisualizer<?, ?> visualizer) throws WrapperCommandSyntaxException {
		roadMap = roadMap != null ? roadMap : CustomArgs.resolveRoadMapWrappedException(sender);
		PathVisualizer<?, ?> old = roadMap.getVisualizer();

		if (!RoadMapHandler.getInstance().setRoadMapVisualizer(roadMap, visualizer)) {
			return;
		}

		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_VISUALIZER.format(TagResolver.builder()
				.tag("key", Messages.formatKey(roadMap.getKey()))
				.resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
				.resolver(Placeholder.component("old-value", old.getDisplayName()))
				.resolver(Placeholder.component("value", roadMap.getVisualizer().getDisplayName()))
				.build()), sender);
	}

	public void onRename(CommandSender sender, @Nullable RoadMap roadMap, String nameNew) throws WrapperCommandSyntaxException {
		roadMap = roadMap != null ? roadMap : CustomArgs.resolveRoadMapWrappedException(sender);
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

	public void onChangeTangentStrength(CommandSender sender, @Nullable RoadMap roadMap, double strength) throws WrapperCommandSyntaxException {
		roadMap = roadMap != null ? roadMap : CustomArgs.resolveRoadMapWrappedException(sender);
		double old = roadMap.getDefaultBezierTangentLength();

		if (!RoadMapHandler.getInstance().setRoadMapCurveLength(roadMap, strength)) {
			return;
		}

		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_CURVED.format(TagResolver.builder()
				.tag("key", Messages.formatKey(roadMap.getKey()))
				.resolver(Placeholder.component("roadmap", roadMap.getDisplayName()))
				.resolver(Placeholder.component("old-value", Component.text(old)))
				.resolver(Placeholder.component("value", Component.text(roadMap.getDefaultBezierTangentLength())))
				.build()), sender);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSelect(RoadmapSelectEvent event) {
		CommandAPI.updateRequirements(event.getPlayer());
	}
}

	/*

	@Subcommand("style")
	@CommandPermission("pathfinder.command.roadmap.style")
	public class RoadmapStyleCommand extends BaseCommand {

		@Subcommand("add")
		@CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER_STYLES)
		@Syntax("<Style>")
		public void onAdd(CommandSender sender, ParticleVisualizer ParticleVisualizer) {
			if (!ParticleVisualizer.isPickable()) {
				PlayerUtils.sendMessage(sender, ChatColor.RED + "Dieser Visualizer ist nicht auswählbar. Konfiguriere ihn mit /path-visualizer.");
				return;
			}
			RoadMap roadMap = CustomArgs.resolveRoadMap(sender);
			Collection<ParticleVisualizer> list = VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getKey(), new ArrayList<>());
			list.add(ParticleVisualizer);
			VisualizerHandler.getInstance().getRoadmapVisualizers().put(roadMap.getKey(), list);
			SqlStorage.getInstance().addStyleToRoadMap(roadMap, ParticleVisualizer);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Style hinzugefügt: " + PathPlugin.COLOR_LIGHT + ParticleVisualizer.getNameFormat());
		}


		@Subcommand("remove")
		@CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER_STYLES)
		@Syntax("<Style>")
		public void onRemove(CommandSender sender, ParticleVisualizer visualizer) {
			if (!visualizer.isPickable()) {
				PlayerUtils.sendMessage(sender, ChatColor.RED + "Dieser Visualizer ist nicht auswählbar. Konfiguriere ihn mit /path-visualizer.");
				return;
			}
			RoadMap roadMap = CustomArgs.resolveRoadMap(sender);
			SqlStorage.getInstance().removeStyleFromRoadMap(roadMap, visualizer);
			Collection<ParticleVisualizer> list = VisualizerHandler.getInstance().getRoadmapVisualizers().get(roadMap.getKey());
			if (list != null) {
				list.remove(visualizer);
			}
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Style entfernt: " + PathPlugin.COLOR_LIGHT + visualizer.getNameFormat());
		}

		@Subcommand("list")
		public void onList(CommandSender sender) {
			Menu menu = new Menu("Alle Styles dieser Roadmap:");
			RoadMap roadMap = CustomArgs.resolveRoadMap(sender);
			for (ParticleVisualizer visualizer : VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getKey(), new ArrayList<>())) {
				menu.addSub(new ComponentMenu(Component.empty()
						.append(visualizer.getDisplayName())
						.append(Component.text(" [X]", NamedTextColor.RED)
								.clickEvent(ClickEvent.runCommand("/roadmap style remove " + visualizer.getNameFormat()))
								.hoverEvent(HoverEvent.showText(Component.text("Klicke zum Entfernen."))))));
			}
			PlayerUtils.sendComponents(sender, menu.toComponents());
		}
	}

	@Subcommand("test")
	@CommandPermission("pathfinder.command.roadmap.test")
	public class RoadMapTestCommand extends BaseCommand {

		@Subcommand("navigate")
		@Syntax("<Findable>")
		@CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION)
		public void onTestNavigate(Player player, Waypoint findable) {
			RoadMap roadMap = CustomArgs.resolveRoadMap(player);

			PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			if (!AStarUtils.startPath(pPlayer, new PlayerNode(player, roadMap), findable, true)) {
				PlayerUtils.sendMessage(player, ChatColor.RED + "Es konnte kein kürzester Pfad ermittelt werden.");
				return;
			}

			player.sendMessage(PathPlugin.PREFIX_COMP
					.append(Component.text("Testpfad gestartet. (", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
					.append(ComponentUtils.getCommandComponent("/cancelpath", ClickEvent.Action.RUN_COMMAND))
					.append(Component.text(")", NamedTextColor.GRAY)));
		}

		@Subcommand("find")
		@Syntax("<Findable>")
		@CommandCompletion(PathPlugin.COMPLETE_FINDABLES_FINDABLE)
		public void onTestFind(Player player, Waypoint findable) {
			RoadMap roadMap = CustomArgs.resolveRoadMap(player);

			PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			if (!AStarUtils.startPath(pPlayer, new PlayerNode(player, roadMap), findable, false)) {
				PlayerUtils.sendMessage(player, ChatColor.RED + "Es konnte kein kürzester Pfad ermittelt werden.");
				return;
			}

			player.sendMessage(PathPlugin.PREFIX_COMP
					.append(Component.text("Testpfad gestartet. (", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
					.append(ComponentUtils.getCommandComponent("/cancelpath", ClickEvent.Action.RUN_COMMAND))
					.append(Component.text(")", NamedTextColor.GRAY)));
		}
	}*/
