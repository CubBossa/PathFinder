package de.bossascrew.pathfinder.old.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import de.bossascrew.pathfinder.old.data.Message;
import de.bossascrew.pathfinder.old.data.Permission;
import de.bossascrew.pathfinder.old.system.Node;

public class NavigateCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;

        if (args.length < 2) {
            if (args[0].equalsIgnoreCase("none")) {
                for (RoadMap rm : RoadMap.getRoadMaps()) {
                    rm.getPathFinder().stopPath(p.getUniqueId());
                }
            } else {
                p.sendMessage("Nutze /navigate [Stra�ennetz] [Zielort]");
                return false;
            }
        }
        RoadMap rm = RoadMap.getRoadMap(args[0]);
        if (rm == null) {
            p.sendMessage("Ung�ltiges Stra�ennetz");
            return false;
        }
        Node target = rm.getFile().getNode(args[1]);
        if (target == null) {
            p.sendMessage("Ung�ltiges Ziel");
            return false;
        }
        if (!(p.hasPermission(Permission.NAVIGATE_COMMAND)
                || p.hasPermission(Permission.NAVIGATE_COMMAND_SUBSTRING_BASE + rm.getKey())
                || p.hasPermission(Permission.NAVIGATE_COMMAND_SUBSTRING_BASE + rm.getKey()
                + Permission.NAVIGATE_COMMAND_SUBSTRING_POINT + target.value))) {
            p.sendMessage(Message.NO_PERMISSION);
            return false;
        }
        rm.getPathFinder().showPath(rm, p.getUniqueId(), p.getLocation(), target, p.getWorld());
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<String>();
        List<String> sortedCompletion = new ArrayList<String>();
        if (args.length <= 0) {
            return completions;
        }
        if (args.length == 1) {
            for (RoadMap rm : RoadMap.getRoadMaps()) {
                completions.add(rm.getKey());
            }
        } else if (args.length == 2) {
            RoadMap rm = RoadMap.getRoadMap(args[0]);
            if (rm != null) {
                for (Node n : rm.getFile().waypoints) {
                    completions.add(n.value);
                }
            }
        }
        Collections.sort(completions);
        for (String s : completions) {
            if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                sortedCompletion.add(s);
            }
        }
        return sortedCompletion;
    }
}
