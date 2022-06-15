package de.bossascrew.pathfinder.commands.dependencies;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.node.Waypoint;
import de.bossascrew.pathfinder.util.AStarUtils;
import de.bossascrew.pathfinder.util.CommandUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("finde|find")
public class FindTraderCommand extends BaseCommand {

    @Subcommand("shop")
    @Syntax("<Shop>")
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_TRADERS)
    @CommandCompletion(PathPlugin.COMPLETE_TRADERS)
    public void onFindeShop(Player player, String shopName) {
        RoadMap roadMap = CommandUtils.getAnyRoadMap(player.getWorld());
        if(roadMap == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Keine Straßenkarte gefunden.");
            return;
        }

        if(!roadMap.getWorld().equals(player.getWorld())) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Diese Straßenkarte liegt nicht in deiner aktuellen Welt.");
            return;
        }

        Waypoint target = roadMap.getNodes().stream()
                .filter(findable -> findable instanceof TraderFindable)
                .map(f -> (TraderFindable) f)
                .filter(f -> f.getName().equalsIgnoreCase(shopName))
                .findAny().orElse(null);

        if (target == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Es konnte kein Shop mit diesem Namen gefunden werden: " + shopName);
            return;
        }
        AStarUtils.startPath(player, target);
    }
}
