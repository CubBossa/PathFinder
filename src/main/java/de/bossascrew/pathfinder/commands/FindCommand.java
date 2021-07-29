package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.util.AStarUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
        PlayerUtils.sendComponents(player, menu.toComponents());
    }

    @Subcommand("ort")
    @Syntax("<Ort>")
    @CommandPermission(PathPlugin.PERM_COMMAND_FIND_LOCATIONS)
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_PARAMETER)
    public void onFindeOrt(Player player, RoadMap roadMap, FindableGroup group) {
        Findable findable = group.getFindables().stream().findAny().orElse(null);
        if (findable == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Ein Fehler ist aufgetreten.");
            return;
        }
        AStarUtils.startPath(player, findable, true);
    }
}
