package de.bossascrew.pathfinder;

import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.findable.PlayerFindable;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.handler.VisualizerHandler;
import de.bossascrew.pathfinder.util.AStarUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Objects;

public class PathFinderAPI {

    public void findFindable(Player player, Node findable) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
        if (pathPlayer == null) {
            return;
        }
        pathPlayer.find(findable, false, new Date());
    }

    public void forgetFindable(Player player, Node findable) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
        if (pathPlayer == null) {
            return;
        }
        pathPlayer.unfind(findable, false);
    }

    public void findFindableGroup(Player player, Node findable) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
        if (pathPlayer == null) {
            return;
        }
        pathPlayer.find(findable, true, new Date());
    }

    public void forgetFindableGroup(Player player, Node findable) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player);
        if (pathPlayer == null) {
            return;
        }
        pathPlayer.unfind(findable, true);
    }

    public void showPath(Player player, Node findable) {
        PathPlayer pPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        AStarUtils.startPath(pPlayer, new PlayerFindable(player, findable.getRoadMap()), findable, true);
    }

    public void cancelAllPaths(Player player) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());
        if (pathPlayer == null) {
            return;
        }
        pathPlayer.cancelPaths();
    }

    public void startEditMode(Player player, RoadMap roadMap) {
        roadMap.setEditMode(player.getUniqueId(), true);
    }

    public void stopEditMode(Player player, RoadMap roadMap) {
        roadMap.setEditMode(player.getUniqueId(), false);
    }

    public void stopAllEditModes(RoadMap roadMap) {
        roadMap.cancelEditModes();
    }

    public void stopAllEditModes() {
        for (RoadMap rm : RoadMapHandler.getInstance().getRoadMaps()) {
            rm.cancelEditModes();
        }
    }

    public void selectRoadMap(CommandSender sender, RoadMap roadMap) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);
        if (pathPlayer == null) {
            return;
        }
        pathPlayer.setSelectedRoadMap(roadMap.getRoadmapId());
    }

    public void unselectRoadMap(CommandSender sender) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);
        if (pathPlayer == null) {
            return;
        }
        pathPlayer.deselectRoadMap();
    }

    public void unselectRoadMapIfSelected(CommandSender sender, RoadMap roadMap) {
        PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(sender);
        if (pathPlayer == null) {
            return;
        }
        pathPlayer.deselectRoadMap(roadMap.getRoadmapId());
    }

    public @Nullable
    Node getFindable(RoadMap roadMap, String name) {
        return roadMap.getFindable(name);
    }

    public @Nullable
    Node getFindable(int databaseId) {
        for (RoadMap roadMap : RoadMapHandler.getInstance().getRoadMaps()) {
            Node f = roadMap.getFindable(databaseId);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    public Node createNode(String name, RoadMap roadMap, Vector position) {
        return roadMap.createNode(position, name);
    }

    public void deleteFindable(int databaseId) {
        RoadMapHandler.getInstance().getRoadMaps().forEach(rm -> rm.deleteFindable(databaseId));
    }

    public void deleteFindable(Node findable) {
        findable.getRoadMap().deleteFindable(findable.getNodeId());
    }

    public @Nullable
    RoadMap getRoadMap(String name) {
        return RoadMapHandler.getInstance().getRoadMap(name);
    }

    public @Nullable
    RoadMap getRoadMap(int databaseId) {
        return RoadMapHandler.getInstance().getRoadMap(databaseId);
    }

    public RoadMap createRoadMap(String name, World world) {
        return RoadMapHandler.getInstance().createRoadMap(name, world, false);
    }

    public void deleteRoadMap(int databaseId) {
        RoadMapHandler.getInstance().deleteRoadMap(Objects.requireNonNull(RoadMapHandler.getInstance().getRoadMap(databaseId)));
    }

    public void deleteRoadMap(RoadMap roadMap) {
        RoadMapHandler.getInstance().deleteRoadMap(roadMap);
    }

    public EditModeVisualizer createEditModeVisualizer(String name, int parentId) {
        EditModeVisualizer parent = VisualizerHandler.getInstance().getEditModeVisualizer(parentId);
        return VisualizerHandler.getInstance().createEditModeVisualizer(name, parent, null, null, null, null, null, null);
    }

    void deleteEditModeVisualizer(int databaseId) {
        VisualizerHandler.getInstance().deleteEditModeVisualizer(databaseId);
    }

    PathVisualizer createPathVisualizer(String name, int parentId) {
        PathVisualizer parent = VisualizerHandler.getInstance().getPathVisualizer(parentId);
        return VisualizerHandler.getInstance().createPathVisualizer(name, parent, null, null, null, null, null);
    }

    void deletePathVisualizer(int databaseId) {
        VisualizerHandler.getInstance().deletePathVisualizer(databaseId);
    }
}
