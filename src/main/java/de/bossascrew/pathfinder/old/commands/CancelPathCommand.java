package de.bossascrew.pathfinder.old.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import de.bossascrew.pathfinder.old.data.Message;
import de.bossascrew.pathfinder.old.data.Permission;

public class CancelPathCommand implements TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(!(sender instanceof Player)) return false;
		Player p = (Player) sender;
		if(p.hasPermission(Permission.CANCEL_PATH_GENERAL)) {
			p.sendMessage(Message.NO_PERMISSION);
			return false;
		}
		if(args.length == 0) {
			for(RoadMap rm : RoadMap.getRoadMaps()) {
				if(!p.hasPermission(Permission.CANCEL_PATH_SUBSTRING + rm.getKey())) {
					p.sendMessage(Message.NO_PERMISSION);
					return false;
				}
				rm.getPathFinder().stopPath(p.getUniqueId());
			}
			p.sendMessage(Message.CANCELLED_PATH);
		} else {
			if(p.hasPermission(Permission.CANCEL_PATH_OTHER)) {
				Player pp = Bukkit.getPlayer(args[0]);
				if(pp == null) {
					p.sendMessage(Message.NO_SUCH_PLAYER);
					return false;
				}
				for(RoadMap rm : RoadMap.getRoadMaps()) {
					rm.getPathFinder().stopPath(pp.getUniqueId());
				}
				p.sendMessage(Message.CANCELLED_PATH_OTHER);
				pp.sendMessage(Message.CANCELLED_PATH_BY_OTHER);
			}
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<String>();
		List<String> sortedCompletion = new ArrayList<String>();
		if(args.length <= 0) return completions;
		if(args.length == 1) {
			if(sender.hasPermission(Permission.CANCEL_PATH_OTHER)) {
				for(Player p : Bukkit.getOnlinePlayers()) {
					completions.add(p.getName());
				}
			}
		}
		Collections.sort(completions);
		for(String s : completions) {
			if(s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
				sortedCompletion.add(s);
			}
		}
		return sortedCompletion;
	}
	
}
