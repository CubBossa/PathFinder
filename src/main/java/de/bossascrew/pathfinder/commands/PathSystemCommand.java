package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.CommandPermission;
import de.bossascrew.acf.annotation.Default;
import de.bossascrew.acf.annotation.Subcommand;
import org.bukkit.entity.Player;

@CommandAlias("pathsystem|ps")
@CommandPermission("brew.command.pathsystem")
public class PathSystemCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    public void onHelp(Player player) {
        //TODO bedienungsanleitung senden
    }
}
