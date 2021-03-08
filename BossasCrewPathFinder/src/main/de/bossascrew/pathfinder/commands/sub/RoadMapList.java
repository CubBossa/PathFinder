package main.de.bossascrew.pathfinder.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import main.de.bossascrew.pathfinder.RoadMap;
import net.md_5.bungee.api.chat.TextComponent;

public class RoadMapList implements SubCommand {

	@Override
	public void onCommandExecute(CommandSender sender, String[] args) {
		TextComponent message = new TextComponent("Alle geladenen Straﬂenkarten:\n");
		for(RoadMap roadmap : RoadMap.getRoadMaps()) {
			TextComponent nodeString = new TextComponent(" ß8- ßa" + roadmap.getKey() + "ß7, Welt: ßa" + roadmap.getWorld().getName()
				+ "ß7, Wegpunkte: ßa" + roadmap.getFile().waypoints.size() + "\n");
			message.addExtra(nodeString);
		}
		sender.sendMessage(message);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public boolean canConsoleExecute() {
		return true;
	}
}
