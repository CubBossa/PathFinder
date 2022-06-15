package de.bossascrew.pathfinder.util;

import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.core.util.Pair;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.astar.AStar;
import de.bossascrew.pathfinder.astar.AStarEdge;
import de.bossascrew.pathfinder.astar.AStarNode;
import de.bossascrew.pathfinder.data.ParticlePath;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.node.PlayerFindable;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AStarUtils {

    public static void startPath(Player player, Waypoint target) {
        startPath(player, target, false);
    }

    public static void startPath(Player player, Waypoint target, boolean findGroup) {
        PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        if (!AStarUtils.startPath(pPlayer, new PlayerFindable(player, target.getRoadMap()), target, false, findGroup)) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Es konnte kein kürzester Pfad ermittelt werden.");
            return;
        }

        player.sendMessage(PathPlugin.PREFIX_COMP
                .append(Component.text("Navigation gestartet. (", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                .append(ComponentUtils.getCommandComponent("/cancelpath", ClickEvent.Action.RUN_COMMAND))
                .append(Component.text(")", NamedTextColor.GRAY)));
    }

    /**
     * Threadsafe, kann asynchron ausgeführt werden.
     * Startet die Pfaddarstellung für einen Spieler
     *
     * @param player Der Spieler, für den die Pfaddarstellung gestartet werden soll.
     * @param start  Der Spieler als Findable für die Berechnung mit Startwegpunkt an der Spielerposition.
     * @param target Das Findable, das der Spieler sucht.
     * @return false, wenn das Ziel nicht erreicht werden konnte.
     */
    public static boolean startPath(PathPlayer player, PlayerFindable start, Waypoint target, boolean ignoreUnfound) {
        return startPath(player, start, target, ignoreUnfound, false);
    }

    public static boolean startPath(PathPlayer player, PlayerFindable start, Waypoint target, boolean ignoreUnfound, boolean findGroup) {
        Pair<AStarNode, AStarNode> pair = createAStarRelations(target.getRoadMap(), player, start, start.getLocation(), target, ignoreUnfound, findGroup);
        if (pair == null || pair.first == null || pair.second == null) {
            return false;
        }

        AStar aStar = new AStar();
        aStar.aStarSearch(pair.first, pair.second);

        List<AStarNode> pathNodes = aStar.printPath(pair.second, findGroup);
        List<Waypoint> pathVar = pathNodes.stream()
                .map(aStarNode -> aStarNode.findable == null ? start : aStarNode.findable)
                .collect(Collectors.toList());

        Waypoint foundLast = pathVar.get(0);
        if (foundLast == null) {
            return false;
        }
        if (foundLast.getNodeId() != start.getNodeId()) {
            //Es konnte kein Pfad ermittelt werden, wenn das letzte Node des Abbruchpfades nicht der Startpunkt ist
            return false;
        }

        ParticlePath path = new ParticlePath(start.getRoadMap(), player.getUuid(), player.getVisualizer(start.getRoadMap()));
        path.addAll(pathVar);
        player.setPath(path);
        return true;
    }

    /**
     * Threadsafe, kann asynchron ausgeführt werden.
     *
     * @param roadMap Die Straßenkarte, aus der der AStar erzeugt wird.
     * @param start   Die Startposition für den Algorithmus. Aus ihr wird die Startnode erzeugt.
     * @param player  Der PathPlayer, für den die Straßenkarte angepasst wird. Für ihn laufen die Permissionabfragen und "Gefunden"-Abfragen
     * @return Die aus der Spielerinformation erzeugte StartNode und Zielnode des AStar Algorithmus. Sie wird benötigt, um den Algorithmus zu starten. Der Return-Wert nimmt null an, wenn keine passenden Nodes gefunden wurden.
     */
    public @Nullable
    static Pair<AStarNode, AStarNode> createAStarRelations(RoadMap roadMap, PathPlayer player, PlayerFindable playerFindable, Location start, Waypoint target, boolean ignoreUnfound, boolean findGrouped) {

        //TODO nicht nur nearest dist, sondern nearest edge center und endpunkte berücksichtigen

        Collection<Waypoint> findables = ignoreUnfound ? roadMap.getNodes() : roadMap.getNodes(player);
        Map<Integer, AStarNode> aStarNodes = new HashMap<>();

        AStarNode playerNode = new AStarNode(playerFindable, 0);
        AStarNode targetNode = null;

        Double nearestDist = null;
        AStarNode nearest = null;

        for (Waypoint findable : findables) {
            double dist = start.distance(findable.getLocation());
            AStarNode aStarNode = new AStarNode(findable, dist);
            aStarNode.groupId = findable.getGroup() != null ? findable.getGroup().getDatabaseId() : null;
            aStarNodes.put(findable.getNodeId(), aStarNode);
            if (nearestDist == null || dist < nearestDist) {
                nearest = aStarNode;
                nearestDist = dist;
            }
            if (findable.getNodeId() == target.getNodeId()) {
                targetNode = aStarNode;
            }
        }

        if (nearest == null) {
            return null;
        }
        playerNode.adjacencies = new AStarEdge[]{new AStarEdge(nearest, nearestDist)};
        nearest.adjacencies = new AStarEdge[]{new AStarEdge(playerNode, nearestDist)};

        for (Waypoint findable : findables) {
            AStarEdge[] adjacencies = new AStarEdge[(int) findable.getEdges().stream()
                    .filter(integer -> findables.stream().anyMatch(f -> f.getNodeId() == integer))
                    .count()];
            AStarNode aStarNode = aStarNodes.get(findable.getNodeId());

            int i = 0;
            for (Integer edgeId : findable.getEdges()) {
                if (findables.stream().noneMatch(f -> f.getNodeId() == edgeId)) {
                    continue;
                }
                AStarEdge edgeTarget = new AStarEdge(aStarNodes.get(edgeId), start.distance(findable.getLocation()));
                adjacencies[i] = edgeTarget;
                i++;
            }
            aStarNode.adjacencies = adjacencies;
        }
        if (targetNode == null) {
            return null;
        }
        return new Pair<>(playerNode, targetNode);
    }
}
