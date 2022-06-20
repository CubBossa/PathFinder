package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.PathPlayerHandler;
import de.bossascrew.pathfinder.node.*;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.visualizer.ParticlePath;
import org.bukkit.entity.Player;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

@CommandAlias("find|gps|navigate")
public class FindCommand extends BaseCommand {

	@Subcommand("location")
	public void onFindSpot(Player player, NavigateSelection navigables) {

		PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);

		RoadMap roadMap = navigables.getRoadMap();
		if (roadMap == null) {
			return;
		}

		// Prepare graph:
		// Every target node will be connected with a new introduced destination node.
		// All new edges have the same weight. The shortest path can only cross a target node.
		// Finally, take a sublist of the shortest path to exclude the destination.

		PlayerNode playerNode = new PlayerNode(player, roadMap);
		Graph<Node, Edge> graph = roadMap.toGraph(playerNode);

		EmptyNode destination = new EmptyNode(roadMap);
		graph.addVertex(destination);
		navigables.stream().flatMap(x -> x.getGroup().stream()).distinct().forEach(n -> {
			Edge e = new Edge(n, destination, 0);
			graph.addEdge(n, destination, e);
			graph.setEdgeWeight(e, 1);
		});

		GraphPath<Node, Edge> path = new DijkstraShortestPath<>(graph).getPath(playerNode, destination);
		ParticlePath particlePath = new ParticlePath(roadMap, player.getUniqueId(), roadMap.getVisualizer());
		particlePath.addAll(path.getVertexList().subList(0, path.getVertexList().size() - 1));
		pathPlayer.setPath(particlePath);
	}

/*
    @CatchUnknown
    @HelpCommand
    public void onDefault(Player player) {
        TextMenu menu = new TextMenu("Finde Orte einer Stadtkarte mit folgenden Befehlen:");
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_LOCATIONS)) {
            menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find ort <Ort>")));
        }
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_ITEMS)) {
            if (PathPlugin.getInstance().isTraders() || PathPlugin.getInstance().isQuests() || (PathPlugin.getInstance().isBentobox()) && PathPlugin.getInstance().isChestShop()) {
                menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find item <Item>")));
            }
        }
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_QUESTS)) {
            if (PathPlugin.getInstance().isQuests()) {
                menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find quest <Quest>")));
            }
        }
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_TRADERS)) {
            if (PathPlugin.getInstance().isTraders()) {
                menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find shop <Shop>")));
            }
        }
        TextMenu menu1 = new TextMenu("Info über gefundene Orte mit: ");
        if (player.hasPermission(PathPlugin.PERM_COMMAND_FIND_INFO) &&
                RoadMapHandler.getInstance().getRoadMaps().stream().anyMatch(RoadMap::isFindableNodes)) {
            menu1.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find info", ClickEvent.Action.RUN_COMMAND)));
        }
        if (menu.hasSubs()) {
            PlayerUtils.sendComponents(player, menu.toComponents());
        }
        if (menu1.hasSubs()) {
            PlayerUtils.sendComponents(player, menu1.toComponents());
        }
        if (!menu.hasSubs() && !menu1.hasSubs()) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Es wurden keine Befehle gefunden.");
        }
    }

    @Subcommand("info")
    @Syntax("[<Straßenkarte>]")
    @CommandCompletion(PathPlugin.COMPLETE_ACTIVE_ROADMAPS)
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_INFO)
    public void onInfo(Player player, @Optional RoadMap roadMap) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
        if (pathPlayer == null) {
            return;
        }
        TextMenu menu = new TextMenu("Straßenkarten erkundet:");
        for (RoadMap rm : roadMap == null ? RoadMapHandler.getInstance().getRoadMaps()
                .stream().filter(RoadMap::isFindableNodes).collect(Collectors.toList()) : Lists.newArrayList(roadMap)) {
            double percent = 100 * ((double) pathPlayer.getFoundAmount(rm)) / rm.getMaxFoundSize();
            menu.addSub(new TextMenu(ChatColor.GRAY + rm.getNameFormat() + ": " + ChatColor.WHITE + String.format("%,.2f", percent) + "%"));
        }
        if (menu.hasSubs()) {
            PlayerUtils.sendComponents(player, menu.toComponents());
        } else {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Keine Straßenkarten gefunden.");
        }
    }

    @Subcommand("style")
    @Syntax("<Straßenkarte>")
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_STYLE)
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS)
    public void onStyle(Player player, RoadMap roadMap) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
        if (pathPlayer == null) {
            return;
        }
        openStyleMenu(player, pathPlayer, roadMap);
    }

    Map<Player, Long> styleMenuCooldown = new ConcurrentHashMap<>();

    private void openStyleMenu(Player player, PathPlayer pathPlayer, RoadMap roadMap) {
        RectInventoryMenu menu = new RectInventoryMenu(Component.text("Wähle deinen Partikelstyle"), 3);
        SimpleCurveVisualizer actual = pathPlayer.getVisualizer(roadMap);

        Collection<SimpleCurveVisualizer> visualizers = VisualizerHandler.getInstance().getRoadmapVisualizers().getOrDefault(roadMap.getKey(), new ArrayList<>());
        for (SimpleCurveVisualizer visualizer : visualizers) {
            String perm = visualizer.getPermission();

            boolean hasPerm = perm == null || player.hasPermission(perm);
            boolean def = false, spender = false, spender2 = false;
            if (!hasPerm) {
                GroupManager groupManager = BukkitMain.getInstance().getLuckPerms().getGroupManager();
                Group defaultGroup = groupManager.getGroup("default");
                def = defaultGroup.getCachedData().getPermissionData().checkPermission(perm).asBoolean();
                if (!def) {
                    Group spenderGroup = groupManager.getGroup("spender");
                    spender = spenderGroup.getCachedData().getPermissionData().checkPermission(perm).asBoolean();
                    if (!spender) {
                        Group spender2Group = groupManager.getGroup("spender2");
                        spender2 = spender2Group.getCachedData().getPermissionData().checkPermission(perm).asBoolean();
                    }
                }
            }

            ItemStack stack = new ItemStack(visualizer.getIconType());
            ItemMeta m = stack.getItemMeta();
            m.displayName(visualizer.getDisplayName());
            if (spender) {
                m.lore(Lists.newArrayList(Component.text("Ab Matrose erhältlich.", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)));
            } else if (spender2) {
                m.lore(Lists.newArrayList(Component.text("Ab Maat erhältlich.", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)));
            }
            stack.setItemMeta(m);

            if (actual.equals(visualizer)) {
                ItemStackUtils.setGlowing(stack);
            }
            menu.addMenuEntry(stack, hasPerm ? 0 : spender ? 1 : spender2 ? 2 : 3, context -> {
                if (!hasPerm) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                if(styleMenuCooldown.containsKey(player)) {
                    if(System.currentTimeMillis() - styleMenuCooldown.get(player) < 1000) {
                        PlayerUtils.sendMessage(player, ChatColor.RED + "Bitte warte eine Sekunde, bevor du deinen Pfad neu setzt.");
                        return;
                    }
                }
                styleMenuCooldown.put(player, System.currentTimeMillis());
                pathPlayer.setVisualizer(roadMap, visualizer);
                player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1, 1);
                PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Partikelstyle ausgewählt: " + visualizer.getNameFormat());
                openStyleMenu(player, pathPlayer, roadMap);
            });
        }
        menu.open(player);
    }

    @Subcommand("ort")
    @Syntax("<Ort>")
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_LOCATIONS)
    @CommandCompletion(PathPlugin.COMPLETE_FINDABLE_LOCATIONS)
    public void onFindeOrt(Player player, String searched) {
        RoadMap roadMap = CommandUtils.getAnyRoadMap(player.getWorld());
        if (roadMap == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Keine Straßenkarte gefunden.");
            return;
        }
        if (!roadMap.getWorld().equals(player.getWorld())) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Diese Straßenkarte liegt nicht in deiner aktuellen Welt.");
            return;
        }
        PathPlayer pp = PathPlayerHandler.getInstance().getPlayer(player);
        if(pp == null) {
            return;
        }

        Waypoint f = roadMap.getNodes().stream()
                .filter(findable -> findable.getGroup() == null)
                .filter(findable -> findable.getNameFormat().equalsIgnoreCase(searched))
                .findFirst().orElse(null);
        if(f == null) {
            NodeGroup group = roadMap.getGroups().values().stream()
                    .filter(NodeGroup::isFindable)
                    .filter(g -> g.getNameFormat().equalsIgnoreCase(searched))
                    .filter(g -> pp.hasFound(g.getGroupId(), true))
                    .findAny().orElse(null);
            if(group == null) {
                PlayerUtils.sendMessage(player, ChatColor.RED + "Es gibt kein Ziel mit diesem Namen.");
                return;
            }
            f = group.getFindables().stream().findAny().orElse(null);
        }
        if (f == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Ein Fehler ist aufgetreten.");
            return;
        }
        AStarUtils.startPath(player, f, true);
    }*/
}
