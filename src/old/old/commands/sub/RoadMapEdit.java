package de.bossascrew.pathfinder.old.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class RoadMapEdit implements MasterCommand, SubCommand {

    List<SubCommand> subcommands;

    @Override
    public void registerSubCommands() {
        subcommands = new ArrayList<SubCommand>();
        subcommands.add(new ParentCommand("waypoint", false, new SubCommand[]{
                new WaypointRemove()
        }));
        //editmode
    }

    @Override
    public void onCommandExecute(CommandSender sender, String[] args) {
        if (args.length > 1) {
            RoadMap rm = RoadMap.getRoadMap(args[0]);
            if (rm == null) {
                sender.sendMessage("�cError: Roadmap does not exist.");
                return;
            }
        } else {
            sender.sendMessage("�cError: Missing arguments.");
            return;
        }
        performCommands(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return getTabCompletions(sender, args);
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public boolean canConsoleExecute() {
        return true;
    }

    @Override
    public boolean performCommands(CommandSender sender, String[] args) {
        String commandName = args.length > 0 ? args[0] : "";
        SubCommand commandModule = this.findMatchingCommand(commandName);
        if (commandModule == null) {
            sender.sendMessage("�cError: Unknown Command.");
            return true;
        }
        String[] cmdArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        if (!commandModule.canConsoleExecute()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("�cError: This command can only be executed by a player.");
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
        } else if (args.length > 2) {
            SubCommand subcommand = this.findMatchingCommand(args[0]);
            if (subcommand != null) {
                if (sender instanceof ConsoleCommandSender && !subcommand.canConsoleExecute()) {
                    return new ArrayList<>();
                }

                String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
                return subcommand.onTabComplete(sender, cmdArgs);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public SubCommand findMatchingCommand(String name) {
        for (SubCommand sc : subcommands) {
            if (sc.getName().equalsIgnoreCase(name)) {
                return sc;
            }
        }
        return null;
    }

    @Override
    public List<String> getCommandNames() {
        List<String> ret = new ArrayList<String>();
        for (SubCommand sc : subcommands) {
            ret.add(sc.getName());
        }
        return ret;
    }

}
