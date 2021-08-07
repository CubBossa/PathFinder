package de.bossascrew.pathfinder.commands.dependencies;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.Shop;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.QuestFindable;
import de.bossascrew.pathfinder.data.findable.TraderFindable;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.util.hooks.ChestShopHook;
import de.bossascrew.pathfinder.util.hooks.QuestsHook;
import de.bossascrew.pathfinder.util.hooks.TradersHook;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("finde|find")
public class FindItemCommand extends BaseCommand {

    @Subcommand("item")
    @Syntax("<Straßenkarte> <Item>")
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_ITEMS)
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + BukkitMain.COMPLETE_MATERIALS_LOWERCASE)
    public void onFindeItem(Player player, RoadMap roadMap, Material material) {

        ComponentMenu menu = new ComponentMenu(Component.text("Item ", NamedTextColor.GRAY)
                .append(Component.translatable(material.getTranslationKey(), PathPlugin.COLOR_LIGHT))
                .append(Component.text(" gefunden:", NamedTextColor.GRAY)));

        if(roadMap.getWorld().equals(player.getWorld())) {
            if (TradersHook.getInstance() != null) {
                PathPlayer pp = PathPlayerHandler.getInstance().getPlayer(player);
                if (pp == null) {
                    return;
                }
                Menu traderMenu = new Menu("Händler:");
                for (Findable f : roadMap.getFindables().stream().filter(findable -> findable instanceof TraderFindable).collect(Collectors.toList())) {
                    TraderFindable trader = (TraderFindable) f;
                    if (!pp.hasFound(trader)) {
                        continue;
                    }
                    Shop.ShopItem buy = trader.getShop().getBuyItemMap().values().stream().filter(shopItem -> shopItem.getItemStack().getType() == material).findAny().orElse(null);
                    Shop.ShopItem sell = trader.getShop().getSellItemMap().values().stream().filter(shopItem -> shopItem.getItemStack().getType() == material).findAny().orElse(null);
                    if (buy == null && sell == null) {
                        continue;
                    }
                    Component c = getTargetComponent(Component.text(trader.getName(), NamedTextColor.BLUE), "/finde shop " + trader.getRoadMap().getName() + " " + trader.getName());
                    Key materialKey = Key.key(material.getKey().asString());
                    if (sell != null) {
                        c = c.append(Component
                                .text(" | Verkauft für ", NamedTextColor.GRAY)
                                .append(Component.text(sell.getPrice() + "", NamedTextColor.YELLOW))
                                .append(Component.text("D", NamedTextColor.GOLD))
                                .hoverEvent(HoverEvent.showItem(materialKey, 1)));
                    }
                    if (buy != null) {
                        c = c.append(Component
                                .text(" | Kauft für ", NamedTextColor.GRAY)
                                .append(Component.text(buy.getPrice() + "", NamedTextColor.YELLOW))
                                .append(Component.text("D", NamedTextColor.GOLD))
                                .hoverEvent(HoverEvent.showItem(materialKey, 1)));
                    }
                    traderMenu.addSub(new ComponentMenu(c));

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
        }
        if (ChestShopHook.getInstance() != null) {
            menu.addSub(new ComponentMenu(Component.text("Spielershops: ")
					.append(Component.text("[Übersicht öffnen]", PathPlugin.COLOR_LIGHT))));
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
