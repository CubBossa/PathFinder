package de.bossascrew.pathfinder.commands.dependencies;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.CommandAlias;
import de.bossascrew.acf.annotation.CommandCompletion;
import de.bossascrew.acf.annotation.Subcommand;
import de.bossascrew.acf.annotation.Syntax;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.Shop;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.QuestFindable;
import de.bossascrew.pathfinder.data.findable.TraderFindable;
import de.bossascrew.pathfinder.util.hooks.ChestShopHook;
import de.bossascrew.pathfinder.util.hooks.QuestsHook;
import de.bossascrew.pathfinder.util.hooks.TradersHook;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("finde|find")
public class FindItemCommand extends BaseCommand {

    @Subcommand("item")
    @Syntax("<Item>")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_MATERIALS)
    public void onFindeItem(Player player, RoadMap roadMap, Material material) {

        ComponentMenu menu = new ComponentMenu(Component.text("Item ", NamedTextColor.GRAY)
                .append(Component.translatable(material.getTranslationKey(), NamedTextColor.AQUA))
                .append(Component.text(" gefunden:", NamedTextColor.GRAY)));

        if (TradersHook.getInstance() != null) {
            Menu traderMenu = new Menu("Händler:");
            for (Findable f : roadMap.getFindables().stream().filter(findable -> findable instanceof TraderFindable).collect(Collectors.toList())) {
                TraderFindable trader = (TraderFindable) f;

                Shop.ShopItem buy = trader.getShop().getBuyItemMap().values().stream().filter(shopItem -> shopItem.getItemStack().getType() == material).findAny().orElse(null);
                Shop.ShopItem sell = trader.getShop().getSellItemMap().values().stream().filter(shopItem -> shopItem.getItemStack().getType() == material).findAny().orElse(null);
                if (buy == null && sell == null) {
                    continue;
                }
                traderMenu.addSub(new ComponentMenu(getTargetComponent(Component.text(trader.getName(), NamedTextColor.BLUE)
                                .append(Component.text(" | Ankauf: " + buy.getPrice() + "D ")
                                        .hoverEvent(HoverEvent.showItem(Key.key(material.getKey().asString()), 1)))
                                .append(Component.text(" | Verkauf: " + buy.getPrice() + "D ")
                                        .hoverEvent(HoverEvent.showItem(Key.key(material.getKey().asString()), 1))),
                        "/find shop " + trader.getName())));
            }
            if (traderMenu.hasSubs()) {
                menu.addSub(traderMenu);
            }
        }
        if (QuestsHook.getInstance() != null) {
            Menu questsMenu = new Menu("Quests:");
            for (Findable f : roadMap.getFindables().stream().filter(findable -> findable instanceof QuestFindable).collect(Collectors.toList())) {
                //TODO
            }
            if (questsMenu.hasSubs()) {
                menu.addSub(questsMenu);
            }
        }
        if (ChestShopHook.getInstance() != null) {
            menu.addSub(new ComponentMenu(Component.text("Spielershops: ")
                    .append(Component.text("[Übersicht öffnen]", NamedTextColor.GREEN))));
        }

        if (menu.hasSubs()) {
            PlayerUtils.sendComponents(player, menu.toComponents());
        } else {
            PlayerUtils.sendMessage(player, PathPlugin.PREFIX + "Keine Möglichkeit gefunden, dieses Item zu handeln.");
        }
    }

    private Component getTargetComponent(Component text, String command) {
        return text
                .hoverEvent(HoverEvent.showText(Component.text("Klicke, um Navigation zu starten")))
                .clickEvent(ClickEvent.runCommand(command));
    }

}
