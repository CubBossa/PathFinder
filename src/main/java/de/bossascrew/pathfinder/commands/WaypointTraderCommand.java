package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.CommandPermission;
import de.bossascrew.acf.annotation.Subcommand;
import de.bossascrew.acf.annotation.Syntax;
import org.bukkit.command.CommandSender;

@CommandAlias("waypoint|waypoints|node")
@Subcommand("create")
public class WaypointTraderCommand extends BaseCommand {

    @Subcommand("trader")
    @Syntax("<NPC-ID>")
    @CommandPermission("bcrew.command.waypoint.create")
    public void onTrader(CommandSender sender, int id) {
        sender.sendMessage("yip");
    }
}

