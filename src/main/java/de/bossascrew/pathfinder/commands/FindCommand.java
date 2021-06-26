package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.Default;
import de.bossascrew.acf.annotation.Subcommand;
import de.bossascrew.acf.annotation.Syntax;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.util.hooks.ChestShopHook;
import org.bukkit.entity.Player;

@CommandAlias("finde|find")
public class FindCommand extends BaseCommand {

    @Default
    @Subcommand("hilfe")
    public void onDefault(Player player) {
        //TODO erklärung wie finden zu nutzen
    }

    @Subcommand("item")
    @Syntax("<Item>")
    public void onFindeItem(Player player) {


        if(ChestShopHook.getInstance() != null) {
            //TODO liste alle gefundenen Chestshops auf und ein Clickbares Component, das dann ein /is teleports menü öffnet,
            // bloß nur mit inseln auf denen so ein Chestshop steht und die ein Warp schild haben.
        }
    }

    @Subcommand("ort")
    @Syntax("<Ort>")
    public void onFindeOrt(Player player) {

    }

    @Subcommand("shop")
    @Syntax("<Shop>")
    public void onFindeShop(Player player) {

    }
}
