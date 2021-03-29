package pathfinder.old.commands.sub;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface MasterCommand {

	public void registerSubCommands();
	
	public boolean performCommands(CommandSender sender, String[] args);
	
	public List<String> getTabCompletions(CommandSender sender, String[] args);
	
	public SubCommand findMatchingCommand(String name);
	
	List<String> getCommandNames();
}
