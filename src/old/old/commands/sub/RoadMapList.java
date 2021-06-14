package de.bossascrew.pathfinder.old.commands.sub;

import java.util.ArrayList;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.TextComponent;

public class RoadMapList implements SubCommand {

    @Override
    public void onCommandExecute(CommandSender sender, String[] args) {
        TextComponent message = new TextComponent("Alle geladenen Stra�enkarten:\n");
        for (RoadMap roadmap : RoadMap.getRoadMaps()) {
            TextComponent nodeString = new TextComponent(" �8- �a" + roadmap.getKey() + "�7, Welt: �a" + roadmap.getWorld().getName()
                    + "�7, Wegpunkte: �a" + roadmap.getFile().waypoints.size() + "\n");
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
