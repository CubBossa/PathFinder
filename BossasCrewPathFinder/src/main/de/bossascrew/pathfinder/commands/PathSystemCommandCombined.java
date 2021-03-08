package main.de.bossascrew.pathfinder.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import main.de.bossascrew.pathfinder.commands.sub.MasterCommand;
import main.de.bossascrew.pathfinder.commands.sub.RoadMapCreate;
import main.de.bossascrew.pathfinder.commands.sub.RoadMapDelete;
import main.de.bossascrew.pathfinder.commands.sub.RoadMapList;
import main.de.bossascrew.pathfinder.commands.sub.SubCommand;

public class PathSystemCommandCombined implements TabExecutor, MasterCommand {

	List<SubCommand> subcommands;

	public void registerSubCommands() {
		subcommands = new ArrayList<SubCommand>();
		subcommands.add(new RoadMapList());
		subcommands.add(new RoadMapCreate());
		subcommands.add(new RoadMapDelete());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return performCommands(sender, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		return getTabCompletions(sender, args);
	}
	
	@Override
	public boolean performCommands(CommandSender sender, String[] args) {
		String commandName = args.length > 0 ? args[0] : "";
		SubCommand commandModule = this.findMatchingCommand(commandName);
		if (commandModule == null) {
			sender.sendMessage("§cError: Unknown Command.");
			return true;
		}
		String[] cmdArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

		if (!commandModule.canConsoleExecute()) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§cError: This command can only be executed by a player.");
				return true;
			}
		} else if (sender instanceof ConsoleCommandSender) {
			commandModule.onCommandExecute(sender, cmdArgs);
			return true;
		}
		commandModule.onCommandExecute(sender, cmdArgs);
		return true;
	}

	@Override
	public List<String> getTabCompletions(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return getCommandNames();
		} else {
			SubCommand subcommand = this.findMatchingCommand(args[0]);
			if (subcommand != null) {
				if (sender instanceof ConsoleCommandSender && !subcommand.canConsoleExecute())
					return new ArrayList<>();

				String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
				return subcommand.onTabComplete(sender, cmdArgs);
			}
		}
		return new ArrayList<>();
	}
	
	public SubCommand findMatchingCommand(String name) {
		for (SubCommand sc : subcommands) {
			if (sc.getName().equalsIgnoreCase(name))
				return sc;
		}
		return null;
	}
	
	public List<String> getCommandNames() {
		List<String> ret = new ArrayList<String>();
		for(SubCommand sc : subcommands) {
			ret.add(sc.getName());
		}
		return ret;
	}
}