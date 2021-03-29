package pathfinder;

import de.bossascrew.core.player.PlayerHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pathfinder.data.DatabaseModel;
import pathfinder.data.FoundInfo;
import pathfinder.util.AStar;
import pathfinder.util.AStarNode;
import pathfinder.util.Path;

import java.util.*;

public class PathPlayer {

    @Getter
    private int globalPlayerId;
    @Getter
    private UUID uuid;

    private Map<Integer, FoundInfo> foundInfos;

    private Map<Integer, Path> activePaths;

    public PathPlayer(int globalPlayerId) {
        this.globalPlayerId = globalPlayerId;
        this.uuid = PlayerHandler.getInstance().getGlobalPlayer(globalPlayerId).getPlayerId();

        foundInfos = DatabaseModel.getInstance().loadFoundNodes(globalPlayerId);
    }

    public void find(RoadMap roadMap, Node node) {
        NodeGroup group = roadMap.getNodeGroup(node.getNodeGroupId());
        if(group == null) {
            findUngrouped(node);
        } else {
            for(Node groupedNode : group.getNodes()) {
                find(roadMap, groupedNode);
            }
        }
    }

    public void findUngrouped(Node node) {
        assert !hasFound(node.getDatabaseId());

        FoundInfo info = DatabaseModel.getInstance().newFoundInfo(globalPlayerId, node.getDatabaseId(), new Date());
        foundInfos.put(info.getDatabaseId(), info);
    }

    public void unfindNode(Node node) {
        unfindNode(node.getDatabaseId());
    }

    public void unfindNode(int nodeId) {
        FoundInfo toRemove = null;
        for(FoundInfo info : foundInfos.values()) {
            if(info.getNodeId() == nodeId)
                toRemove = info;
        }
        foundInfos.remove(toRemove.getDatabaseId());

        DatabaseModel.getInstance().deleteFoundNode(toRemove);
    }

    public boolean hasFound(int nodeId) {
        return hasFound(foundInfos.values(), nodeId);
    }

    public boolean hasFound(Collection<FoundInfo> infos, int nodeId) {
        for(FoundInfo info : infos) {
            if(info.getNodeId() == nodeId) return true;
        }
        return false;
    }

    public void setPath(Node targetNode) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        setPath(player, targetNode);
    }

    public void setPath(Player player, Node targetNode) {

        AStar aStar = new AStar();
        //aStar.aStarSearch(startNode, goalNode);
        //return aStar.printPath(goalNode);

        //TODO astarmap aus roadmap erzeugen, astar aufruf, path erzeugen und setzen
    }

    public void setPath(Path path) {
        assert path != null;
        activePaths.put(path.getRoadMapId(), path);
    }

    public void cancelPaths() {
        for(Path path : activePaths.values()) {
            path.cancel();
        }
        activePaths.clear();
    }

    public void cancelPath(RoadMap roadMap) {
        Path toBeCancelled = activePaths.get(roadMap.getDatabaseId());
        assert toBeCancelled != null;

        toBeCancelled.cancel();
        activePaths.remove(toBeCancelled);
    }

    public void pauseActivePath(RoadMap roadMap) {
        Path active = activePaths.get(roadMap.getDatabaseId());
        assert active != null;
        active.cancel();
    }

    public void pauseActivePaths() {
        for(Path path : activePaths.values()) {
            path.cancel();
        }
    }

    public void resumePausedPath(RoadMap roadMap) {
        Path paused = activePaths.get(roadMap.getDatabaseId());
        assert paused != null;
        paused.run();
    }

    public void resumePausedPaths() {
        for(Path path : activePaths.values()) {
            path.run();
        }
    }

    public AStarNode asNode(Location location) {
        return new AStarNode(-1, 0);
    }
}
