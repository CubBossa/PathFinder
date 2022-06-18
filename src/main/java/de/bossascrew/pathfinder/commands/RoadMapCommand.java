package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.PathPlayerHandler;
import de.bossascrew.pathfinder.node.Findable;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.roadmap.RoadMapEditor;
import de.bossascrew.pathfinder.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.SelectionUtils;
import de.bossascrew.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@CommandAlias("roadmap")
public class RoadMapCommand extends BaseCommand {

	@Subcommand("info")
	@Syntax("<roadmap>")
	@CommandPermission("pathfinder.command.roadmap.info")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onInfo(CommandSender sender, @Optional RoadMap roadMap) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(sender);
		}

		FormattedMessage message = Messages.CMD_RM_INFO.format(TagResolver.builder()
				.tag("id", Tag.preProcessParsed(roadMap.getKey() + ""))
				.tag("name", Tag.inserting(roadMap.getDisplayName()))
				.tag("name-format", Tag.inserting(Component.text(roadMap.getNameFormat())))
				.tag("world", Tag.preProcessParsed(roadMap.getWorld().getName()))
				.tag("findable", Tag.inserting(roadMap.isFindableNodes() ?
						Messages.GEN_TRUE.asComponent(sender) : Messages.GEN_FALSE.asComponent(sender)))
				.tag("find-distance", Tag.preProcessParsed(roadMap.getNodeFindDistance() + ""))
				.tag("curve-length", Tag.preProcessParsed(roadMap.getDefaultBezierTangentLength() + ""))
				.tag("path-visualizer", Tag.inserting(roadMap.getVisualizer() == null ? Messages.GEN_NULL.asComponent() : roadMap.getVisualizer().getDisplayName()))
				.build());

		TranslationHandler.getInstance().sendMessage(message, sender);
	}

	@Subcommand("create")
	@Syntax("<key> [<world>] [findable]")
	@CommandPermission("pathfinder.command.roadmap.create")
	@CommandCompletion("@nothing @worlds findable")
	public void onCreate(Player player, NamespacedKey key,
						 @Optional @Values("@worlds") World world,
						 @Optional @Single @Values("findable") String findable) {

		boolean findableNodes = findable != null;
		if (world == null) {
			world = player.getWorld();
		}

		try {
			RoadMap roadMap = RoadMapHandler.getInstance().createRoadMap(key, world, findableNodes);
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_SUCCESS
					.format(TagResolver.resolver("name", Tag.inserting(roadMap.getDisplayName()))), player);

			PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			pathPlayer.setSelectedRoadMap(roadMap.getKey());

		} catch (Exception e) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_FAIL, player);
			e.printStackTrace();
		}
	}

	@Subcommand("delete")
	@Syntax("<roadmap>")
	@CommandPermission("pathfinder.command.roadmap.delete")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onDelete(CommandSender sender, @Optional RoadMap roadMap) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(sender);
		}

		RoadMapHandler.getInstance().deleteRoadMap(roadMap);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_DELETE.format(TagResolver.resolver("roadmap", Tag.inserting(roadMap.getDisplayName()))), sender);
	}

	@Subcommand("editmode")
	@CommandPermission("pathfinder.command.roadmap.editmode")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	@Syntax("[<roadmap>]")
	public void onEdit(Player player, @Optional RoadMap roadMap) {
		PathPlayer pp = PathPlayerHandler.getInstance().getPlayer(player);
		if (pp == null) {
			return;
		}
		if (roadMap == null) {
			if (RoadMapHandler.getInstance().getRoadMaps().size() == 0) {
				TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_CREATE, player);
				return;
			}
			if (pp.getSelectedRoadMap() != null) {
				roadMap = RoadMapHandler.getInstance().getRoadMap(pp.getSelectedRoadMap());
			}
		}
		if (roadMap == null) {
			if (RoadMapHandler.getInstance().getRoadMaps().size() == 1) {
				roadMap = RoadMapHandler.getInstance().getRoadMaps().values().stream().findAny().orElse(null);
			} else {
				Collection<RoadMap> inWorld = RoadMapHandler.getInstance().getRoadMaps(player.getWorld());
				if (inWorld.size() == 1) {
					roadMap = inWorld.stream().findAny().orElse(null);
				}
			}
			if (roadMap != null) {
				pp.setSelectedRoadMap(roadMap.getKey());
				TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_SELECTED.format(TagResolver.resolver("roadmap",
						Tag.inserting(roadMap.getDisplayName()))), player);
			}
		}
		if (roadMap == null) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_SELECT, player);
			return;
		}
		RoadMapHandler.getInstance().getRoadMapEditor(roadMap.getKey()).toggleEditMode(player.getUniqueId());
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_EM_ACTIVATED.format(TagResolver.resolver("roadmap",
				Tag.inserting(roadMap.getDisplayName()))), player);
	}

	@Subcommand("list")
	@CommandPermission("pathfinder.command.roadmap.list")
	@Syntax("[<page>]")
	public void onList(CommandSender sender, @Optional Integer page) {

		page = page == null ? 0 : page - 1;

		PathPlayer player = PathPlayerHandler.getInstance().getPlayer(sender);
		NamespacedKey selection = player.getSelectedRoadMap();

		TagResolver resolver = TagResolver.builder()
				.tag("page", Tag.preProcessParsed(page + 1 + ""))
				.tag("prev-page", Tag.preProcessParsed(Integer.max(1, page + 1) + ""))
				.tag("next-page", Tag.preProcessParsed(Integer.min((int) Math.ceil(RoadMapHandler.getInstance().getRoadMaps().size() / 10.), page + 3) + ""))
				.tag("pages", Tag.preProcessParsed((int) Math.ceil(RoadMapHandler.getInstance().getRoadMaps().size() / 10.) + ""))
				.build();

		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_LIST_HEADER.format(resolver), sender);

		for (RoadMap roadMap : CommandUtils.subList(new ArrayList<>(RoadMapHandler.getInstance().getRoadMaps().values()), page, 10)) {
			TagResolver r = TagResolver.builder()
					.tag("id", Tag.preProcessParsed(roadMap.getKey().getKey() + ""))
					.tag("name", Tag.inserting(roadMap.getDisplayName()))
					.tag("world", Tag.preProcessParsed(roadMap.getWorld().getName()))
					.tag("findable", Tag.inserting(roadMap.isFindableNodes() ?
							Messages.GEN_TRUE.asComponent(sender) : Messages.GEN_FALSE.asComponent(sender)))
					.tag("find-distance", Tag.preProcessParsed(roadMap.getNodeFindDistance() + ""))
					.tag("curve-length", Tag.preProcessParsed(roadMap.getDefaultBezierTangentLength() + ""))
					.tag("path-visualizer", Tag.inserting(roadMap.getVisualizer() == null ? Messages.GEN_NULL.asComponent() : roadMap.getVisualizer().getDisplayName()))
					.build();

			TranslationHandler.getInstance().sendMessage(
					(roadMap.getKey().equals(selection) ? Messages.CMD_RM_LIST_SELECTED : Messages.CMD_RM_LIST_ENTRY)
							.format(resolver, r),
					sender);
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_LIST_FOOTER.format(resolver), sender);
	}

	@Subcommand("forcefind")
	@Syntax("<roadmap> <player> <nodes> [group]")
	@CommandPermission("pathfinder.command.roadmap.forcefind")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " @players " + PathPlugin.COMPLETE_NODE_SELECTION)
	public void onForceFind(CommandSender sender, RoadMap roadMap, Player target, @Single NodeSelection selection,
							@Optional @Single @Values("group") String group) {

		boolean findSingle = group != null;
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(target.getUniqueId());
		if (pathPlayer == null) {
			return;
		}

		for (Node node : selection) {
			if(node instanceof Findable findable) {
				pathPlayer.find(findable, !findSingle, new Date());
			}
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_FORCE_FIND.format(TagResolver.builder()
				.tag("name", Tag.inserting(PathPlugin.getInstance().getAudiences().player(target).getOrDefault(Identity.DISPLAY_NAME, Component.text(target.getName()))))
				.tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))
				.build()), sender);
	}

	@Subcommand("forceforget")
	@Syntax("<roadmap> <player> <nodes> [grouped]")
	@CommandPermission("pathfinder.command.roadmap.forceforget")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " @players " + PathPlugin.COMPLETE_FINDABLES_FOUND + " ungruppiert")
	public void onForceForget(CommandSender sender, RoadMap roadMap, Player target, @Single NodeSelection selection,
							  @Optional @Single String grouped) {

		boolean group = grouped != null && grouped.equalsIgnoreCase("grouped");
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(target.getUniqueId());
		if (pathPlayer == null) {
			return;
		}

		for (Node node : selection) {
			if (node instanceof Findable findable) {
				pathPlayer.forget(findable, group);
			}
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_FORCE_FORGET.format(TagResolver.builder()
				.tag("name", Tag.inserting(PathPlugin.getInstance().getAudiences().player(target).getOrDefault(Identity.DISPLAY_NAME, Component.text(target.getName()))))
				.tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection))).build()), sender);
	}

	@Subcommand("select")
	@Syntax("<Straßenkarte>")
	@CommandPermission("pathfinder.command.roadmap.select")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onSelect(CommandSender sender, RoadMap roadMap) {
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);

		pathPlayer.setSelectedRoadMap(roadMap.getKey());
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SELECT.format(TagResolver.resolver("name", Tag.inserting(roadMap.getDisplayName()))), sender);
	}

	@Subcommand("deselect")
	@CommandPermission("pathfinder.command.roadmap.select")
	public void onDeselect(CommandSender sender) {
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);

		pathPlayer.deselectRoadMap();
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_DESELECT, sender);
	}

	@Subcommand("set")
	public class RoadMapSetCommand extends BaseCommand {

		@Subcommand("path-visualizer")
		@Syntax("<Style>")
		@CommandPermission("pathfinder.command.roadmap.set.path-visualizer")
		@CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
		public void onStyle(CommandSender sender, PathVisualizer visualizer) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			roadMap.setVisualizer(visualizer);

			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_VISUALIZER.format(TagResolver.builder()
					.tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
					.tag("visualizer", Tag.inserting(visualizer.getDisplayName()))
					.build()), sender);
		}

		@Subcommand("name")
		@Syntax("<new name>")
		@CommandPermission("pathfinder.command.roadmap.set.name")
		public void onRename(CommandSender sender, @Single String nameNew) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			Component old = roadMap.getDisplayName();
			roadMap.setNameFormat(nameNew);

			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_NAME.format(TagResolver.builder()
					.tag("roadmap", Tag.inserting(old))
					.tag("name-format", Tag.inserting(Component.text(nameNew)))
					.tag("display-name", Tag.preProcessParsed(nameNew))
					.build()), sender);
		}

		@Subcommand("curve-length")
		@Syntax("<value>")
		@CommandPermission("pathfinder.command.roadmap.set.curved")
		public void onChangeTangentStrength(CommandSender sender, double strength) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			roadMap.setDefaultBezierTangentLength(strength);
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_CURVED.format(TagResolver.builder()
					.tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
					.tag("value", Tag.preProcessParsed(String.format("%,.2f", strength)))
					.build()), sender);
		}

		@Subcommand("world")
		@Syntax("<world> [force]")
		@CommandPermission("pathfinder.command.roadmap.set.world")
		@CommandCompletion("@worlds force")
		public void onChangeWorld(CommandSender sender, World world, @Optional @Single @Values("force") String forceString) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			RoadMapEditor editor = RoadMapHandler.getInstance().getRoadMapEditor(roadMap.getKey());
			boolean force = forceString != null;

			if (!force && editor.isEdited()) {
				TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CURRENTLY_EDITED, sender);
				return;
			}
			if (editor.isEdited()) {
				editor.cancelEditModes();
			}
			roadMap.setWorld(world);
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_WORLD.format(TagResolver.builder()
					.tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
					.tag("world", Tag.inserting(Component.text(world.getName())))
					.build()), sender);
		}

		@Subcommand("find-distance")
		@Syntax("<distance>")
		@CommandPermission("pathfinder.command.roadmap.set.find-distance")
		public void onFindDistance(CommandSender sender, double findDistance) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);

			TagResolver resolver = TagResolver.builder()
					.tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
					.tag("value", Tag.preProcessParsed(String.format("%,.2f", findDistance)))
					.build();

			if (findDistance < 0.05) {
				TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_FIND_DIST_TOO_SMALL.format(resolver), sender);
				return;
			}
			roadMap.setNodeFindDistance(findDistance);
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_FIND_DIST.format(resolver), sender);
		}

		@Subcommand("findable")
		@Syntax("<findable>")
		@CommandPermission("pathfinder.command.roadmap.set.findable")
		@CommandCompletion("true|false")
		public void onSetFindable(CommandSender sender, @Optional Boolean findable) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			roadMap.setFindableNodes(findable);
			findable = findable != null && findable;

			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_SET_FINDABLE.format(TagResolver.builder()
					.tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
					.tag("value", Tag.inserting(Messages.formatBool(findable)))
					.build()), sender);
		}
	}

	/*

	@Subcommand("style")
	@CommandPermission("pathfinder.command.roadmap.style")
	public class RoadmapStyleCommand extends BaseCommand {

		@Subcommand("add")
		@CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER_STYLES)
		@Syntax("<Style>")
		public void onAdd(CommandSender sender, SimpleCurveVisualizer simpleCurveVisualizer) {
			if (!simpleCurveVisualizer.isPickable()) {
				PlayerUtils.sendMessage(sender, ChatColor.RED + "Dieser Visualizer ist nicht auswählbar. Konfiguriere ihn mit /path-visualizer.");
				return;
			}
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			Collection<SimpleCurveVisualizer> list = VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getKey(), new ArrayList<>());
			list.add(simpleCurveVisualizer);
			VisualizerHandler.getInstance().getRoadmapVisualizers().put(roadMap.getKey(), list);
			SqlStorage.getInstance().addStyleToRoadMap(roadMap, simpleCurveVisualizer);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Style hinzugefügt: " + PathPlugin.COLOR_LIGHT + simpleCurveVisualizer.getNameFormat());
		}


		@Subcommand("remove")
		@CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER_STYLES)
		@Syntax("<Style>")
		public void onRemove(CommandSender sender, SimpleCurveVisualizer visualizer) {
			if (!visualizer.isPickable()) {
				PlayerUtils.sendMessage(sender, ChatColor.RED + "Dieser Visualizer ist nicht auswählbar. Konfiguriere ihn mit /path-visualizer.");
				return;
			}
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			SqlStorage.getInstance().removeStyleFromRoadMap(roadMap, visualizer);
			Collection<SimpleCurveVisualizer> list = VisualizerHandler.getInstance().getRoadmapVisualizers().get(roadMap.getKey());
			if (list != null) {
				list.remove(visualizer);
			}
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Style entfernt: " + PathPlugin.COLOR_LIGHT + visualizer.getNameFormat());
		}

		@Subcommand("list")
		public void onList(CommandSender sender) {
			Menu menu = new Menu("Alle Styles dieser Roadmap:");
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			for (SimpleCurveVisualizer visualizer : VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getKey(), new ArrayList<>())) {
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
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

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
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

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
}