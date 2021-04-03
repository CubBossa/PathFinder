package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import org.bukkit.entity.Player;

@CommandAlias("finde|find")
public class FindeCommand extends BaseCommand {

    @Default
    @Subcommand("help|hilfe")
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
