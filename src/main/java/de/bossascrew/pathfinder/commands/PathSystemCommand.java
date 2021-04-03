package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.pathfinder.PathPlugin;
import org.bukkit.entity.Player;

@CommandAlias("pathsystem|ps")
@CommandPermission(PathPlugin.PERM_COMMAND_PATHSYSTEM)
public class PathSystemCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    public void onHelp(Player player) {
        //TODO bedienungsanleitung senden
    }


    @Subcommand("reload")
    public void onReload(Player player) {
        //TODO falls gebündelte chatmessages, messages neuladen
    }
}
