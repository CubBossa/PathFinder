package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.astar.AStar;
import de.bossascrew.pathfinder.astar.AStarEdge;
import de.bossascrew.pathfinder.astar.AStarNode;
import de.bossascrew.pathfinder.data.ParticlePath;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.PlayerFindable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class AStarUtils {

    /**
     * Threadsafe, kann asynchron ausgeführt werden.
     * Startet die Pfaddarstellung für einen Spieler
     *
     * @param player Der Spieler, für den die Pfaddarstellung gestartet werden soll.
     * @param start  Der Spieler als Findable für die Berechnung mit Startwegpunkt an der Spielerposition
     * @param target Das Findable, das der Spieler sucht
     */
    public static void startPath(PathPlayer player, PlayerFindable start, Findable target) {
        Pair<AStarNode, AStarNode> pair = createAStarRelations(target.getRoadMap(), player, start.getLocation(), target);
        if (pair == null) {
            return;
        }
        AStar aStar = new AStar();
        aStar.aStarSearch(pair.first, pair.second);

        List<AStarNode> pathNodes = aStar.printPath(pair.second);
        List<Findable> pathVar = pathNodes.stream()
                .map(aStarNode -> aStarNode.findable == null ? start : aStarNode.findable)
                .collect(Collectors.toList());

        ParticlePath path = new ParticlePath(start.getRoadMap(), player.getUuid());
        path.addAll(pathVar);
        player.setPath(path);
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
    static Pair<AStarNode, AStarNode> createAStarRelations(RoadMap roadMap, PathPlayer player, Location start, Findable target) {

        Collection<Findable> findables = roadMap.getFindables(player);
        Map<Integer, AStarNode> aStarNodes = new HashMap<>();

        AStarNode playerNode = new AStarNode(null, 0);
        AStarNode targetNode = null;

        Double nearestDist = null;
        AStarNode nearest = null;

        for (Findable findable : findables) {
            double dist = start.distance(findable.getLocation());
            AStarNode aStarNode = new AStarNode(findable, dist);
            aStarNodes.put(findable.getDatabaseId(), aStarNode);
            if (nearestDist == null || dist < nearestDist) {
                nearest = aStarNode;
                nearestDist = dist;
            }
            if (findable.getDatabaseId() == target.getDatabaseId()) {
                targetNode = aStarNode;
            }
        }

        if (nearest == null) {
            return null;
        }
        playerNode.adjacencies = new AStarEdge[]{new AStarEdge(nearest, nearestDist)};
        nearest.adjacencies = new AStarEdge[]{new AStarEdge(playerNode, nearestDist)};

        for (Findable findable : findables) {
            AStarEdge[] adjacencies = new AStarEdge[findable.getEdges().size()];
            AStarNode aStarNode = aStarNodes.get(findable.getDatabaseId());

            int i = 0;
            for (Integer edgeId : findable.getEdges()) {
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
