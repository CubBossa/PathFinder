package de.bossascrew.pathfinder.old.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.bossascrew.pathfinder.old.RoadMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import main.de.bossascrew.pathfinder.PathSystem;
import de.bossascrew.pathfinder.old.data.Message;
import de.bossascrew.pathfinder.old.system.Node;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class PathSystemCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player p = (Player) sender;
        if (!(p instanceof Player)) {
            return false;
        }

        if (args.length < 1) {
            usage(p);
            return false;
        }
        if (args[0].equalsIgnoreCase("list")) {

            TextComponent message = new TextComponent("Alle geladenen Stra�enkarten:\n");
            for (RoadMap roadmap : RoadMap.getRoadMaps()) {
                TextComponent nodeString = new TextComponent(" �8- �a" + roadmap.getKey() + "�7, Welt: �a" + roadmap.getWorld().getName()
                        + "�7, Wegpunkte: �a" + roadmap.getFile().waypoints.size() + "\n");
                message.addExtra(nodeString);
            }
            p.sendMessage(message);
        } else if (args[0].equalsIgnoreCase("reload")) {

            //TODO reload

        } else {
            if (args.length < 2) {
                usage(p);
                return false;
            }
            String roadmapName = args[1];
            RoadMap rm = RoadMap.getRoadMap(roadmapName);

            if (args[0].equalsIgnoreCase("create")) {
                if (rm != null) {
                    p.sendMessage(Message.ALREADY_SUCH_ROADMAP);
                    return false;
                }
                new RoadMap(roadmapName, p.getWorld());

            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                if (rm == null) {
                    p.sendMessage(Message.NO_SUCH_ROADMAP);
                    return false;
                }
                rm.delete();
            } else if (args[0].equalsIgnoreCase("edit")) {
                if (rm == null) {
                    p.sendMessage(Message.NO_SUCH_ROADMAP);
                    return false;
                }
                if (args.length < 3) {
                    usage(p);
                    return false;
                }
                if (args[2].equalsIgnoreCase("waypoint") || args[2].equalsIgnoreCase("waypoints")) {
                    if (args[3].equalsIgnoreCase("save")) {
                        toggleEditOff(rm, p);
                        rm.save();
                    } else if (args[3].equalsIgnoreCase("list")) {
                        TextComponent message = new TextComponent("Alle geladenen Wegpunkte: ");
                        for (Node n : rm.getFile().waypoints) {
                            TextComponent nodeString = new TextComponent("�a" + n.value + "�8, �f");
                            List<Content> contents = new ArrayList<Content>();
                            contents.add(new Text("�7Der Wegpunkt liegt bei:"));
                            contents.add(new Text("�7x �a" + n.loc.getBlockX() + "�7, y�a" + n.loc.getBlockY() + "�7, z�a" + n.loc.getBlockZ()));
                            nodeString.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, contents));
                            message.addExtra(nodeString);
                        }
                        p.sendMessage(message);
                    } else {
                        String name = args[4];
                        if (args[3].equalsIgnoreCase("set")) {
                            toggleEditOn(rm, p);
                            Node setNode;
                            boolean nameTaken = false;
                            for (Node n : rm.getFile().waypoints) {
                                if (n.value.equalsIgnoreCase(name)) {
                                    nameTaken = true;
                                }
                            }
                            if (name.equalsIgnoreCase(PathSystem.PLAYER_NODE)) {
                                nameTaken = true;
                            }

                            if ((args.length >= 6 && args[5].equalsIgnoreCase("-o")) || !nameTaken) {
                                setNode = new Node(rm.getFile().getIncrementID(), name, 0, p.getLocation().toVector().add(new Vector(0, 1, 0)));
                                rm.saveWaypoint(setNode);
                            }
                            rm.getVisualizer().refresh();
                        } else if (args[3].equalsIgnoreCase("remove") || args[3].equalsIgnoreCase("delete")) {
                            toggleEditOn(rm, p);
                            Node n = rm.getFile().getNode(name);
                            rm.removeWaypoint(n.id);

                            rm.getVisualizer().refresh();
                        } else if (args[3].equalsIgnoreCase("permission")) {
                            Node n = rm.getFile().getNode(args[4]);
                            if (n != null) {
                                if (args.length >= 6) {
                                    n.permission = args[5];
                                    p.sendMessage("�7Permission set for �9" + n.value + ": �a" + n.permission);
                                }
                            } else {
                                p.sendMessage("No such node"); //TODO verlagern
                            }
                        } else {
                            usage(p);
                            return false;
                        }
                    }
                } else if (args[2].equalsIgnoreCase("editmode") || args[2].equalsIgnoreCase("edit")) {
                    toggleEdit(rm, p);
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<String>();
        List<String> sortedCompletion = new ArrayList<String>();
        if (args.length <= 0) {
            return completions;
        }
        if (args.length == 1) {
            completions.add("create");
            completions.add("remove");
            completions.add("edit");
            completions.add("list");

        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("delete")) {
                for (RoadMap rm : RoadMap.getRoadMaps()) {
                    completions.add(rm.getKey());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("edit")) {
                completions.add("waypoint");
                completions.add("editmode");
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("edit")) {
                if (args[2].equalsIgnoreCase("waypoint")) {
                    completions.add("set");
                    completions.add("remove");
                    completions.add("save");
                    completions.add("permission");
                }
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("edit")) {
                if (args[2].equalsIgnoreCase("waypoint")) {
                    if (args[3].equalsIgnoreCase("remove") || args[3].equalsIgnoreCase("permission")) {
                        RoadMap rm = RoadMap.getRoadMap(args[1]);
                        if (rm != null) {
                            for (Node n : rm.getFile().waypoints) {
                                completions.add(n.value);
                            }
                        }
                    }
                }
            }
        } else if (args.length == 6) {
            if (args[0].equalsIgnoreCase("edit")) {
                if (args[2].equalsIgnoreCase("waypoint")) {
                    if (args[3].equalsIgnoreCase("permission")) {
                        completions.add("none");
                    }
                }
            }
        }
        Collections.sort(completions);
        for (String s : completions) {
            if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                sortedCompletion.add(s);
            }
        }
        return sortedCompletion;
    }

    private void usage(Player p) {

        p.sendMessage("\n--- Stra�enkarten Benutzung ---");
        p.sendMessage("�7- �f/pathsystem list");
        p.sendMessage("�7- �f/pathsystem create [RoadMap]");
        p.sendMessage("�7- �f/pathsystem remove [RoadMap]");
        //TODO rename command
        //p.sendMessage("�7- �f/pathsystem edit [RoadMap] rename [RoadMap]");
        p.sendMessage("�7- �f/pathsystem edit [RoadMap] waypoint set [WayPoint]");
        p.sendMessage("�7- �f/pathsystem edit [RoadMap] waypoint remove [WayPoint]");
        p.sendMessage("�7- �f/pathsystem edit [RoadMap] waypoint editmode");
        p.sendMessage("�7- �f/pathsystem edit [RoadMap] waypoint save");
        p.sendMessage("�7- �f/pathsystem edit [RoadMap] waypoint list");
    }

    private void toggleEditOff(RoadMap rm, Player p) {
        if (rm.getEditMode().contains(p.getUniqueId())) {
            toggleEdit(rm, p);
        }
    }

    private void toggleEditOn(RoadMap rm, Player p) {
        if (!rm.getEditMode().contains(p.getUniqueId())) {
            toggleEdit(rm, p);
        }
    }

    private void toggleEdit(RoadMap rm, Player p) {
        if (rm.getEditMode().contains(p.getUniqueId())) {
            rm.getEditMode().remove(p.getUniqueId());
            p.sendMessage(Message.EDIT_MODE_OFF);
            if (rm.getEditMode().size() < 1) {
                rm.getVisualizer().hide();
            }
        } else {
            rm.getEditMode().add(p.getUniqueId());
            p.sendMessage(Message.EDIT_MODE_ON);
            if (!rm.getVisualizer().isVisualizing()) {
                rm.getVisualizer().visualize();
            }
        }
    }
}
