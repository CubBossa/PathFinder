package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.data.PathPlayer;
import de.cubbossa.pathfinder.data.PathPlayerHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import lombok.experimental.UtilityClass;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.List;

@UtilityClass
public class CommandUtils {

    public <T> List<T> subList(List<T> list, int page, int pageSize) {
        return list.subList(Integer.min(page * pageSize, list.size() == 0 ? 0 : list.size() - 1), Integer.min((page + 1) * pageSize, list.size()));
    }

    public @Nullable
	RoadMap getAnyRoadMap(World world) {
        return RoadMapHandler.getInstance().getRoadMaps(world).stream().findFirst().orElse(null);
    }

    public RoadMap getSelectedRoadMap(CommandSender sender) {
        return getSelectedRoadMap(sender, true);
    }

    public RoadMap getSelectedRoadMap(CommandSender sender, boolean cancelIfUnselected) {
        PathPlayer pplayer = PathPlayerHandler.getInstance().getPlayer(sender);
        if (pplayer.getSelectedRoadMap() == null) {
            if (!cancelIfUnselected) {
                return null;
            }
            throw new RuntimeException("You have to select a roadmap. (/roadmap select <roadmap>)");
        }
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pplayer.getSelectedRoadMap());
        return roadMap;
    }
}
