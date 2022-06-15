package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.SqlStorage;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.node.PlayerFindable;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.util.AStarUtils;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.bossascrew.pathfinder.util.SelectionUtils;
import de.cubbossa.menuframework.chat.ComponentMenu;
import de.cubbossa.menuframework.chat.TextMenu;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

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
				.tag("id", Tag.preProcessParsed(roadMap.getRoadmapId() + ""))
				.tag("name", Tag.inserting(roadMap.getDisplayName()))
				.tag("world", Tag.preProcessParsed(roadMap.getWorld().getName()))
				.tag("findable", Tag.inserting(roadMap.isFindableNodes() ?
						Messages.GEN_TRUE.asComponent(sender) : Messages.GEN_FALSE.asComponent(sender)))
				.tag("find-distance", Tag.preProcessParsed(roadMap.getNodeFindDistance() + ""))
				.tag("curve-length", Tag.preProcessParsed(roadMap.getDefaultBezierTangentLength() + ""))
				.tag("path-visualizer", Tag.inserting(roadMap.getPathVisualizer().getDisplayName()))
				.tag("path-visualizer", Tag.inserting(roadMap.getEditModeVisualizer().getName()))
				.build());

		TranslationHandler.getInstance().sendMessage(message, sender);
	}

	@Subcommand("info ungrouped")
	@Syntax("<roadmap>")
	@CommandPermission("pathfinder.command.roadmap.info")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onUngrouped(CommandSender sender, @Optional RoadMap roadMap) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(sender);
		}
		TextMenu menu = new TextMenu("Ungruppierte Wegpunkte in " + roadMap.getNameFormat() + ":");
		String list = "";
		for (Waypoint findable : roadMap.getNodes().stream()
				.filter(findable -> findable.getGroup() == null)
				.collect(Collectors.toList())) {
			list += ChatColor.WHITE + findable.getNameFormat() + ChatColor.GRAY + ", ";
		}
		menu.addSub(new TextMenu(list));
		PlayerUtils.sendComponents(sender, menu.toComponents());
	}

	@Subcommand("create")
	@Syntax("<name> [<world>] [findable]")
	@CommandPermission("pathfinder.command.roadmap.create")
	@CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " findable")
	public void onCreate(Player player, String name,
	                     @Optional @Values("@worlds") World world,
	                     @Optional @Single @Values("findbar") String findable) {

		boolean findableNodes = findable != null;
		if (world == null) {
			world = player.getWorld();
		}

		try {
			RoadMap roadMap = RoadMapHandler.getInstance().createRoadMap(name, world, findableNodes);
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_SUCCESS.format(TagResolver.resolver("name", Tag.inserting(roadMap.getDisplayName()))), player);

			PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			pathPlayer.setSelectedRoadMap(roadMap.getRoadmapId());

		} catch (Exception e) {
			TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_CREATE_FAIL, player);
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
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Straßenkarte " + PathPlugin.CHAT_COLOR_LIGHT + roadMap.getNameFormat() +
				ChatColor.GRAY + " erfolgreich gelöscht.");
	}

	@Subcommand("editmode")
	@CommandPermission("pathfinder.command.roadmap.editmode")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	@Syntax("[<Straßenkarte>]")
	public void onEdit(Player player, @Optional RoadMap roadMap) {
		PathPlayer pp = PathPlayerHandler.getInstance().getPlayer(player);
		if (pp == null) {
			return;
		}
		if (roadMap == null) {
			if (pp.getSelectedRoadMapId() != null) {
				roadMap = RoadMapHandler.getInstance().getRoadMap(pp.getSelectedRoadMapId());
			}
		}
		if (roadMap == null) {
			if (RoadMapHandler.getInstance().getRoadMaps().size() == 1) {
				roadMap = RoadMapHandler.getInstance().getRoadMaps().stream().findAny().orElse(null);
				if (roadMap == null) {
					if (RoadMapHandler.getInstance().getRoadMaps(player.getWorld()).size() == 1) {
						roadMap = RoadMapHandler.getInstance().getRoadMaps(player.getWorld()).stream().findAny().orElse(null);
					}
				}
			}
			if (roadMap != null) {
				pp.setSelectedRoadMap(roadMap.getRoadmapId());
				PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Straßenkarte ausgewählt.");
			}
		}
		if (roadMap == null) {
			PlayerUtils.sendMessage(player, ChatColor.RED + "Du musst eine Straßenkarte ausgewählt haben.");
			return;
		}

		roadMap.toggleEditMode(player.getUniqueId());
		PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Bearbeitungsmodus: " +
				(roadMap.isEditing(player) ? "AKTIVIERT" : "DEAKTIVIERT"));
	}

	@Subcommand("list")
	@CommandPermission("pathfinder.command.roadmap.list")
	public void onList(CommandSender sender) {

		TextMenu list = new TextMenu("Straßenkarten");
		RoadMap selected = null;
		if (sender instanceof Player) {
			selected = CommandUtils.getSelectedRoadMap(sender, false);
		}

		for (RoadMap roadMap : RoadMapHandler.getInstance().getRoadMaps()) {

			String isEditing = "";
			if (sender instanceof Player) {
				if (roadMap.isEditing((Player) sender)) {
					isEditing = PathPlugin.CHAT_COLOR_LIGHT + " BEARBEITUNGSMODUS";
				}
			}

			list.addSub(new ComponentMenu(Component.empty()
					.append(Component.text(roadMap.getNameFormat() + "(#" + roadMap.getRoadmapId() + ")",
									selected != null && roadMap.getNameFormat().equalsIgnoreCase(selected.getNameFormat()) ? PathPlugin.COLOR_LIGHT : PathPlugin.COLOR_DARK)
							.hoverEvent(HoverEvent.showText(Component.text("Klicken zum Auswählen.")))
							.clickEvent(ClickEvent.runCommand("/roadmap select " + roadMap.getNameFormat())))
					.append(Component.text(", Welt: ", NamedTextColor.GRAY))
					.append(Component.text(roadMap.getWorld().getName(), PathPlugin.COLOR_LIGHT)
							.hoverEvent(HoverEvent.showText(Component.text("Klicke zum Teleportieren")))
							.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/world " + roadMap.getWorld().getName()))) //TODO zu erster node teleportieren
					.append(Component.text(isEditing, NamedTextColor.RED))));
		}

		PlayerUtils.sendComponents(sender, list.toComponents());
	}

	@Subcommand("forcefind")
	@Syntax("<roadmap> <player> <nodes> [ungrouped]")
	@CommandPermission("pathfinder.command.roadmap.forcefind")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_VISIBLE_BUKKIT_PLAYERS + " " + PathPlugin.COMPLETE_FINDABLES)
	public void onForceFind(CommandSender sender, RoadMap roadMap, Player target, @Single NodeSelection selection,
							@Optional @Single @Values("ungrouped") String ungrouped) {

		boolean findSingle = ungrouped != null;
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(target.getUniqueId());
		if (pathPlayer == null) {
			return;
		}

		for (Node node : selection) {
			pathPlayer.find(node, !findSingle, new Date());
		}
		TranslationHandler.getInstance().sendMessage(Messages.CMD_RM_FORCE_FIND.format(TagResolver.builder()
				.tag("name", Tag.inserting(PathPlugin.getInstance().getAudiences().player(target).getOrDefault(Identity.DISPLAY_NAME, Component.text(target.getName()))))
				.tag("selection", Tag.inserting(SelectionUtils.formatSelection(selection)))
				.build()), sender);
	}

	@Subcommand("forceforget")
	@Syntax("<Straßenkarte> <Spieler> <Wegpunkte> [ungruppiert]")
	@CommandPermission("pathfinder.command.roadmap.forceforget")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_VISIBLE_BUKKIT_PLAYERS +
			" " + PathPlugin.COMPLETE_FINDABLES_FOUND + "|* ungruppiert")
	public void onForceForget(CommandSender sender, RoadMap roadMap, Player target, @Single String nodename,
	                          @Optional @Single String ungrouped) {

		boolean findSingle = ungrouped != null && ungrouped.equalsIgnoreCase("ungruppiert");
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(target.getUniqueId());
		if (pathPlayer == null) {
			return;
		}

		boolean all = nodename.equals("*");
		for (Waypoint findable : roadMap.getNodes()) {
			if (all || findable.getNameFormat().equalsIgnoreCase(nodename)) {
				pathPlayer.unfind(findable, !findSingle);
				if (!all) {
					break;
				}
			}
		}
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Spieler " + PathPlugin.CHAT_COLOR_LIGHT + target.getName() +
				ChatColor.GRAY + " hat " + PathPlugin.CHAT_COLOR_LIGHT + nodename + ChatColor.GRAY + " vergessen.");
	}

	@Subcommand("select")
	@Syntax("<Straßenkarte>")
	@CommandPermission("pathfinder.command.roadmap.select")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onSelect(CommandSender sender, RoadMap roadMap) {
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);

		pathPlayer.setSelectedRoadMap(roadMap.getRoadmapId());
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

			roadMap.setPathVisualizer(visualizer);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikel-Style erfolgreich auf Straßenkarte angewendet.");
		}

		@Subcommand("editmode-visualizer")
		@Syntax("<Style>")
		@CommandPermission("pathfinder.command.roadmap.set.editmode-visualizer")
		@CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
		public void onStyleEditMode(CommandSender sender, EditModeVisualizer visualizer) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);

			roadMap.setEditModeVisualizer(visualizer);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikel-Style erfolgreich auf Straßenkarte angewendet.");
		}

		@Subcommand("name")
		@Syntax("<new name>")
		@CommandPermission("pathfinder.command.roadmap.set.name")
		public void onRename(CommandSender sender, @Single String nameNew) {

			CommandUtils.getSelectedRoadMap(sender).setNameFormat(nameNew);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Straßenkarte erfolgreich umbenannt: " + nameNew);
		}

		@Subcommand("tangent-strength")
		@Syntax("<Wert>")
		@CommandPermission("pathfinder.command.roadmap.set.tangent-strength")
		public void onChangeTangentStrength(CommandSender sender, double strength) {
			CommandUtils.getSelectedRoadMap(sender).setDefaultBezierTangentLength(strength);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Standard-Tangentenstärke erfolgreich gesetzt: " + strength);
		}

		@Subcommand("world")
		@Syntax("<Welt> [erzwingen]")
		@CommandPermission("pathfinder.command.roadmap.set.world")
		@CommandCompletion(BukkitMain.COMPLETE_LOCAL_WORLDS + " erzwingen")
		public void onChangeWorld(CommandSender sender, World world, @Optional @Single @Values("erzwingen") String forceString) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			boolean force = forceString != null;

			if (!force && roadMap.isEdited()) {
				PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Diese Straßenkarte wird gerade bearbeitet. " +
						"Nutze den [erzwingen] Parameter, um die Welt trotzdem zu ändern.");
				return;
			}

			if (roadMap.isEdited()) {
				roadMap.cancelEditModes();
			}

			roadMap.setWorld(world);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Die Welt für " + roadMap.getNameFormat() + " wurde erfolgreich gewechselt.\n" +
					ChatColor.RED + "ACHTUNG! Wegpunke sind möglicherweise nicht da, wo man sie erwartet.");
		}

		@Subcommand("find-distance")
		@Syntax("<finde-entfernung>")
		@CommandPermission("pathfinder.command.roadmap.set.find-distance")
		public void onFindDistance(CommandSender sender, double findDistance) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			if (findDistance < 0.05) {
				PlayerUtils.sendMessage(sender, ChatColor.RED + "Die angebenene Distanz ist zu klein.");
				return;
			}
			roadMap.setNodeFindDistance(findDistance);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Finde-Entfernung erfolgreich gesetzt: " + PathPlugin.CHAT_COLOR_LIGHT + findDistance);
		}

		@Subcommand("findable")
		@Syntax("<findbare Nodes>")
		@CommandPermission("pathfinder.command.roadmap.set.findable")
		@CommandCompletion(BukkitMain.COMPLETE_BOOLEAN)
		public void onSetFindable(CommandSender sender, boolean findbar) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			roadMap.setFindableNodes(findbar);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Node-Findbarkeit umgestellt auf: " + PathPlugin.CHAT_COLOR_LIGHT + (findbar ? "an" : "aus"));
		}
	}

	@Subcommand("style")
	@CommandPermission("pathfinder.command.roadmap.style")
	public class RoadmapStyleCommand extends BaseCommand {

		@Subcommand("add")
		@CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER_STYLES)
		@Syntax("<Style>")
		public void onAdd(CommandSender sender, PathVisualizer pathVisualizer) {
			if (!pathVisualizer.isPickable()) {
				PlayerUtils.sendMessage(sender, ChatColor.RED + "Dieser Visualizer ist nicht auswählbar. Konfiguriere ihn mit /path-visualizer.");
				return;
			}
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			Collection<PathVisualizer> list = VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getRoadmapId(), new ArrayList<>());
			list.add(pathVisualizer);
			VisualizerHandler.getInstance().getRoadmapVisualizers().put(roadMap.getRoadmapId(), list);
			SqlStorage.getInstance().addStyleToRoadMap(roadMap, pathVisualizer);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Style hinzugefügt: " + PathPlugin.COLOR_LIGHT + pathVisualizer.getName());
		}


		@Subcommand("remove")
		@CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER_STYLES)
		@Syntax("<Style>")
		public void onRemove(CommandSender sender, PathVisualizer visualizer) {
			if (!visualizer.isPickable()) {
				PlayerUtils.sendMessage(sender, ChatColor.RED + "Dieser Visualizer ist nicht auswählbar. Konfiguriere ihn mit /path-visualizer.");
				return;
			}
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			SqlStorage.getInstance().removeStyleFromRoadMap(roadMap, visualizer);
			Collection<PathVisualizer> list = VisualizerHandler.getInstance().getRoadmapVisualizers().get(roadMap.getRoadmapId());
			if (list != null) {
				list.remove(visualizer);
			}
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Style entfernt: " + PathPlugin.COLOR_LIGHT + visualizer.getName());
		}

		@Subcommand("list")
		public void onList(CommandSender sender) {
			Menu menu = new Menu("Alle Styles dieser Roadmap:");
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			for (PathVisualizer visualizer : VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getRoadmapId(), new ArrayList<>())) {
				menu.addSub(new ComponentMenu(Component.empty()
						.append(visualizer.getDisplayName())
						.append(Component.text(" [X]", NamedTextColor.RED)
								.clickEvent(ClickEvent.runCommand("/roadmap style remove " + visualizer.getName()))
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
		@CommandCompletion(PathPlugin.COMPLETE_FINDABLES)
		public void onTestNavigate(Player player, Waypoint findable) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

			PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
			if (!AStarUtils.startPath(pPlayer, new PlayerFindable(player, roadMap), findable, true)) {
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
			if (!AStarUtils.startPath(pPlayer, new PlayerFindable(player, roadMap), findable, false)) {
				PlayerUtils.sendMessage(player, ChatColor.RED + "Es konnte kein kürzester Pfad ermittelt werden.");
				return;
			}

			player.sendMessage(PathPlugin.PREFIX_COMP
					.append(Component.text("Testpfad gestartet. (", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
					.append(ComponentUtils.getCommandComponent("/cancelpath", ClickEvent.Action.RUN_COMMAND))
					.append(Component.text(")", NamedTextColor.GRAY)));
		}

		@Subcommand("visible")
		@Syntax("true|false")
		@CommandCompletion("true|false")
		public void onTestVisible(Player player, boolean visible) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
			roadMap.toggleArmorStandsVisible(player, visible);
			player.sendMessage(PathPlugin.PREFIX_COMP.append(Component.text("Visibility gesetzt: " + visible)));
		}
	}
}