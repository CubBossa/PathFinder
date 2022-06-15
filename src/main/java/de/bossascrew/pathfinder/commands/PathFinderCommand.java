package de.bossascrew.pathfinder.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

@CommandAlias("pathfinder")
public class PathFinderCommand extends BaseCommand {

	@Subcommand("reload")
	@CommandPermission("pathfinder.command.pathfinder.reload")
	public void onCleanUp(CommandSender sender, @Optional World world) {


	}
}
