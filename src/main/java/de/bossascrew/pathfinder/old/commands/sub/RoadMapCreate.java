package de.bossascrew.pathfinder.old.commands.sub;

import java.util.ArrayList;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import de.bossascrew.pathfinder.old.data.Message;

public class RoadMapCreate implements SubCommand {

	@Override
	public void onCommandExecute(CommandSender sender, String[] args) {
		
		String roadmapName = args[1];
		RoadMap rm = RoadMap.getRoadMap(roadmapName);
		if(rm != null) {
			sender.sendMessage(Message.ALREADY_SUCH_ROADMAP);
			return;
		}
		Player p;
		World w = null;
		if(sender instanceof ConsoleCommandSender) {
			if(args.length == 2) {
				w = Bukkit.getWorld(args[1]);
				if(w == null) {
					System.out.println("�cError: World does not exist.");
					return;
				}
			}
		} else {
			p = (Player) sender;
			w = p.getWorld();
		}
		new RoadMap(roadmapName, w);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> completions = new ArrayList<String>();
		if(args.length == 2) {
			for(World w : Bukkit.getWorlds()) {
				completions.add(w.getName());
			}
		}
		return completions;
	}

	@Override
	public String getName() {
		return "create";
	}

	@Override
	public boolean canConsoleExecute() {
		return true;
	}

}
