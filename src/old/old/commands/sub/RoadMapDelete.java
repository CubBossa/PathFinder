package de.bossascrew.pathfinder.old.commands.sub;

import java.util.ArrayList;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.command.CommandSender;

import de.bossascrew.pathfinder.old.data.Message;

public class RoadMapDelete implements SubCommand {

    @Override
    public void onCommandExecute(CommandSender sender, String[] args) {
        String roadmapName = args[1];
        RoadMap rm = RoadMap.getRoadMap(roadmapName);
        if (rm == null) {
            sender.sendMessage(Message.NO_SUCH_ROADMAP);
            return;
        }
        rm.delete();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<String>();
        for (RoadMap rm : RoadMap.getRoadMaps()) {
            completions.add(rm.getKey());
        }
        return completions;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public boolean canConsoleExecute() {
        return true;
    }

}
