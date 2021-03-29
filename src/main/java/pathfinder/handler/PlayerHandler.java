package pathfinder.handler;

import lombok.Getter;
import pathfinder.PathPlayer;

import javax.annotation.Nullable;
import java.util.Map;

public class PlayerHandler {

    @Getter
    private static PlayerHandler instance;

    private Map<Integer, PathPlayer> pathPlayer;

    public PlayerHandler() {
        instance = this;
    }



    public PathPlayer getPlayer(int globalPlayerId) {
        PathPlayer pathPlayer = getLoadedPlayer(globalPlayerId);
        if(pathPlayer == null) {
            pathPlayer = new PathPlayer(globalPlayerId);
        }
        return pathPlayer;
    }

    private @Nullable
    PathPlayer getLoadedPlayer(int globalPlayerId) {
        for(PathPlayer pathPlayer : pathPlayer.values()) {
            if(pathPlayer.getGlobalPlayerId() == globalPlayerId)
                return pathPlayer;
        }
        return null;
    }
}
