package de.bossascrew.pathfinder;

import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.data.visualisation.EditModeVisualizer;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface PathFinderAPI {

    void findFindable(Player player, Findable findable);

    void forgetFindable(Player player, Findable findable);

    void findFindableGrouped(Player player, Findable findable);

    void forgetFindableGrouped(Player player, Findable findable);

    void showPath(Findable findable);

    void cancelAllPaths(Player player);

    void startEditMode(Player player, RoadMap roadMap);

    void stopEditMode(Player player, RoadMap roadMap);

    void stopAllEditModes(Player player);

    void selectRoadMap(CommandSender sender, RoadMap roadMap);

    void unselectRoadMap(CommandSender sender);

    void unselectRoadMapIfSelected(CommandSender sender, RoadMap roadMap);

    Findable getFindable(RoadMap roadMap, String name);

    Findable getFindable(int databaseId);

    Node createNode(String name, RoadMap roadMap, Vector position);

    void deleteFindable(int databaseId);

    void deleteFindable(Findable findable);

    RoadMap getRoadMap(String name);

    RoadMap getRoadMap(int databaseId);

    RoadMap createRoadMap(String name, World world);

    void deleteRoadMap(int databaseId);

    void deleteRoadMap(RoadMap roadMap);

    EditModeVisualizer createEditModeVisualizer(String name, int parentId);

    void deleteEditModeVisualizer(int databaseId);

    PathVisualizer createPathVisualizer(String name, int parentId);

    void deletePathVisualizer(int databaseId);
}
