package pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.Subcommand;
import de.bossascrew.acf.annotation.Syntax;
import org.bukkit.entity.Player;

@CommandAlias("roadmap|rm")
public class RoadMapCommand extends BaseCommand {

    @Subcommand("create")
    @Syntax("[name]")
    public void onCreate(Player player, String name) {

    }

    @Subcommand("delete")
    @Syntax("[name]")
    public void onDelete(Player player, String name) {

    }

    @Subcommand("edit")
    public void onEdit(Player player, String name) {

    }
}
