package de.bossascrew.pathfinder.commands;

import de.bossascrew.acf.BaseCommand;
import de.bossascrew.acf.annotation.*;
import de.bossascrew.core.base.ComponentMenu;
import de.bossascrew.core.base.Menu;
import de.bossascrew.core.bukkit.player.PlayerUtils;
import de.bossascrew.core.util.ComponentUtils;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
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
    @CommandCompletion(PathPlugin.COMPLETE_ROADMAPS + " " + PathPlugin.COMPLETE_FINDABLE_LOCATIONS)
    public void onFindeOrt(Player player, RoadMap roadMap, String searched) {
        PathPlayer pp = PathPlayerHandler.getInstance().getPlayer(player);
        if(pp == null) {
            return;
        }

        Findable f = roadMap.getFindables().stream()
                .filter(findable -> findable.getGroup() == null)
                .filter(findable -> findable.getName().equalsIgnoreCase(searched))
                .findFirst().orElse(null);
        if(f == null) {
            FindableGroup group = roadMap.getGroups().values().stream()
                    .filter(FindableGroup::isFindable)
                    .filter(g -> g.getName().equalsIgnoreCase(searched))
                    .filter(g -> pp.hasFound(g.getDatabaseId(), true))
                    .findAny().orElse(null);
            if(group == null) {
                PlayerUtils.sendMessage(player, ChatColor.RED + "Dieses Ziel gibt es nicht.");
                return;
            }
            f = group.getFindables().stream().findAny().orElse(null);
        }
        if (f == null) {
            PlayerUtils.sendMessage(player, ChatColor.RED + "Ein Fehler ist aufgetreten.");
            return;
        }
        AStarUtils.startPath(player, f, true);
    }
}
