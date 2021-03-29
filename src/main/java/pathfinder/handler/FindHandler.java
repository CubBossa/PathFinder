package pathfinder.handler;

import lombok.Getter;
import pathfinder.RoadMap;
import pathfinder.util.AStar;
import pathfinder.util.AStarNode;

import java.util.List;

public class FindHandler {

    @Getter
    private static FindHandler instance;

    public FindHandler() {
        instance = this;
    }


    public List<AStarNode> generateAStarMap(RoadMap roadMap) {

        return null;
    }
}
