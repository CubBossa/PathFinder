package pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import org.bukkit.entity.Player;
import pathfinder.PathPlugin;
import pathfinder.RoadMap;

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

    //TODO vllt forcefind und forceforget noch in roadmap command

    @Subcommand("forcefind")
    public void onForceFind(Player player, Player target, RoadMap roadMap, String nodename, @Optional boolean grouped) {
        //TODO lasse einen Spieler eine Node finden. Wenn nodename = * dann alle. Wenn grouped, dann als gruppen finden, sonst einzeln.

    }

    @Subcommand("forceforget")
    public void onForceForget() {
        //TODO lasse Spieler nodes vergessen.
    }
}
