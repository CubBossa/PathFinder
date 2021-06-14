package de.bossascrew.pathfinder.util;

import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AStarUtils {

    /**
     * @param roadMap Die Straßenkarte, aus der der AStar erzeugt wird.
     * @param start   Die Startposition für den Algorithmus. Aus ihr wird die Startnode erzeugt.
     * @param player  Der PathPlayer, für den die Straßenkarte angepasst wird. Für ihn laufen die Permissionabfragen und "Gefunden"-Abfragen
     * @return Die aus der Spielerinformation erzeugte StartNode des AStar Algorithmus. Sie wird benötigt, um den Algorithmus zu starten.
     */
    public static AStarNode createAStarRelations(RoadMap roadMap, Location start, PathPlayer player) {

        Collection<Findable> findables = roadMap.getFindables(player);
        Map<Integer, AStarNode> aStarNodes = new HashMap<>();

        AStarNode playerNode = new AStarNode(-1, 0);

        double nearestDist = 100;
        AStarNode nearest = null;

        for (Findable findable : findables) {
            double dist = start.distance(findable.getLocation());
            AStarNode aStarNode = new AStarNode(findable.getDatabaseId(), dist);
            aStarNodes.put(findable.getDatabaseId(), aStarNode);
            if (dist < nearestDist) {
                nearest = aStarNode;
            }
        }

        if (nearest == null) {
            return null;
        }
        playerNode.adjacencies = new AStarEdge[]{new AStarEdge(nearest, nearestDist)};

        for (Findable findable : findables) {
            AStarEdge[] adjacencies = new AStarEdge[findables.size()];
            AStarNode aStarNode = aStarNodes.get(findable.getDatabaseId());

            int i = 0;
            for (Integer edgeId : findable.getEdges()) {
                AStarEdge edgeTarget = new AStarEdge(aStarNodes.get(edgeId), start.distance(findable.getLocation()));
                adjacencies[i] = edgeTarget;
                i++;
            }
            aStarNode.adjacencies = adjacencies;
        }
        return playerNode;
    }
}
