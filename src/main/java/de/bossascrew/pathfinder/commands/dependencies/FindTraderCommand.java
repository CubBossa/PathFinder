package de.bossascrew.pathfinder.commands.dependencies;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.CommandCompletion;
import de.bossascrew.acf.annotation.Subcommand;
import de.bossascrew.acf.annotation.Syntax;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.TraderFindable;
import de.bossascrew.pathfinder.util.AStarUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("finde|find")
public class FindTraderCommand extends BaseCommand {

    @Subcommand("shop")
    @Syntax("<Shop>")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_TRADERS)
    public void onFindeShop(Player player, RoadMap roadMap, String shopName) {

        Findable target = null;
        for (Findable f : roadMap.getFindables().stream().filter(findable -> findable instanceof TraderFindable).collect(Collectors.toList())) {
            TraderFindable tf = (TraderFindable) f;
            if (tf.getShop().getName().equalsIgnoreCase(shopName)) {
                target = tf;
                break;
            }
        }
        if (target == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Es konnte kein Shop mit diesem Namen gefunden werden: " + shopName);
            return;
        }
        AStarUtils.startPath(player, target);
    }
}
