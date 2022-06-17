package de.bossascrew.pathfinder.util;

import co.aikar.commands.ConditionFailedException;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.roadmap.RoadMapHandler;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

@UtilityClass
public class CommandUtils {

    public @Nullable RoadMap getAnyRoadMap(World world) {
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
            throw new ConditionFailedException("Du musst eine Straßenkarte ausgewählt haben. (/roadmap select <Straßenkarte>)");
        }
        RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(pplayer.getSelectedRoadMap());
        return roadMap;
    }
}
