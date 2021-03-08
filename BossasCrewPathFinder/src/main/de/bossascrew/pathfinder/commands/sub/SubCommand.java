package main.de.bossascrew.pathfinder.commands.sub;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface SubCommand {

	public void onCommandExecute(CommandSender sender, String[] args);
	
	public List<String> onTabComplete(CommandSender sender, String[] args);
	
	public String getName();
	
	public boolean canConsoleExecute();
}
