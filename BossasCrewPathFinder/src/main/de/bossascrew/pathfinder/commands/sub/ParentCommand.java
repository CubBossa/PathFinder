package main.de.bossascrew.pathfinder.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ParentCommand implements MasterCommand, SubCommand {

	String name;
	boolean consoleCommand;
	List<SubCommand> subcommands;
	
	public ParentCommand(String name, boolean consoleCommand, SubCommand[] sbcmds) {
		this.name = name;
		this.consoleCommand = consoleCommand;
		subcommands = new ArrayList<SubCommand>();
		for(SubCommand sc : sbcmds) {
			subcommands.add(sc);
		}
	}
	
	@Override
	public void registerSubCommands() {}

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
		if (args.length == 2) {
			return getCommandNames();
		} else if(args.length > 2){
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

	@Override
	public SubCommand findMatchingCommand(String name) {
		for (SubCommand sc : subcommands) {
			if (sc.getName().equalsIgnoreCase(name))
				return sc;
		}
		return null;
	}

	@Override
	public List<String> getCommandNames() {
		List<String> ret = new ArrayList<String>();
		for(SubCommand sc : subcommands) {
			ret.add(sc.getName());
		}
		return ret;
	}

	@Override
	public void onCommandExecute(CommandSender sender, String[] args) {
		performCommands(sender, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		return getTabCompletions(sender, args);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean canConsoleExecute() {
		return consoleCommand;
	}

}
