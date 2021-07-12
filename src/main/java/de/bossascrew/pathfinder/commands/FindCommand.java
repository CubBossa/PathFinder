package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.BukkitMain;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.Shop;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.QuestFindable;
import de.bossascrew.pathfinder.data.findable.TraderFindable;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import de.bossascrew.pathfinder.util.AStarUtils;
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

import java.util.Comparator;
import java.util.stream.Collectors;

@CommandAlias("finde|find")
public class FindCommand extends BaseCommand {

    @CatchUnknown
    @HelpCommand
    public void onDefault(Player player) {
        Menu menu = new Menu("Finde Orte einer Stadtkarte mit folgenden Befehlen:");
        menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find ort <Straßenkarte> <Ort>")));

        if(PathPlugin.getInstance().isTraders() || PathPlugin.getInstance().isQuests() || (PathPlugin.getInstance().isBentobox()) && PathPlugin.getInstance().isChestShop()) {
            menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find item <Straßenkarte> <Item>")));
        }
        if (PathPlugin.getInstance().isQuests()) {
            menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find quest <Straßenkarte> <Quest>")));
        }
        if (PathPlugin.getInstance().isTraders()) {
            menu.addSub(new ComponentMenu(ComponentUtils.getCommandComponent("/find shop <Straßenkarte> <Shop>")));
        }
    }

    @Subcommand("ort")
    @Syntax("<Ort>")
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_FINDABLE_FINDABLE_GROUPS)
    public void onFindeOrt(Player player, RoadMap roadMap, FindableGroup group) {
        Findable findable = group.stream().findAny().orElse(null);
        if (findable == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Ein Fehler ist aufgetreten.");
            return;
        }
        AStarUtils.startPath(player, findable, true);
    }
}
