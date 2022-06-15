package de.bossascrew.pathfinder.commands.dependencies;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("waypoint|waypoints|node")
@Subcommand("create")
public class WaypointQuesterCommand extends BaseCommand {

    @Subcommand("quester")
    @Syntax("<NPC-ID> [<Name>]")
    @CommandPermission("pathfinder.command.waypoint.create")
    public void onTrader(CommandSender sender, int id, @Optional @Single String name) {

        RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);
        NPC npc = CitizensAPI.getNPCRegistry().getById(id);

        if (roadMap.isNodeNPC(id)) {
            PlayerUtils.sendMessage(sender, ChatColor.RED + "Dieser NPC ist bereits ein Wegpunkt.");
            return;
        }
        roadMap.createQuestFindable(id, name, null, null);
        PlayerUtils.sendMessage(sender, PathPlugin.PREFIX + "Node erfolgreich erstellt: " + PathPlugin.CHAT_COLOR_LIGHT + name);
    }
}

