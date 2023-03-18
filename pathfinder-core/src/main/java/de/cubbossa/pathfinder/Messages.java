package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageFile;
import de.cubbossa.translations.MessageMeta;
import de.cubbossa.translations.TranslationHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@MessageFile
public class Messages {

  @MessageMeta("<offset>PathFinder</offset> <dark_gray>»</dark_gray> <gray>")
  public static final Message PREFIX = new Message("prefix");
  @MessageMeta("<offset_light>true</offset_light>")
  public static final Message GEN_TRUE = new Message("general.true");
  @MessageMeta("<offset_light>false</offset_light>")
  public static final Message GEN_FALSE = new Message("general.false");
  @MessageMeta(value = "<offset_light><x:#.##><gray>,</gray> <y:#.##><gray>,</gray> <z:#.##></offset_light>",
      placeholders = {"x", "y", "z"},
      comment = "The numberformat can be specified as argument for x, y and z. Check out https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html for more information on number formatting.")
  public static final Message GEN_VECTOR = new Message("general.vector");
  @MessageMeta(value = "<main><permission></main>", placeholders = "permission")
  public static final Message GEN_PERMISSION = new Message("general.permission");
  @MessageMeta(value = "<main><particle></main>", placeholders = {"particle", "meta"})
  public static final Message GEN_PARTICLE = new Message("general.particle");
  @MessageMeta(value = "<main><particle> <gray>(<meta>)</gray></main>", placeholders = {"particle",
      "meta"})
  public static final Message GEN_PARTICLE_META = new Message("general.particle");

  @MessageMeta(value = "<main>null</main>")
  public static final Message GEN_NULL = new Message("general.null");
  @MessageMeta("<green>Accept</green>")
  public static final Message GEN_GUI_ACCEPT_N = new Message("general.gui.accept.name");
  @MessageMeta("")
  public static final Message GEN_GUI_ACCEPT_L = new Message("general.gui.accept.lore");
  @MessageMeta("<yellow>Warning")
  public static final Message GEN_GUI_WARNING_N = new Message("general.gui.warning.name");
  @MessageMeta("")
  public static final Message GEN_GUI_WARNING_L = new Message("general.gui.warning.lore");
  @MessageMeta(value = "<white><u><amount> Nodes</u></white>", placeholders = "amount")
  public static final Message GEN_NODE_SEL = new Message("general.selection.nodes");
  @MessageMeta(value = "<white><u><amount> Groups</u></white>", placeholders = "amount")
  public static final Message GEN_GROUP_SEL = new Message("general.selection.groups");
  @MessageMeta(value = "<white><u><amount> Groups</u></white>", placeholders = "amount")
  public static final Message GEN_RM_SEL = new Message("general.selection.roadmaps");

  public static final Message ERROR_PARSE_STRING = new Message("error.parse.string");
  public static final Message ERROR_PARSE_INTEGER = new Message("error.parse.integer");
  public static final Message ERROR_PARSE_DOUBLE = new Message("error.parse.double");
  public static final Message ERROR_PARSE_PERCENT = new Message("error.parse.percent");
  public static final Message ERROR_PARSE_KEY = new Message("error.parse.namespaced_key");

  @MessageMeta(placeholders = "error", value = "<negative>An error occurred while reloading: <error></negative>")
  public static final Message RELOAD_ERROR = new Message("command.reload.error");
  @MessageMeta(value = "<ins:prefix>Successfully reloaded in <offset_light><ms></offset_light><offset>ms</offset>.", placeholders = "ms")
  public static final Message RELOAD_SUCCESS = new Message("command.reload.success.general");
  @MessageMeta(value = "<ins:prefix>Successfully reloaded language in <offset_light><ms></offset_light><offset>ms</offset>.", placeholders = "ms")
  public static final Message RELOAD_SUCCESS_LANG = new Message("command.reload.success.language");
  @MessageMeta(value = "<ins:prefix>Successfully reloaded effects in <offset_light><ms></offset_light><offset>ms</offset>.", placeholders = "ms")
  public static final Message RELOAD_SUCCESS_FX = new Message("command.reload.success.effects");
  @MessageMeta(value = "<ins:prefix>Successfully reloaded config files in <offset_light><ms></offset_light><offset>ms</offset>.", placeholders = "ms")
  public static final Message RELOAD_SUCCESS_CFG = new Message("command.reload.success.config");

  @MessageMeta(value = """
      <gradient:black:dark_gray:black>------------ <offset>Pathfinder</offset> ------------</gradient>
      <gray>Running <offset>Pathfinder v<version></offset>.
            
      <gray>Require help? Checkout the <warm><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></warm>.
      <gray>Use <warm>/pf help</warm> to view available commands.
      """, placeholders = {"version"})
  public static final Message HELP = new Message("general.help");
  @MessageMeta("""
      <gradient:black:dark_gray:black>------------ <offset>Pathfinder</offset> ------------</gradient>
      <gray>Require help? Checkout the <warm><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></warm>.
      			
      <white>Commands:</white>
      <dark_gray>» </dark_gray><gray><warm>/roadmap</warm> - Group waypoints in roadmaps
      <dark_gray>» </dark_gray><gray><warm>/nodegroup</warm> - Add behaviour to multiple waypoints
      <dark_gray>» </dark_gray><gray><warm>/waypoint</warm> - Create, edit and delete waypoints
      <dark_gray>» </dark_gray><gray><warm>/pathvisualizer</warm> - Compass, particles, placeholders and more
      <dark_gray>» </dark_gray><gray><warm>/gps</warm> - Find the shortest way to a nodegroup
      """)
  public static final Message CMD_HELP = new Message("command.help");

  @MessageMeta("<red>Your command is incomplete. Use one of the syntaxes below:")
  public static final Message CMD_INCOMPLETE = new Message("command.error.incomplete.title");
  @MessageMeta(value = "<dark_gray>» </dark_gray><cmd>", placeholders = "cmd")
  public static final Message CMD_INCOMPLETE_LINE = new Message("command.error.incomplete.line");

  @MessageMeta(value = """
      <gradient:black:dark_gray:black>------------ <offset>Pathfinder</offset> ------------</gradient>
      <dark_gray>» </dark_gray><gray>Authors: <warm><authors></warm>
      <dark_gray>» </dark_gray><gray>Current version: <warm><version></warm>
      <dark_gray>» </dark_gray><gray>Main Game version: <warm><api-version></warm>
      <dark_gray>» </dark_gray><gray>Spigot-Page: <warm><u><click:open_url:"https://www.spigotmc.org/resources/gps-pathfinder-minecraft-pathfinding-tool.104961/">https://www.spigotmc.org/...</click></u></warm>
      """, placeholders = {"authors", "version", "api-version"})
  public static final Message INFO = new Message("commands.info");

  @MessageMeta(value = """
      <offset>Active Modules:</offset>
      <modules:"\n":"<dark_gray>» </dark_gray>">""", placeholders = { "modules" })
  public static final Message MODULES = new Message("commands.modules");

  @MessageMeta(value = """
      <offset>Roadmap:</offset> <name> <gray>(<key>)</gray>
      <dark_gray>» </dark_gray><gray>Name: <main><hover:show_text:"Click to change name"><click:suggest_command:'/roadmap edit <key> name <name-format>'><name-format></click></hover></main>
      <dark_gray>» </dark_gray><gray>Nodes: <nodes></gray>
      <dark_gray>» </dark_gray><gray>Groups: <groups></gray>
      <dark_gray>» </dark_gray><offset>Visualizer:</offset>
      <dark_gray>  » </dark_gray><gray>Path Visualizer: <main><hover:show_text:"Click to change path-visualizer"><click:suggest_command:'/roadmap edit <key> visualizer <path-visualizer>'><path-visualizer></click></hover>
      <dark_gray>  » </dark_gray><gray>Default Curve length: <main><hover:show_text:"Click to change curve length"><click:suggest_command:'/roadmap edit <key> curve-length <curve-length>'><curve-length></click></hover>
      """, placeholders = {"name", "key", "name-format", "curve-length", "path-visualizer", "nodes",
      "groups"})
  public static final Message CMD_RM_INFO = new Message("commands.roadmap.info");
  @MessageMeta("<negative>Could not create Roadmap, another Roadmap with this key already exists.")
  public static final Message CMD_RM_CREATE_DUPLICATE_KEY =
      new Message("commands.roadmap.create.duplicate_key");
  @MessageMeta("<negative>Could not create Roadmap. Check out console for details.")
  public static final Message CMD_RM_CREATE_FAIL = new Message("commands.roadmap.create.fail");
  @MessageMeta(value = "<ins:prefix><gray>Successfully created Roadmap <offset><name></offset>.</gray>", placeholders = "name")
  public static final Message CMD_RM_CREATE_SUCCESS =
      new Message("commands.roadmap.create.success");
  @MessageMeta(value = "<ins:prefix><gray>Successfully deleted Roadmap <offset><roadmap></offset>.</gray>", placeholders = "roadmap")
  public static final Message CMD_RM_DELETE = new Message("commands.roadmap.delete");
  @MessageMeta(value = "<gradient:black:dark_gray:black>------------ <offset>Roadmaps</offset> ------------</gradient>",
      placeholders = {"page", "next-page", "prev-page", "pages"})
  public static final Message CMD_RM_LIST_HEADER = new Message("commands.roadmap.list.header");
  @MessageMeta(value = "<dark_gray> » </dark_gray><name> <gray>(<key>)</gray>",
      placeholders = {"key", "name", "world", "discoverable", "find-distance", "curve-length",
          "path-visualizer"})
  public static final Message CMD_RM_LIST_ENTRY = new Message("commands.roadmap.list.entry");
  @MessageMeta(value = "<gradient:black:dark_gray:black>------------<gray> <click:run_command:/roadmap list <prev-page>>←</click> <page>/<pages> <click:run_command:/roadmap list <next-page>>→</click> </gray>-------------</gradient>",
      placeholders = {"page", "next-page", "prev-page", "pages"})
  public static final Message CMD_RM_LIST_FOOTER = new Message("commands.roadmap.list.footer");
  @MessageMeta("<negative>Please specify a roadmap: /roadmap editmode <roadmap>")
  public static final Message CMD_RM_EM_PROVIDE_RM =
      new Message("commands.roadmap.editmode.specify_roadmap");
  @MessageMeta(value = "<ins:prefix>Editmode activated for <offset><roadmap></offset>.", placeholders = {
      "roadmap"})
  public static final Message CMD_RM_EM_ACTIVATED =
      new Message("commands.roadmap.editmode.activated");
  @MessageMeta(value = "<ins:prefix>Editmode deactivated for <offset><roadmap></offset>.", placeholders = {
      "roadmap"})
  public static final Message CMD_RM_EM_DEACTIVATED =
      new Message("commands.roadmap.editmode.deactivated");
  @MessageMeta("<red>No editor implementation found.</red>")
  public static final Message CMD_RM_EM_NO_IMPL = new Message("commands.roadmap.editmode.no_impl");
  @MessageMeta(value = "<ins:prefix>Player <name> discovered <discovery>.", placeholders = {"name",
      "discovery"})
  public static final Message CMD_RM_FORCE_FIND = new Message("commands.roadmap.force_find");
  @MessageMeta(value = "<ins:prefix>Player <name> forgot about <discovery>.", placeholders = {
      "name", "discovery"})
  public static final Message CMD_RM_FORCE_FORGET = new Message("commands.roadmap.force_forget");
  @MessageMeta(value = "<ins:prefix>Successfully set name for <offset><old-value></offset> to <value>. (<name-format>)</gray>",
      placeholders = {"key", "roadmap", "old-value", "name-format", "value"})
  public static final Message CMD_RM_SET_NAME = new Message("commands.roadmap.set_name");
  @MessageMeta(value = "<ins:prefix>Successfully set curve length for <offset><roadmap></offset> to <offset_light><value></offset_light>",
      placeholders = {"key", "roadmap", "old-value", "value"})
  public static final Message CMD_RM_SET_CURVED = new Message("commands.roadmap.set_curve_length");
  @MessageMeta(value = "<ins:prefix>Successfully set visualizer for <offset><roadmap></offset> " +
      "from <old-value> to <value>.</gray>",
      placeholders = {"key", "roadmap", "old-value", "value"})
  public static final Message CMD_RM_SET_VISUALIZER =
      new Message("commands.roadmap.set_visualizer");

  @MessageMeta(value = "<ins:prefix>Successfully created Node #<id>.", placeholders = "id")
  public static final Message CMD_N_CREATE = new Message("commands.node.create");
  @MessageMeta(value = "<ins:prefix>Successfully deleted <selection>.",
      placeholders = "selection")
  public static final Message CMD_N_DELETE = new Message("commands.node.delete");
  @MessageMeta(value = "<ins:prefix><gray>Updated <selection>.</gray>",
      placeholders = {"selection", "location"})
  public static final Message CMD_N_UPDATED = new Message("commands.node.moved");
  @MessageMeta(value = """
      <offset>Node #<id></offset> <gray>(<roadmap>)</gray>
      <dark_gray>» </dark_gray><gray>Position: <main><position></main> (<world>)
      <dark_gray>» </dark_gray><gray>Curve-Length: <main><curve-length></main>
      <dark_gray>» </dark_gray><gray>Edges: <edges>
      <dark_gray>» </dark_gray><gray>Groups: <groups>
      """, placeholders = {"id", "roadmap", "groups", "position", "world", "curve-length", "edges"})
  public static final Message CMD_N_INFO = new Message("commands.node.info");
  @MessageMeta("<negative>No nodes found to display. Check your selection query.</negative>")
  public static final Message CMD_N_INFO_NO_SEL = new Message("commands.node.info_no_selection");
  @MessageMeta(value = "<ins:prefix>Curve-length set to <length> for <selection>.",
      placeholders = {"selection", "length"})
  public static final Message CMD_N_SET_TANGENT = new Message("commands.node.set_curve_length");
  @MessageMeta(value = "<ins:prefix>Added <nodes> to group <group>.",
      placeholders = {"nodes", "group"})
  public static final Message CMD_N_ADD_GROUP = new Message("commands.node.add_group");
  @MessageMeta(value = "<ins:prefix>Removed <nodes> from group <group>.",
      placeholders = {"nodes", "group"})
  public static final Message CMD_N_REMOVE_GROUP = new Message("commands.node.remove_groups");
  @MessageMeta(value = "<ins:prefix>Cleared all groups for <nodes>.",
      placeholders = {"nodes"})
  public static final Message CMD_N_CLEAR_GROUPS = new Message("commands.node.clear_groups");

  @MessageMeta(value = "<gradient:black:dark_gray:black>------------ <offset>Waypoints</offset> ------------</gradient>",
      placeholders = {"roadmap-key", "roadmap-name", "page", "next-page", "prev-page", "pages"})
  public static final Message CMD_N_LIST_HEADER = new Message("commands.node.list.header");
  @MessageMeta(value = "<dark_gray>» </dark_gray><hover:show_text:'<gray>Groups: <groups><newline><gray>Edges to: <edges><newline><gray>Click for more information'><click:run_command:/waypoint info \"@n[id=<id>]\"><gray>#<id> at <position> (<world>)",
      placeholders = {"id", "position", "world", "curve-length", "edges", "groups"})
  public static final Message CMD_N_LIST_ELEMENT = new Message("commands.node.list.element");
  @MessageMeta(value = "<gradient:black:dark_gray:black>------------<gray> <click:run_command:/waypoint list \"<selector>\" <prev-page>>←</click> <page>/<pages> <click:run_command:/waypoint list \"<selector>\" <next-page>>→</click> </gray>-------------</gradient>",
      placeholders = {"roadmap-key", "roadmap-name", "page", "next-page", "prev-page", "pages"})
  public static final Message CMD_N_LIST_FOOTER = new Message("commands.node.list.footer");
  @MessageMeta(value = "<ins:prefix>Connected <start> to <end>.",
      placeholders = {"start", "end"})
  public static final Message CMD_N_CONNECT = new Message("commands.node.connect.success");
  @MessageMeta(value = "<negative>Nodes cannot be connected to themselves: <start>>",
      placeholders = {"start", "end"})
  public static final Message CMD_N_CONNECT_IDENTICAL =
      new Message("commands.node.connect.identical");
  @MessageMeta(value = "<negative><start> and <end> are already connected.",
      placeholders = {"start", "end"})
  public static final Message CMD_N_CONNECT_ALREADY_CONNECTED =
      new Message("commands.node.connect.already_connected");
  @MessageMeta(value = "<ins:prefix>Disconnected <start> from <end>.",
      placeholders = {"start", "end"})
  public static final Message CMD_N_DISCONNECT = new Message("commands.node.disconnect.success");

  @MessageMeta(value = "<negative>Could not create Nodegroup. Check out console for details.")
  public static final Message CMD_NG_CREATE_FAIL = new Message("commands.node_group.create_fail");
  @MessageMeta(value = "<negative>A node group with this namespaced key (<name>) already exists.</negative>",
      placeholders = "name")
  public static final Message CMD_NG_ALREADY_EXISTS =
      new Message("commands.node_group.already_exists");
  @MessageMeta(value = "<ins:prefix><gray>Node group created: <name>.</gray>",
      placeholders = "name")
  public static final Message CMD_NG_CREATE = new Message("commands.node_group.create");
  @MessageMeta(value = "<ins:prefix><gray>Node group deleted: <name>.</gray>",
      placeholders = "name")
  public static final Message CMD_NG_DELETE = new Message("commands.node_group.delete");
  @MessageMeta(value = "<gradient:black:dark_gray:black>------------ <offset>Node-Groups</offset> ------------</gradient>",
      placeholders = {"page", "next-page", "prev-page", "pages"})
  public static final Message CMD_NG_LIST_HEADER = new Message("commands.node_group.list.header");
  @MessageMeta(value = "<dark_gray> » </dark_gray><name> <gray>(<key>)</gray>",
      placeholders = {"page", "key", "name", "size", "discoverable"})
  public static final Message CMD_NG_LIST_LINE = new Message("commands.node_group.list.line");
  @MessageMeta(value = "<gradient:black:dark_gray:black>------------<gray> <click:run_command:/nodegroup list <prev-page>>←</click> <page>/<pages> <click:run_command:/nodegroup list <next-page>>→</click> </gray>-------------</gradient>",
      placeholders = {"page", "next-page", "prev-page", "pages"})
  public static final Message CMD_NG_LIST_FOOTER = new Message("commands.node_group.list.footer");
  @MessageMeta(value = "<ins:prefix>Displayname set for <key> from <name> to <new-name> (<value>).",
      placeholders = {"key", "name", "old-value", "value"})
  public static final Message CMD_NG_SET_NAME = new Message("commands.node_group.set_name");
  @MessageMeta(value = "<ins:prefix>Permission set for <key> from <old-value> to <value>.",
      placeholders = {"key", "name", "old-value", "value"})
  public static final Message CMD_NG_SET_PERM = new Message("commands.node_group.set_permission");
  @MessageMeta(value = "<ins:prefix>Navigability set for <key> from <old-value> to <value>.",
      placeholders = {"key", "name", "old-value", "value"})
  public static final Message CMD_NG_SET_NAVIGABLE =
      new Message("commands.node_group.set_navigable");
  @MessageMeta(value = "<ins:prefix>Discoverability set for <key> from <old-value> to <value>.",
      placeholders = {"key", "name", "old-value", "value"})
  public static final Message CMD_NG_SET_DISCOVERABLE =
      new Message("commands.node_group.set_discoverable");
  @MessageMeta(value = "<ins:prefix>Find distance set for <key> from <old-value> to <value>.",
      placeholders = {"key", "name", "old-value", "value"})
  public static final Message CMD_NG_SET_FIND_DIST =
      new Message("commands.node_group.set_find_distance");
  @MessageMeta(value = "<ins:prefix>Search terms for <name>:\n<dark_gray>» <offset_light><values></offset_light></dark_gray>",
      placeholders = {"name", "values"})
  public static final Message CMD_NG_TERMS_LIST = new Message("commands.node_group.terms.list");
  @MessageMeta(value = "<ins:prefix>Successfully added search terms to <name>: <offset_light><values></offset_light>", placeholders = {
      "name", "values"})
  public static final Message CMD_NG_TERMS_ADD = new Message("commands.node_group.terms.add");
  @MessageMeta(value = "<ins:prefix>Successfully removed search terms from <name>: <offset_light><values></offset_light>", placeholders = {
      "name", "values"})
  public static final Message CMD_NG_TERMS_REMOVE = new Message("commands.node_group.terms.remove");
  @MessageMeta(value = "<ins:prefix>Navigation started.  [ <aqua><click:run_command:/cancelpath>CANCEL</click></aqua> ]")
  public static final Message CMD_FIND = new Message("commands.find.success");
  @MessageMeta(value = "<ins:prefix>No matching waypoints could be found.")
  public static final Message CMD_FIND_EMPTY = new Message("commands.find.no_nodes_found");
  @MessageMeta(value = "<ins:prefix>The given location is too far away from any waypoint.")
  public static final Message CMD_FIND_TOO_FAR = new Message("commands.find.too_far_away");
  @MessageMeta(value = "<ins:prefix>No possible way could be found to reach that target.")
  public static final Message CMD_FIND_BLOCKED = new Message("commands.find.no_path_found");
  @MessageMeta(value = "<ins:prefix>No visualizer is set for this roadmap.")
  public static final Message CMD_FIND_NO_VIS = new Message("commands.find.no_visualizer_selected");
  @MessageMeta(value = "<ins:prefix>Navigation cancelled.")
  public static final Message CMD_CANCEL = new Message("commands.cancel_path");


  @MessageMeta(value = "<gradient:black:dark_gray:black>------------ <offset>Visualizer</offset> ------------</gradient>",
      placeholders = {"page", "next-page", "prev-page", "pages"})
  public static final Message CMD_VIS_LIST_HEADER =
      new Message("commands.path_visualizer.list.header");
  @MessageMeta(value = "<dark_gray> » </dark_gray><name> <gray>(<key>)</gray>",
      placeholders = {"key", "name", "world", "discoverable", "find-distance", "curve-length",
          "path-visualizer"})
  public static final Message CMD_VIS_LIST_ENTRY =
      new Message("commands.path_visualizer.list.entry");
  @MessageMeta(value = "<gradient:black:dark_gray:black>------------<gray> <click:run_command:/roadmap list <prev-page>>←</click> <page>/<pages> <click:run_command:/roadmap list <next-page>>→</click> </gray>-------------</gradient>",
      placeholders = {"page", "next-page", "prev-page", "pages"})
  public static final Message CMD_VIS_LIST_FOOTER =
      new Message("commands.path_visualizer.list.footer");

  @MessageMeta(value = "<ins:prefix><gray>Successfully created Visualizer <offset><name></offset> (<name-format>) of type '<type>'.</gray>",
      placeholders = {"key", "name", "name-format", "type"})
  public static final Message CMD_VIS_CREATE_SUCCESS =
      new Message("commands.path_visualizer.create.success");
  @MessageMeta("<negative>Another visualizer with this name already exists.")
  public static final Message CMD_VIS_NAME_EXISTS =
      new Message("commands.path_visualizer.create.already_exists");
  @MessageMeta(value = "<ins:prefix><gray>Successfully deleted Visualizer <offset><name></offset>.</gray>", placeholders = "key, name, nameformat")
  public static final Message CMD_VIS_DELETE_SUCCESS =
      new Message("commands.path_visualizer.delete.success");
  @MessageMeta("<negative>An unknown error occurred while deleting a visualizer. Please check the console for more information.")
  public static final Message CMD_VIS_DELETE_ERROR =
      new Message("commands.path_visualizer.delete.error");
  @MessageMeta(value = "<ins:prefix><gray>Changed name of <old-value> to <value>.", placeholders = {
      "key", "name", "type", "value", "old-value"})
  public static final Message CMD_VIS_SET_NAME = new Message("commands.path_visualizer.set.name");
  @MessageMeta(value = "<ins:prefix><gray>Changed permission of <name> from <old-value> to <value>.", placeholders = {
      "key", "name", "type", "value", "old-value"})
  public static final Message CMD_VIS_SET_PERM = new Message("commands.path_visualizer.set.perm");
  @MessageMeta(value = "<ins:prefix><gray>Changed <property> for <name> from <old-value> to <value>.", placeholders = {
      "key", "name", "type", "property", "value", "old-value"})
  public static final Message CMD_VIS_SET_PROP =
      new Message("commands.path_visualizer.set.interval");

  @MessageMeta("<negative>Could not import file, another visualizer with this key already exists.</negative>")
  public static final Message CMD_VIS_IMPORT_EXISTS =
      new Message("commands.path_visualizer.import.already_exists");
  @MessageMeta("<negative>Could not import file, there is no example file with this name.</negative>")
  public static final Message CMD_VIS_IMPORT_NOT_EXISTS =
      new Message("commands.path_visualizer.import.file_doesnt_exist");
  @MessageMeta(value = "<ins:prefix>Successfully imported Visualizer: <name>", placeholders = {
      "key", "name"})
  public static final Message CMD_VIS_IMPORT_SUCCESS =
      new Message("commands.path_visualizer.import.successful");

  @MessageMeta(value = """
      <offset>Visualizer:</offset> <name> <gray>(<key>)</gray>
      <dark_gray>» </dark_gray><gray>Name: <main><hover:show_text:"Click to change name"><click:suggest_command:"/pathvisualizer edit particle <key> name"><name-format></click></hover></main>
      <dark_gray>» </dark_gray><gray>Permission: <main><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle <key> permission"><permission></click></hover></main>
      <dark_gray>» </dark_gray><gray>Children:<entries:"":"<br><dark_gray>  » </dark_gray>"/>""",
      placeholders = "entries[:<separator>][:<prefix>][:<suffix>]")
  public static final Message CMD_VIS_COMBINED_INFO =
      new Message("commands.path_visualizer.type.combined.info");
  @MessageMeta(value = "<ins:prefix>Added <child> as child to <visualizer>.", placeholders = {
      "child", "visualizer"})
  public static final Message CMD_VIS_COMBINED_ADD =
      new Message("commands.path_visualizer.type.combined.add");
  @MessageMeta(value = "<ins:prefix>Removed <child> from children for <visualizer>.")
  public static final Message CMD_VIS_COMBINED_REMOVE =
      new Message("commands.path_visualizer.type.combined.remove");
  @MessageMeta(value = "<ins:prefix>Cleared all children for <visualizer>.")
  public static final Message CMD_VIS_COMBINED_CLEAR =
      new Message("commands.path_visualizer.type.combined.clear");

  @MessageMeta(placeholders = {
      "key", "name", "name-format", "type", "permission", "interval", "point-distance",
      "particle", "particle-steps", "amount", "speed", "offset"
  }, value = """
      <offset>Visualizer:</offset> <name> <gray>(<key>)</gray>
      <dark_gray>» </dark_gray><gray>Name: <main><hover:show_text:"Click to change name"><click:suggest_command:"/pathvisualizer edit particle <key> name"><name-format></click></hover></main>
      <dark_gray>» </dark_gray><gray>Permission: <main><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle <key> permission"><permission></click></hover></main>
      <dark_gray>» </dark_gray><gray>Interval: <main><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit particle <key> interval"><interval></click></hover></main>
      <dark_gray>» </dark_gray><gray>Point-Distance: <main><hover:show_text:"Click to change point-distance"><click:suggest_command:"/pathvisualizer edit particle <key> point-distance"><point-distance></click></hover></main>
      <dark_gray>» </dark_gray><gray>Particle: <main><hover:show_text:"Click to change particle"><click:suggest_command:"/pathvisualizer edit particle <key> particle"><particle></click></hover></main>
      <dark_gray>» </dark_gray><gray>Particle-Steps: <main><hover:show_text:"Click to change particle-steps"><click:suggest_command:"/pathvisualizer edit particle-steps <key> particle"><particle-steps></click></hover></main>
      <dark_gray>» </dark_gray><gray>Amount: <main><hover:show_text:"Click to change amount"><click:suggest_command:"/pathvisualizer edit particle <key> particle"><amount></click></hover></main>
      <dark_gray>» </dark_gray><gray>Speed: <main><hover:show_text:"Click to change speed"><click:suggest_command:"/pathvisualizer edit particle <key> speed"><speed></click></hover></main>
      <dark_gray>» </dark_gray><gray>Offset: <main><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit particle <key> particle"><offset></click></hover></main>""")
  public static final Message CMD_VIS_INFO_PARTICLES =
      new Message("commands.path_visualizer.type.particle_visualizer.info");


  @MessageMeta(placeholders = {"marker-north", "marker-south", "marker-east", "marker-west",
      "marker-target", "background", "color", "overlay"}, value = """
      <offset>Visualizer:</offset> <name> <gray>(<key>)</gray>
      <dark_gray>» </dark_gray><gray>Name: <main><hover:show_text:"Click to change name"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> name"><name-format></click></hover></main>
      <dark_gray>» </dark_gray><gray>Permission: <main><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission"><permission></click></hover></main>
      <dark_gray>» </dark_gray><gray>Interval: <main><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval"><interval></click></hover></main>
      <dark_gray>» </dark_gray><gray>Marker:
          <dark_gray>» </dark_gray><gray>Target: <marker-target>
          <dark_gray>» </dark_gray><gray>North: <marker-north>
          <dark_gray>» </dark_gray><gray>East: <marker-east>
          <dark_gray>» </dark_gray><gray>South: <marker-south>
          <dark_gray>» </dark_gray><gray>West: <marker-west>
      <dark_gray>» </dark_gray><gray>Background: <background>
      <dark_gray>» </dark_gray><gray>Color: <color>
      <dark_gray>» </dark_gray><gray>Overlay: <overlay>""")
  public static final Message CMD_VIS_COMPASS_INFO =
      new Message("commands.path_visualizer.type.compass.info");

  @MessageMeta(placeholders = {
      "key", "name", "name-format", "type", "permission", "interval", "point-distance",
      "particle", "particle-steps", "amount", "speed", "offset-x", "offset-y", "offset-z",
      "path-x", "path-y", "path-z"
  }, value = """
      <offset>Visualizer:</offset> <name> <gray>(<key>)</gray>
      <dark_gray>» </dark_gray><gray>Name: <main><hover:show_text:"Click to change name"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> name"><name-format></click></hover></main>
      <dark_gray>» </dark_gray><gray>Permission: <main><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission"><permission></click></hover></main>
      <dark_gray>» </dark_gray><gray>Interval: <main><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval"><interval></click></hover></main>
      <dark_gray>» </dark_gray><gray>Point-Distance: <main><hover:show_text:"Click to change point-distance"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> point-distance"><point-distance></click></hover></main>
      <dark_gray>» </dark_gray><gray>Particle: <main><hover:show_text:"Click to change particle"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle"><particle></click></hover></main>
      <dark_gray>» </dark_gray><gray>Particle-Data: <main><hover:show_text:"Click to change particle-Data"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle-data"><particle-data></click></hover></main>
      <dark_gray>» </dark_gray><gray>Amount: <main><hover:show_text:"Click to change amount"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle"><amount></click></hover></main>
      <dark_gray>» </dark_gray><gray>Speed: <main><hover:show_text:"Click to change speed"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle"><speed></click></hover></main>
      <dark_gray>» </dark_gray><gray>Offset:
          <dark_gray>» </dark_gray><gray>X: <main><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-x"><offset-x></click></hover></main>
          <dark_gray>» </dark_gray><gray>Y: <main><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-y"><offset-y></click></hover></main>
          <dark_gray>» </dark_gray><gray>Z: <main><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-z"><offset-z></click></hover></main>
      <dark_gray>» </dark_gray><gray>Path Offset (e.g. to make Spirals):
          <dark_gray>» </dark_gray><gray>X: <main><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-x"><path-x></click></hover></main>
          <dark_gray>» </dark_gray><gray>Y: <main><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-y"><path-y></click></hover></main>
          <dark_gray>» </dark_gray><gray>Z: <main><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-z"><path-z></click></hover></main>""")
  public static final Message CMD_ADV_VIS_INFO_PARTICLES =
      new Message("commands.path_visualizer.type.advanced_particle_visualizer.info");

  @MessageMeta(placeholders = {
      "key", "name", "name-format", "type", "permission", "interval",
      "format-north",
      "format-north-east",
      "format-east",
      "format-south-east",
      "format-south",
      "format-south-west",
      "format-west",
      "format-north-west",
      "format-distance"
  }, value = """
      <offset>Visualizer:</offset> <name> <gray>(<key>)</gray>
      <dark_gray>» </dark_gray><gray>Name: <main><hover:show_text:"Click to change name"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> name"><name-format></click></hover></main>
      <dark_gray>» </dark_gray><gray>Permission: <main><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission"><permission></click></hover></main>
      <dark_gray>» </dark_gray><gray>Interval: <main><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval"><interval></click></hover></main>
      <dark_gray>» </dark_gray><gray>Placeholder:</gray>
          <dark_gray>» </dark_gray><gray>North: <main><hover:show_text:"Click to change format-north"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-north"><format-north></click></hover></main>
          <dark_gray>» </dark_gray><gray>North-East: <main><hover:show_text:"Click to change format-northeast"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-northeast"><format-northeast></click></hover></main>
          <dark_gray>» </dark_gray><gray>East: <main><hover:show_text:"Click to change format-east"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-east"><format-east></click></hover></main>
          <dark_gray>» </dark_gray><gray>South-East: <main><hover:show_text:"Click to change format-southeast"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-southeast"><format-southeast></click></hover></main>
          <dark_gray>» </dark_gray><gray>South: <main><hover:show_text:"Click to change format-south"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-south"><format-south></click></hover></main>
          <dark_gray>» </dark_gray><gray>South-West: <main><hover:show_text:"Click to change format-southwest"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-southwest"><format-southwest></click></hover></main>
          <dark_gray>» </dark_gray><gray>West: <main><hover:show_text:"Click to change format-west"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-west"><format-west></click></hover></main>
          <dark_gray>» </dark_gray><gray>North-West: <main><hover:show_text:"Click to change format-northwest"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-northwest"><format-northwest></click></hover></main>
          <dark_gray>» </dark_gray><gray>Distance: <main><hover:show_text:"Click to change format-distance"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-distance"><format-distance></click></hover></main>""")
  public static final Message CMD_VIS_PAPI_INFO =
      new Message("commands.path_visualizer.type.placeholder_api.info");

  @MessageMeta(value = "<list>", placeholders = "list")
  public static final Message E_NODE_NAME = new Message("editor.node_name");
  @MessageMeta("<white><u>Node Tool</u></white>")
  public static final Message E_NODE_TOOL_N = new Message("editor.toolbar.node_tool.name");
  @MessageMeta("""
      <gray>» <yellow>right-click:</yellow> Create node</gray>
      <gray>» <yellow>left-click:</yellow> Delete clicked node</gray>""")
  public static final Message E_NODE_TOOL_L = new Message("editor.toolbar.node_tool.lore");
  @MessageMeta("<white><u>Edge Tool</u></white>")
  public static final Message E_EDGE_TOOL_N = new Message("editor.toolbar.edge_tool.name");
  @MessageMeta("""
      <gray>» <yellow>right-click node:</yellow> Connect nodes</gray>
      <gray>» <yellow>left-click node:</yellow> Disconnect all edges</gray>
      <gray>» <yellow>left-click edge:</yellow> Dissolve edge</gray>
      <gray>» <yellow>left-click air:</yellow> Toggle directed</gray>""")
  public static final Message E_EDGE_TOOL_L = new Message("editor.toolbar.edge_tool.lore");
  @MessageMeta("<ins:prefix>Node connection mode cancelled")
  public static final Message E_EDGE_TOOL_CANCELLED =
      new Message("editor.toolbar.edge_tool.cancelled");
  @MessageMeta(value = "<ins:prefix>Edges directed: <main><value><main>", placeholders = "value")
  public static final Message E_EDGE_TOOL_DIR_TOGGLE =
      new Message("editor.toolbar.edge_tool.directed");
  @MessageMeta("<white><u>Assign Group</u></white>")
  public static final Message E_GROUP_TOOL_N = new Message("editor.toolbar.group_tool.name");
  @MessageMeta("")
  public static final Message E_GROUP_TOOL_L = new Message("editor.toolbar.group_tool.lore");
  @MessageMeta("<white><u>Mutli Group Tool</u></white>")
  public static final Message E_MULTI_GROUP_TOOL_N =
      new Message("editor.toolbar.multi_group_tool.name");
  @MessageMeta("""
      <gray>Assign and remove multiple
      <gray>groups at once.
      			
      <gray>» <yellow>right-click air:</yellow> Open GUI</gray>
      <gray>» <yellow>right-click node:</yellow> Add groups</gray>
      <gray>» <yellow>right-click node:</yellow> Remove groups</gray>""")
  public static final Message E_MULTI_GROUP_TOOL_L =
      new Message("editor.toolbar.multi_group_tool.lore");
  @MessageMeta("<white><u>Teleport Tool</u></white>")
  public static final Message E_TP_TOOL_N = new Message("editor.toolbar.teleport_tool.name");
  @MessageMeta("<gray>Teleports you to the\n<gray>nearest node.")
  public static final Message E_TP_TOOL_L = new Message("editor.toolbar.teleport_tool.lore");
  @MessageMeta("Assign Node Groups")
  public static final Message E_SUB_GROUP_TITLE = new Message("editor.groups.title");
  @MessageMeta("<gold>Info</gold>")
  public static final Message E_SUB_GROUP_INFO_N = new Message("editor.groups.info.name");
  @MessageMeta("<gray>Create a new nodegroup with\n<gray>» <yellow>/nodegroup create <key>")
  public static final Message E_SUB_GROUP_INFO_L = new Message("editor.groups.info.lore");
  @MessageMeta("<negative>Reset Groups</negative>")
  public static final Message E_SUB_GROUP_RESET_N = new Message("editor.groups.reset.name");
  @MessageMeta("<gray>Reset all groups for the\n<gray>selected node.")
  public static final Message E_SUB_GROUP_RESET_L = new Message("editor.groups.reset.lore");
  @MessageMeta(value = "<name>",
      placeholders = {"key", "name", "name-format", "discoverable", "search-terms"})
  public static final Message E_SUB_GROUP_ENTRY_N = new Message("editor.groups.entry.name");
  @MessageMeta(value = """
      <dark_gray>» </dark_gray><gray>Key: <key></gray>
      <dark_gray>» </dark_gray><gray>Name: <name-format></gray>
      <dark_gray>» </dark_gray><gray>Permission: <permission></gray>
      <dark_gray>» </dark_gray><gray>Navigable: <navigable></gray>
      <dark_gray>» </dark_gray><gray>Discoverable: <discoverable></gray>
      <dark_gray>» </dark_gray><gray>Find distance: <find-distance:#.##></gray>
      <dark_gray>» </dark_gray><gray>Search terms: <search-terms></gray>""",
      placeholders = {"key", "name", "name-format", "permission", "navigable", "discoverable",
          "find-distance", "search-terms"})
  public static final Message E_SUB_GROUP_ENTRY_L = new Message("editor.groups.entry.lore");


  @MessageMeta(value = "<ins:prefix>Target reached.")
  public static final Message TARGET_FOUND = new Message("general.target_reached");
  @MessageMeta(value = "<roadmap>: <percent>", placeholders = {"roadmap", "percent"})
  public static final Message LOCATION_FOUND_SINGLE_RM_PERCENT_FORMAT =
      new Message("general.target_discovered.percent");
  @MessageMeta(value = "", placeholders = {"name", "roadmaps"})
  public static final Message LOCATION_FOUND_TITLE_1 =
      new Message("general.target_discovered.title");
  @MessageMeta(value = "You found <name>", placeholders = {"name", "roadmaps"})
  public static final Message LOCATION_FOUND_TITLE_2 =
      new Message("general.target_discovered.subtitle");
  @MessageMeta(value = "Discovered: <name>", placeholders = {"name"})
  public static final Message LOCATION_FOUND_AB =
      new Message("general.target_discovered.actionbar");

  public static Message formatBool(boolean val) {
    return val ? GEN_TRUE : GEN_FALSE;
  }

  public static Component formatNodeSelection(CommandSender sender, Collection<Node<?>> nodes) {
    return formatGroupInHover(sender, GEN_NODE_SEL, nodes,
        node -> Component.text("#" + node.getNodeId()));
  }

  public static Component formatNodeGroups(CommandSender sender, Collection<NodeGroup> groups) {
    return formatGroupInHover(sender, GEN_GROUP_SEL, groups, g -> Component.text(g.getKey().toString()));
  }

  public static <T> Component formatGroupConcat(CommandSender sender, Message placeHolder,
                                                Collection<T> collection,
                                                Function<T, ComponentLike> converter) {
    return placeHolder.format(
        Placeholder.unparsed("amount", collection.size() + ""),
        Placeholder.component("list",
            Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
                collection.stream().map(converter).collect(Collectors.toList())))
    ).asComponent(sender);
  }

  public static <T> Component formatGroupInHover(CommandSender sender, Message placeHolder,
                                                 Collection<T> collection,
                                                 Function<T, ComponentLike> converter) {
    return placeHolder.format(Placeholder.unparsed("amount", collection.size() + ""))
        .asComponent(sender)
        .hoverEvent(HoverEvent.showText(
            Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
                collection.stream().map(converter).collect(Collectors.toList()))));
  }

  public static FormattedMessage formatParticle(Particle particle, Object data) {
    return data == null ?
        GEN_PARTICLE.format(TagResolver.builder()
            .resolver(Placeholder.component("particle", Component.text(particle.toString())))
            .build()) :
        GEN_PARTICLE_META.format(TagResolver.builder()
            .resolver(Placeholder.component("particle", Component.text(particle.toString())))
            .resolver(Placeholder.component("meta", Component.text(data.toString())))
            .build());
  }

  public static FormattedMessage formatVector(Vector vector) {
    return GEN_VECTOR.format(TagResolver.builder()
        .resolver(Formatter.number("x", vector.getX()))
        .resolver(Formatter.number("y", vector.getY()))
        .resolver(Formatter.number("z", vector.getZ()))
        .build());
  }

  public static FormattedMessage formatPermission(@Nullable String permission) {
    return permission == null ?
        GEN_NULL.format() :
        GEN_PERMISSION.format(
            TagResolver.resolver("permission", Tag.inserting(Component.text(permission))));
  }

  public static <T> BiFunction<ArgumentQueue, Context, Tag> formatList(Collection<T> entries,
                                                                       Function<T, ComponentLike> formatter) {
    Collection<ComponentLike> componentLikes = new ArrayList<>();
    for (T entry : entries) {
      componentLikes.add(formatter.apply(entry));
    }
    return formatList(componentLikes);
  }

  public static <T extends ComponentLike> BiFunction<ArgumentQueue, Context, Tag> formatList(
      Collection<T> entries) {
    return (queue, context) -> {
      MiniMessage mm = TranslationHandler.getInstance().getMiniMessage();
      ComponentLike separator = Component.text(", ", NamedTextColor.GRAY), prefix = null, suffix =
          null;
      if (queue.hasNext()) {
        separator = mm.deserialize(queue.pop().value());
      }
      if (queue.hasNext()) {
        prefix = mm.deserialize(queue.pop().value());
      }
      if (queue.hasNext()) {
        suffix = mm.deserialize(queue.pop().value());
      }
      ComponentLike finalPrefix = prefix;
      ComponentLike finalSuffix = suffix;
      return Tag.selfClosingInserting(Component.join(JoinConfiguration.builder()
          .separator(separator)
          .build(), entries.stream().map(c -> {
        if (finalPrefix == null) {
          return c;
        }
        if (finalSuffix == null) {
          return Component.empty().append(finalPrefix).append(c);
        }
        return Component.empty().append(finalPrefix).append(c).append(finalSuffix);
      }).collect(Collectors.toList())));
    };
  }

  public static BiFunction<ArgumentQueue, Context, Tag> formatKey(NamespacedKey key) {
    return (queue, context) -> {
      if (key == null) {
        return Tag.selfClosingInserting(GEN_NULL.format());
      }

      TextColor namespaceColor, keyColor;

      Component namespaceString = Component.text(key.getNamespace());
      Component keyString = Component.text(key.getKey());

      if (queue.hasNext()) {
        namespaceColor = TextColor.fromCSSHexString(queue.pop().value());
        if (namespaceColor != null) {
          namespaceString = namespaceString.color(namespaceColor);
          keyString = keyString.color(namespaceColor);
        }
        if (queue.hasNext()) {
          keyColor = TextColor.fromCSSHexString(queue.pop().value());
          if (keyColor != null) {
            keyString = keyString.color(keyColor);
          }
        }
      }
      return Tag.selfClosingInserting(Component.empty()
          .append(namespaceString)
          .append(Component.text(":"))
          .append(keyString));
    };
  }
}
