package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.Default;
import de.bossascrew.acf.annotation.Subcommand;
import de.bossascrew.acf.annotation.Syntax;
import org.bukkit.entity.Player;

@CommandAlias("finde|find")
public class FindeCommand extends BaseCommand {

    @Default
    @Subcommand("hilfe")
    public void onDefault(Player player) {
        //erklärung wie finden zu nutzen
    }

    @Subcommand("item")
    @Syntax("[item]")
    public void onFindeItem(Player player) {

    }

    @Subcommand("ort")
    @Syntax("[ort]")
    public void onFindeOrt(Player player) {

    }

    @Subcommand("shop")
    @Syntax("[shop]")
    public void onFindeShop(Player player) {

    }
}
