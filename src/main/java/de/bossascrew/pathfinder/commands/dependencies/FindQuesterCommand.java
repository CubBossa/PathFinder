package de.bossascrew.pathfinder.commands.dependencies;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.RoadMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("finde|find")
public class FindQuesterCommand extends BaseCommand {

    @Subcommand("quest")
    @Syntax("<Quest>")
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_QUESTS)
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_QUESTERS)
    public void onFindeQuest(Player player, RoadMap roadMap, String questerName) {
        if(!roadMap.getWorld().equals(player.getWorld())) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Diese Straßenkarte liegt nicht in deiner aktuellen Welt.");
            return;
        }
    }
}
