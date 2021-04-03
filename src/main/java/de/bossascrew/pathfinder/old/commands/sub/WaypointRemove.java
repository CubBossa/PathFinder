package de.bossascrew.pathfinder.old.commands.sub;

import java.util.ArrayList;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.bossascrew.pathfinder.old.system.Node;

public class WaypointRemove implements SubCommand {
	
	RoadMap rm;
	
	@Override
	public void onCommandExecute(CommandSender sender, String[] args) {
		if(args.length == 1) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(!rm.getEditMode().contains(p.getUniqueId())) {
					rm.toggleEdit(p);
				}
			}
			Node n = rm.getFile().getNode(args[0]);
			rm.removeWaypoint(n.id);
			
			rm.getVisualizer().refresh();
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> completions = new ArrayList<String>();
		if(args.length == 1) {
			for(Node n : rm.getFile().waypoints) {
				completions.add(n.value);
			}
		}
		return completions;
	}

	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public boolean canConsoleExecute() {
		return false;
	}
}
