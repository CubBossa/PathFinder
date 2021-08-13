package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.PlayerFindable;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.util.AStarUtils;
import de.bossascrew.pathfinder.util.CommandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
	@Syntax("<Straßenkarte>")
	@CommandPermission("bcrew.command.roadmap.info")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onInfo(CommandSender sender, @Optional RoadMap roadMap) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(sender);
		}

		Menu menu = new Menu("Straßenkarte: " + PathPlugin.CHAT_COLOR_DARK + roadMap.getName() + " (#" + roadMap.getDatabaseId() + ")");
		menu.addSub(getSubMenu(
				Component.text("Name: ").append(Component.text(roadMap.getName(), PathPlugin.COLOR_LIGHT)),
				Component.text("Klicke, um den Namen zu ändern."),
				"/roadmap rename " + roadMap.getName() + " <neuer Name>"));
		menu.addSub(getSubMenu(
				Component.text("Welt: ").append(Component.text(roadMap.getWorld().getName(), PathPlugin.COLOR_LIGHT)),
				Component.text("Klicke, um die Welt zu ändern."),
				"/roadmap setworld " + roadMap.getName() + " <Welt>"));
		menu.addSub(getSubMenu(
				Component.text("Pfadvisualisierer: ").append(Component.text(roadMap.getPathVisualizer().getName(), PathPlugin.COLOR_LIGHT)),
				Component.text("Klicke, um den Partikelstyle zu wechseln"),
				"/roadmap set path-visualizer " + roadMap.getName() + " <Style>"));
		menu.addSub(getSubMenu(
				Component.text("Editmode-Visualisierer: ").append(Component.text(roadMap.getEditModeVisualizer().getName(), PathPlugin.COLOR_LIGHT)),
				Component.text("Klicke, um den Editmode-Partikelstyle zu wechseln"),
				"/roadmap set editmode-visualizer " + roadMap.getName() + " <Style>"));
		menu.addSub(getSubMenu(
				Component.text("Findbarkeit: ").append(Component.text(roadMap.isFindableNodes() ? "An" : "Aus", PathPlugin.COLOR_LIGHT)),
				Component.text("Klicke, um die Findbarkeit zu setzen."),
				"/roadmap set findable " + roadMap.getName() + " <Wert>"));
		menu.addSub(getSubMenu(
				Component.text("Finde-Distanz: ", roadMap.isFindableNodes() ? NamedTextColor.WHITE : NamedTextColor.GRAY).append(Component.text(roadMap.getNodeFindDistance(), roadMap.isFindableNodes() ? PathPlugin.COLOR_LIGHT : NamedTextColor.DARK_GRAY)),
				Component.text("Klicke, um die Finde-Distanz zu setzen."),
				"/roadmap set find-distance " + roadMap.getName() + " <Distanz>"));
		menu.addSub(getSubMenu(
				Component.text("Default-Rundungsstärke: ").append(Component.text(roadMap.getDefaultBezierTangentLength(), PathPlugin.COLOR_LIGHT)),
				Component.text("Klicke, um die Rundungsstärke zu setzen."),
				"/roadmap set tangent-strength " + roadMap.getName() + " <Stärke>"));

		PlayerUtils.sendComponents(sender, menu.toComponents());
	}

	private ComponentMenu getSubMenu(Component text, Component hover, String command) {
		return new ComponentMenu(text
				.hoverEvent(HoverEvent.showText(hover))
				.clickEvent(ClickEvent.suggestCommand(command)));
	}

	@Subcommand("info ungrouped")
	@Syntax("<Straßenkarte>")
	@CommandPermission("bcrew.command.roadmap.info")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onUngrouped(CommandSender sender, @Optional RoadMap roadMap) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(sender);
		}
		Menu menu = new Menu("Ungruppierte Wegpunkte in " + roadMap.getName() + ":");
		String list = "";
		for (Findable findable : roadMap.getFindables().stream()
				.filter(findable -> findable.getGroup() == null)
				.collect(Collectors.toList())) {
			list += ChatColor.WHITE + findable.getName() + ChatColor.GRAY + ", ";
		}
		menu.addSub(new Menu(list));
		PlayerUtils.sendComponents(sender, menu.toComponents());
	}

	@Subcommand("create")
	@Syntax("<Name> [<Welt>] [findbar]")
	@CommandPermission("bcrew.command.roadmap.create")
	@CommandCompletion(BukkitMain.COMPLETE_NOTHING + " " + BukkitMain.COMPLETE_LOCAL_WORLDS + " findbar")
	public void onCreate(Player player, String name,
						 @Optional @Values("@worlds") World world,
						 @Optional @Single @Values("findbar") String findable) {

		if (RoadMapHandler.getInstance().getRoadMap(name) != null) {
			PlayerUtils.sendMessage(player, ChatColor.RED + "Es gibt bereits eine Roadmap mit diesem Namen.");
			return;
		}
		boolean findableNodes = findable != null;
		if (world == null) {
			world = player.getWorld();
		}

		RoadMap roadMap = RoadMapHandler.getInstance().createRoadMap(name, world, findableNodes);
		if (roadMap == null) {
			PlayerUtils.sendMessage(player, PathPlugin.PREFIX + ChatColor.RED + "Fehler beim Erstellen der Roadmap");
			return;
		}
		PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Roadmap " + PathPlugin.CHAT_COLOR_LIGHT + name + ChatColor.GRAY + " erfolgreich erstellt");

		//Karte auswählen, wenn erstellt
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
		pathPlayer.setSelectedRoadMap(roadMap.getDatabaseId());
	}

	@Subcommand("delete")
	@Syntax("<Straßenkarte>")
	@CommandPermission("bcrew.command.roadmap.delete")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onDelete(CommandSender sender, @Optional RoadMap roadMap) {
		if (roadMap == null) {
			roadMap = CommandUtils.getSelectedRoadMap(sender);
		}

		RoadMapHandler.getInstance().deleteRoadMap(roadMap);
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Straßenkarte " + PathPlugin.CHAT_COLOR_LIGHT + roadMap.getName() +
				ChatColor.GRAY + " erfolgreich gelöscht.");
	}

	@Subcommand("editmode")
	@CommandPermission("bcrew.command.roadmap.editmode")
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
				pp.setSelectedRoadMap(roadMap.getDatabaseId());
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
	@CommandPermission("bcrew.command.roadmap.list")
	public void onList(CommandSender sender) {

		Menu list = new Menu("Straßenkarten");
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
					.append(Component.text(roadMap.getName() + "(#" + roadMap.getDatabaseId() + ")",
							selected != null && roadMap.getName().equalsIgnoreCase(selected.getName()) ? PathPlugin.COLOR_LIGHT : PathPlugin.COLOR_DARK)
							.hoverEvent(HoverEvent.showText(Component.text("Klicken zum Auswählen.")))
							.clickEvent(ClickEvent.runCommand("/roadmap select " + roadMap.getName())))
					.append(Component.text(", Welt: ", NamedTextColor.GRAY))
					.append(Component.text(roadMap.getWorld().getName(), PathPlugin.COLOR_LIGHT)
							.hoverEvent(HoverEvent.showText(Component.text("Klicke zum Teleportieren")))
							.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/world " + roadMap.getWorld().getName()))) //TODO zu erster node teleportieren
					.append(Component.text(isEditing, NamedTextColor.RED))));
		}

		PlayerUtils.sendComponents(sender, list.toComponents());
	}

	@Subcommand("forcefind")
	@Syntax("<Straßenkarte> <Spieler> <Wegpunkt>|* [ungruppiert]")
	@CommandPermission("bcrew.command.roadmap.forcefind")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_VISIBLE_BUKKIT_PLAYERS + " " + PathPlugin.COMPLETE_FINDABLES)
	public void onForceFind(CommandSender sender, RoadMap roadMap, Player target, @Single String nodename,
							@Optional @Single @Values("ungruppiert") String ungrouped) {

		boolean findSingle = ungrouped != null;
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(target.getUniqueId());
		if (pathPlayer == null) {
			return;
		}

		boolean all = nodename.equals("*");
		for (Findable findable : roadMap.getFindables()) {
			if (findable.getName().equalsIgnoreCase(nodename) || all) {
				pathPlayer.find(findable, !findSingle, new Date());
				if (!all) {
					break;
				}
			}
		}
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Spieler " + PathPlugin.CHAT_COLOR_LIGHT + target.getName() +
				ChatColor.GRAY + " hat " + PathPlugin.CHAT_COLOR_LIGHT + nodename + ChatColor.GRAY + " gefunden.");
	}

	@Subcommand("forceforget")
	@Syntax("<Straßenkarte> <Spieler> <Wegpunkt>|* [ungruppiert]")
	@CommandPermission("bcrew.command.roadmap.forceforget")
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
		for (Findable findable : roadMap.getFindables()) {
			if (all || findable.getName().equalsIgnoreCase(nodename)) {
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
	@CommandPermission("bcrew.command.roadmap.select")
	@CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
	public void onSelect(CommandSender sender, RoadMap roadMap) {
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);

		pathPlayer.setSelectedRoadMap(roadMap.getDatabaseId());
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Straßenkarte ausgewählt: " + roadMap.getName());
	}

	@Subcommand("deselect")
	@CommandPermission("bcrew.command.roadmap.select")
	public void onDeselect(CommandSender sender) {
		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);

		pathPlayer.deselectRoadMap();
		PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Straßenkarte nicht mehr ausgewählt.");
	}

	@Subcommand("set")
	public class RoadMapSetCommand extends BaseCommand {

		@Subcommand("path-visualizer")
		@Syntax("<Style>")
		@CommandPermission("bcrew.command.roadmap.set.path-visualizer")
		@CommandCompletion(PathPlugin.COMPLETE_PATH_VISUALIZER)
		public void onStyle(CommandSender sender, PathVisualizer visualizer) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);

			roadMap.setPathVisualizer(visualizer);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikel-Style erfolgreich auf Straßenkarte angewendet.");
		}

		@Subcommand("editmode-visualizer")
		@Syntax("<Style>")
		@CommandPermission("bcrew.command.roadmap.set.editmode-visualizer")
		@CommandCompletion(PathPlugin.COMPLETE_EDITMODE_VISUALIZER)
		public void onStyleEditMode(CommandSender sender, EditModeVisualizer visualizer) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);

			roadMap.setEditModeVisualizer(visualizer);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Partikel-Style erfolgreich auf Straßenkarte angewendet.");
		}

		@Subcommand("name")
		@Syntax("<Neuer Name>")
		@CommandPermission("bcrew.command.roadmap.set.name")
		public void onRename(CommandSender sender, @Single String nameNew) {
			if (RoadMapHandler.getInstance().getRoadMap(nameNew) != null) {
				PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + ChatColor.RED + "Dieser Name ist bereits vergeben.");
				return;
			}
			CommandUtils.getSelectedRoadMap(sender).setName(nameNew);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Straßenkarte erfolgreich umbenannt: " + nameNew);
		}

		@Subcommand("tangent-strength")
		@Syntax("<Wert>")
		@CommandPermission("bcrew.command.roadmap.set.tangent-strength")
		public void onChangeTangentStrength(CommandSender sender, double strength) {
			CommandUtils.getSelectedRoadMap(sender).setDefaultBezierTangentLength(strength);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Standard-Tangentenstärke erfolgreich gesetzt: " + strength);
		}

		@Subcommand("world")
		@Syntax("<Welt> [erzwingen]")
		@CommandPermission("bcrew.command.roadmap.set.world")
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
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Die Welt für " + roadMap.getName() + " wurde erfolgreich gewechselt.\n" +
					ChatColor.RED + "ACHTUNG! Wegpunke sind möglicherweise nicht da, wo man sie erwartet.");
		}

		@Subcommand("find-distance")
		@Syntax("<finde-entfernung>")
		@CommandPermission("bcrew.command.roadmap.set.find-distance")
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
		@CommandPermission("bcrew.command.roadmap.set.findable")
		@CommandCompletion(BukkitMain.COMPLETE_BOOLEAN)
		public void onSetFindable(CommandSender sender, boolean findbar) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			roadMap.setFindableNodes(findbar);
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Node-Findbarkeit umgestellt auf: " + PathPlugin.CHAT_COLOR_LIGHT + (findbar ? "an" : "aus"));
		}
	}

	@Subcommand("style")
	@CommandPermission("bcrew.command.roadmap.style")
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
			Collection<PathVisualizer> list = VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getDatabaseId(), new ArrayList<>());
			list.add(pathVisualizer);
			VisualizerHandler.getInstance().getRoadmapVisualizers().put(roadMap.getDatabaseId(), list);
			DatabaseModel.getInstance().addStyleToRoadMap(roadMap, pathVisualizer);
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
			DatabaseModel.getInstance().removeStyleFromRoadMap(roadMap, visualizer);
			Collection<PathVisualizer> list = VisualizerHandler.getInstance().getRoadmapVisualizers().get(roadMap.getDatabaseId());
			if (list != null) {
				list.remove(visualizer);
			}
			PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Style entfernt: " + PathPlugin.COLOR_LIGHT + visualizer.getName());
		}

		@Subcommand("list")
		public void onList(CommandSender sender) {
			Menu menu = new Menu("Alle Styles dieser Roadmap:");
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
			for (PathVisualizer visualizer : VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getDatabaseId(), new ArrayList<>())) {
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
	@CommandPermission("bcrew.command.roadmap.test")
	public class RoadMapTestCommand extends BaseCommand {

		@Subcommand("navigate")
		@Syntax("<Findable>")
		@CommandCompletion(PathPlugin.COMPLETE_FINDABLES)
		public void onTestNavigate(Player player, Findable findable) {
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
		public void onTestFind(Player player, Findable findable) {
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