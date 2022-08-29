package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageFile;
import de.cubbossa.translations.MessageMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

@MessageFile
public class Messages {

	// #6569eb = default blue
	// #8f65eb = default purple
	// #8265eb = roadmap


	@MessageMeta("<#7b42f5>PathFinder</#7b42f5> <dark_gray>»</dark_gray> <gray>")
	public static final Message PREFIX = new Message("prefix");
	@MessageMeta("<#8f65eb>true</#8f65eb>")
	public static final Message GEN_TRUE = new Message("general.true");
	@MessageMeta("<#8f65eb>false</#8f65eb>")
	public static final Message GEN_FALSE = new Message("general.false");
	@MessageMeta(value = "<#8f65eb><x><gray>,</gray> <y><gray>,</gray> <z></#8f65eb>", placeholders = {"x", "y", "z"})
	public static final Message GEN_VECTOR = new Message("general.vector");
	@MessageMeta(value = "<#8f65eb><namespace><gray>:</gray><key></#8f65eb>", placeholders = {"namespace", "key"})
	public static final Message GEN_KEY = new Message("general.key");
	@MessageMeta(value = "<#6569eb><permission></#6569eb>", placeholders = "permission")
	public static final Message GEN_PERMISSION = new Message("general.permission");
	@MessageMeta(value = "<#6569eb><particle><#/6569eb>", placeholders = {"particle", "meta"})
	public static final Message GEN_PARTICLE = new Message("general.particle");
	@MessageMeta(value = "<#6569eb><particle> <gray>(<meta>)</gray><#/6569eb>", placeholders = {"particle", "meta"})
	public static final Message GEN_PARTICLE_META = new Message("general.particle");

	@MessageMeta(value = "<#6569eb>null</#6569eb>")
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

	public static final Message ERROR_PARSE_STRING = new Message("error.parse.string");
	public static final Message ERROR_PARSE_INTEGER = new Message("error.parse.integer");
	public static final Message ERROR_PARSE_DOUBLE = new Message("error.parse.double");
	public static final Message ERROR_PARSE_PERCENT = new Message("error.parse.percent");
	public static final Message ERROR_PARSE_KEY = new Message("error.parse.namespaced_key");

	@MessageMeta(placeholders = "error", value = "<red>An error occurred while reloading: <error></red>")
	public static final Message RELOAD_ERROR = new Message("command.reload.error");
	@MessageMeta(value = "<ins:prefix>Successfully reloaded in <#8f65eb><ms></#8f65eb><#7b42f5>ms</#7b42f5>.", placeholders = "ms")
	public static final Message RELOAD_SUCCESS = new Message("command.reload.success.general");
	@MessageMeta(value = "<ins:prefix>Successfully reloaded language in <#8f65eb><ms></#8f65eb><#7b42f5>ms</#7b42f5>.", placeholders = "ms")
	public static final Message RELOAD_SUCCESS_LANG = new Message("command.reload.success.language");
	@MessageMeta(value = "<ins:prefix>Successfully reloaded effects in <#8f65eb><ms></#8f65eb><#7b42f5>ms</#7b42f5>.", placeholders = "ms")
	public static final Message RELOAD_SUCCESS_FX = new Message("command.reload.success.effects");
	@MessageMeta(value = "<ins:prefix>Successfully reloaded config files in <#8f65eb><ms></#8f65eb><#7b42f5>ms</#7b42f5>.", placeholders = "ms")
	public static final Message RELOAD_SUCCESS_CFG = new Message("command.reload.success.config");

	@MessageMeta(value = """
			<#7b42f5>Roadmap:</#7b42f5> <name> <gray>(<id>)</gray>
			<dark_gray>» </dark_gray><gray>Name: <#6569eb><hover:show_text:"Click to change name"><click:suggest_command:/roadmap rename <id> [new name]><name-format></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>World: <#6569eb><hover:show_text:"Click to change world"><click:suggest_command:/roadmap setworld <id> [new world]><world></click></hover>
			<dark_gray>» </dark_gray><gray>Discoverable: <#6569eb><hover:show_text:"Click to change discoverable state"><click:suggest_command:/roadmap set discoverable <id> true|false><discoverable></click></hover>
			<dark_gray>» </dark_gray><gray>Discover Distance: <#6569eb><hover:show_text:"Click to change find distance"><click:suggest_command:/roadmap set find-distance <id> [distance]><find-distance></click></hover>
			<dark_gray>» </dark_gray><gray>Nodes: <nodes></gray>
			<dark_gray>» </dark_gray><gray>Groups: <groups></gray>
			<dark_gray>» </dark_gray><#8265eb>Visualizer:</#8265eb>
			<dark_gray>  » </dark_gray><gray>Path Visualizer: <#6569eb><hover:show_text:"Click to change path-visualizer"><click:suggest_command:/roadmap set path-visualizer <id> [path-visualizer]><path-visualizer></click></hover>
			<dark_gray>  » </dark_gray><gray>Default Curve length: <#6569eb><hover:show_text:"Click to change curve length"><click:suggest_command:/roadmap set curve-length <id> [curve-length]><curve-length></click></hover>
			""", placeholders = {"name", "id", "name-format", "world", "discoverable", "find-distance", "curve-length", "path-visualizer", "nodes", "groups"})
	public static final Message CMD_RM_INFO = new Message("commands.roadmap.info");
	@MessageMeta("<msg:prefix><#8265eb>Roadmap</#8265eb> <gray>selected: <name>")
	public static final Message CMD_RM_SELECT = new Message("commands.roadmap.select");
	@MessageMeta("<msg:prefix><#8265eb>Roadmap</#8265eb> <gray>deselected.")
	public static final Message CMD_RM_DESELECT = new Message("commands.roadmap.deselect");
	@MessageMeta("<red>Could not create Roadmap. Check out console for details.")
	public static final Message CMD_RM_CREATE_FAIL = new Message("commands.roadmap.create.fail");
	@MessageMeta(value = "<ins:prefix><gray>Successfully created Roadmap <#8265eb><name></#8265eb>.</gray>", placeholders = "name")
	public static final Message CMD_RM_CREATE_SUCCESS = new Message("commands.roadmap.create.success");
	@MessageMeta(value = "<ins:prefix><gray>Successfully deleted Roadmap <#8265eb><roadmap></#8265eb>.</gray>", placeholders = "roadmap")
	public static final Message CMD_RM_DELETE = new Message("commands.roadmap.delete");
	@MessageMeta(value = "<gradient:black:dark_gray:black>------------ <#8265eb>Roadmaps</#8265eb> ------------</gradient>",
			placeholders = {"page", "next-page", "prev-page"})
	public static final Message CMD_RM_LIST_HEADER = new Message("commands.roadmap.list.header");
	@MessageMeta(value = "<dark_gray> » </dark_gray><name> <gray>(<id>)</gray>",
			placeholders = {"id", "name", "world", "discoverable", "find-distance", "curve-length", "path-visualizer"})
	public static final Message CMD_RM_LIST_ENTRY = new Message("commands.roadmap.list.entry");
	@MessageMeta(value = "<gray>» </gray><name> <gray>(<id>)</gray> <white><i>Selected</i></white>",
			placeholders = {"id", "name", "world", "discoverable", "find-distance", "curve-length", "path-visualizer"})
	public static final Message CMD_RM_LIST_SELECTED = new Message("commands.roadmap.list.entry_selected");
	@MessageMeta(value = "<gradient:black:dark_gray:black>------------<gray> <click:run_command:/roadmap list <prev-page>>←</click> <page>/<pages> <click:run_command:/roadmap list <next-page>>→</click> </gray>-------------</gradient>",
			placeholders = {"page", "next-page", "prev-page"})
	public static final Message CMD_RM_LIST_FOOTER = new Message("commands.roadmap.list.footer");
	@MessageMeta("<red>No roadmap discoverable. Create a new roadmap with <click:suggest_command:/roadmap create>/roadmap create <pre><key></pre></click>.")
	public static final Message CMD_RM_EM_CREATE = new Message("commands.roadmap.editmode.create_new");
	@MessageMeta(value = "<msg:prefix><gray>Nearest <#8265eb>roadmap</#8265eb> <roadmap> selected.</gray>", placeholders = "roadmap")
	public static final Message CMD_RM_EM_SELECTED = new Message("commands.roadmap.editmode.selected");
	@MessageMeta(value = "<red>You have to select a <#8265eb>roadmap</#8265eb> first.")
	public static final Message CMD_RM_EM_SELECT = new Message("commands.roadmap.editmode.select");
	@MessageMeta(value = "<ins:prefix>Editmode activated for <#8265eb><roadmap></#8265eb>.", placeholders = {"roadmap"})
	public static final Message CMD_RM_EM_ACTIVATED = new Message("commands.roadmap.editmode.activated");
	@MessageMeta(value = "<ins:prefix>Player <name> discoverable <selection>.", placeholders = {"name", "selection"})
	public static final Message CMD_RM_FORCE_FIND = new Message("commands.roadmap.force_find");
	@MessageMeta(value = "<msg:prefix>Player <name> forgot about <selection>.", placeholders = {"name", "selection"})
	public static final Message CMD_RM_FORCE_FORGET = new Message("commands.roadmap.force_forget");
	@MessageMeta(value = "<red>This roadmap is currently being edited. Try again later.")
	public static final Message CMD_RM_CURRENTLY_EDITED = new Message("commands.roadmap.currently_edited");
	@MessageMeta(value = "<msg:prefix><gray>Successfully set world for <#8265eb><roadmap></#8265eb> to <#8f65eb><world></#8f65eb></gray>.",
			placeholders = {"roadmap", "world"})
	public static final Message CMD_RM_SET_WORLD = new Message("commands.roadmap.set_world");
	@MessageMeta(value = "<msg:prefix><gray>Successfully set name for <#8265eb><roadmap></#8265eb> to <display-name>. (<pre><name-format></pre>)</gray>",
			placeholders = {"roadmap", "name-format", "display-name"})
	public static final Message CMD_RM_SET_NAME = new Message("commands.roadmap.set_name");
	@MessageMeta(value = "<msg:prefix><gray>Successfully set curve length for <#8265eb><roadmap></#8265eb> to <#8f65eb><value></#8f65eb>",
			placeholders = {"roadmap", "value"})
	public static final Message CMD_RM_SET_CURVED = new Message("commands.roadmap.set_curved");
	@MessageMeta(value = "<msg:prefix><gray>Successfully set visualizer for <#8265eb><roadmap></#8265eb> to <#8f65eb><visualizer></#8f65eb>.</gray>",
			placeholders = {"roadmap", "visualizer"})
	public static final Message CMD_RM_SET_VISUALIZER = new Message("commands.roadmap.set_visualizer");
	@MessageMeta(value = "<msg:prefix><gray>Successfully set find distance for <#8265eb><roadmap></#8265eb> to <#8f65eb><value></#8f65eb>.</gray>",
			placeholders = {"roadmap", "value"})
	public static final Message CMD_RM_SET_FIND_DIST = new Message("commands.roadmap.set_find_distance.success");
	@MessageMeta(value = "<red>Find distance '<value>' is too small. Try again.",
			placeholders = {"roadmap", "value"})
	public static final Message CMD_RM_SET_FIND_DIST_TOO_SMALL = new Message("commands.roadmap.set_find_distance.too_small");
	@MessageMeta(value = "<msg:prefix><gray>Successfully set discoverable = '<#8f65eb><value></#8f65eb>' for <#8265eb><roadmap></#8265eb>.</gray>",
			placeholders = {"roadmap", "value"})
	public static final Message CMD_RM_SET_FINDABLE = new Message("commands.roadmap.set_findable");

	@MessageMeta(value = "<ins:prefix>Successfully created Node #<id>.", placeholders = "id")
	public static final Message CMD_N_CREATE = new Message("commands.node.create");
	@MessageMeta(value = "<ins:prefix>Successfully deleted <selection>.",
			placeholders = "selection")
	public static final Message CMD_N_DELETE = new Message("commands.node.delete");
	@MessageMeta(value = "<ins:prefix><gray>Moved <selection> to <location>.</gray>",
			placeholders = {"selection", "location"})
	public static final Message CMD_N_MOVED = new Message("commands.node.moved");
	@MessageMeta(value = """
			<#7b42f5>Node #<id></7b42f5> <gray>(<roadmap>)</gray>
			<dark_gray>» </dark_gray><gray>Permission: <#6569eb><permission></#6569eb>
			<dark_gray>» </dark_gray><gray>Groups: <#6569eb><groups></#6569eb>
			<dark_gray>» </dark_gray><gray>Position: <#6569eb><position></#6569eb>
			<dark_gray>» </dark_gray><gray>Curve-Length: <#6569eb><curve-length></#6569eb>
			<dark_gray>» </dark_gray><gray>Edge-Count: <#6569eb><edge-count></#6569eb>
			""", placeholders = {"id", "roadmap", "permission", "groups", "position", "curve-length", "edge-count"})
	public static final Message CMD_N_INFO = new Message("commands.node.info");
	@MessageMeta(placeholders = {"selection", "permission"})
	public static final Message CMD_N_SET_PERMISSION = new Message("commands.node.set_permission");
	@MessageMeta(placeholders = {"selection", "length"})
	public static final Message CMD_N_SET_TANGENT = new Message("commands.node.set_curve_length");
	public static final Message CMD_N_SET_GROUP = new Message("commands.node.set_group.success");
	public static final Message CMD_N_SET_GROUP_UNKNOWN = new Message("commands.node.set_group.unknown_group");
	@MessageMeta(placeholders = {"roadmap", "page"})
	public static final Message CMD_N_LIST_HEADER = new Message("commands.node.list.header");
	@MessageMeta(placeholders = {"roadmap", "page", "name", "permission", "position", "group-key"})
	public static final Message CMD_N_LIST_ELEMENT = new Message("commands.node.list.element");
	@MessageMeta(placeholders = {"roadmap", "page"})
	public static final Message CMD_N_LIST_FOOTER = new Message("commands.node.list.footer");
	@MessageMeta(placeholders = {"start", "end"})
	public static final Message CMD_N_CONNECT = new Message("commands.node.connect.success");
	@MessageMeta(placeholders = {"start", "end"})
	public static final Message CMD_N_CONNECT_IDENTICAL = new Message("commands.node.connect.identical");
	@MessageMeta(placeholders = {"start", "end"})
	public static final Message CMD_N_CONNECT_ALREADY_CONNECTED = new Message("commands.node.connect.already_connected");
	@MessageMeta(placeholders = {"start", "end"})
	public static final Message CMD_N_DISCONNECT = new Message("commands.node.disconnect.success");

	@MessageMeta(value = "<red>A node group with this namespaced key (<name>) already exists.</red>",
			placeholders = "name")
	public static final Message CMD_NG_ALREADY_EXISTS = new Message("commands.node_group.already_exists");
	@MessageMeta(value = "<ins:prefix><gray>Node group created: <name>.</gray>",
			placeholders = "name")
	public static final Message CMD_NG_CREATE = new Message("commands.node_group.create");
	@MessageMeta(value = "<ins:prefix><gray>Node group deleted: <name>.</gray>",
			placeholders = "name")
	public static final Message CMD_NG_DELETE = new Message("commands.node_group.delete");
	@MessageMeta(value = "<gradient:black:dark_gray:black>------------ <#8265eb>Node-Groups</#8265eb> ------------</gradient>",
			placeholders = {"roadmap", "page", "next-page", "prev-page"})
	public static final Message CMD_NG_LIST_HEADER = new Message("commands.node_group.list.header");
	@MessageMeta(value = "<dark_gray> » </dark_gray><name> <gray>(<id>)</gray>",
			placeholders = {"roadmap", "page", "id", "name", "size", "discoverable"})
	public static final Message CMD_NG_LIST_LINE = new Message("commands.node_group.list.line");
	@MessageMeta(value = "<gradient:black:dark_gray:black>------------<gray> <click:run_command:/roadmap list <prev-page>>←</click> <page>/<pages> <click:run_command:/roadmap list <next-page>>→</click> </gray>-------------</gradient>",
			placeholders = {"roadmap", "page", "next-page", "prev-page"})
	public static final Message CMD_NG_LIST_FOOTER = new Message("commands.node_group.list.footer");
	@MessageMeta(value = "<ins:prefix>Displayname for <id> set from <name> to <new-name> (<value>).",
			placeholders = {"id", "name", "new-name", "value"})
	public static final Message CMD_NG_SET_NAME = new Message("commands.node_group.set_name");
	@MessageMeta(value = "<ins:prefix><gray>Group <name> discoverable set to: <value>.</gray>",
			placeholders = {"name", "value"})
	public static final Message CMD_NG_SET_FINDABLE = new Message("commands.node_group.set_findable");
	@MessageMeta(value = "<ins:prefix>Search terms for <name>:\n<dark_gray>» <#8f65eb><values></#8f65eb></dark_gray>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_LIST = new Message("commands.node_group.terms.list");
	@MessageMeta(value = "<ins:prefix>Successfully added search terms to <name>: <#8f65eb><values></#8f65eb>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_ADD = new Message("commands.node_group.terms.add");
	@MessageMeta(value = "<ins:prefix>Successfully removed search terms from <name>: <#8f65eb><values></#8f65eb>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_REMOVE = new Message("commands.node_group.terms.remove");
	@MessageMeta(value = "<ins:prefix>Navigation started.  [ <aqua><click:run_command:/cancelpath>CANCEL</click></aqua> ]")
	public static final Message CMD_FIND = new Message("commands.find");
	@MessageMeta(value = "<ins:prefix>Navigation cancelled.")
	public static final Message CMD_CANCEL = new Message("commands.cancel_path");


	@MessageMeta(value = "<gradient:black:dark_gray:black>------------ <#8265eb>Visualizer</#8265eb> ------------</gradient>",
			placeholders = {"page", "next-page", "prev-page"})
	public static final Message CMD_VIS_LIST_HEADER = new Message("commands.path_visualizer.list.header");
	@MessageMeta(value = "<dark_gray> » </dark_gray><name> <gray>(<key>)</gray>",
			placeholders = {"key", "name", "world", "discoverable", "find-distance", "curve-length", "path-visualizer"})
	public static final Message CMD_VIS_LIST_ENTRY = new Message("commands.path_visualizer.list.entry");
	@MessageMeta(value = "<gradient:black:dark_gray:black>------------<gray> <click:run_command:/roadmap list <prev-page>>←</click> <page>/<pages> <click:run_command:/roadmap list <next-page>>→</click> </gray>-------------</gradient>",
			placeholders = {"page", "next-page", "prev-page"})
	public static final Message CMD_VIS_LIST_FOOTER = new Message("commands.path_visualizer.list.footer");

	@MessageMeta(value = "<ins:prefix><gray>Successfully created Visualizer <#8265eb><name></#8265eb> of type '<type>'.</gray>", placeholders = {"key", "name", "name-format", "type"})
	public static final Message CMD_VIS_CREATE_SUCCESS = new Message("commands.path_visualizer.create.success");
	@MessageMeta("<red>Another visualizer with this name already exists.")
	public static final Message CMD_VIS_NAME_EXISTS = new Message("commands.path_visualizer.create.already_exists");
	@MessageMeta(value = "<ins:prefix><gray>Successfully deleted Visualizer <#8265eb><name></#8265eb>.</gray>", placeholders = "key, name, nameformat")
	public static final Message CMD_VIS_DELETE_SUCCESS = new Message("commands.path_visualizer.delete.success");
	@MessageMeta("<red>An unknown error occurred while deleting a visualizer. Please check the console for more information.")
	public static final Message CMD_VIS_DELETE_ERROR = new Message("commands.path_visualizer.delete.error");
	@MessageMeta(value = "<ins:prefix><gray>Changed name of <old-value> to <value>.", placeholders = {"key", "name", "type", "value", "old-value"})
	public static final Message CMD_VIS_SET_NAME = new Message("commands.path_visualizer.set.name");
	@MessageMeta(value = "<ins:prefix><gray>Changed permission of <name> from <old-value> to <value>.", placeholders = {"key", "name", "type", "value", "old-value"})
	public static final Message CMD_VIS_SET_PERM = new Message("commands.path_visualizer.set.perm");
	@MessageMeta(value = "<ins:prefix><gray>Changed interval for <name> from <old-value> to <value>.", placeholders = {"key", "name", "type", "value", "old-value"})
	public static final Message CMD_VIS_SET_INTERVAL = new Message("commands.path_visualizer.set.interval");
	@MessageMeta(value = "<ins:prefix><gray>Changed point distance for <name> from <old-value> to <value>.", placeholders = {"key", "name", "type", "value", "old-value"})
	public static final Message CMD_VIS_SET_DIST = new Message("commands.path_visualizer.set.distance");

	@MessageMeta(placeholders = {
			"key", "name", "name-format", "type", "permission", "interval", "point-distance",
			"particle", "particle-steps", "amount", "speed", "offset"
	}, value = """
			<#7b42f5>Visualizer:</#7b42f5> <name> <gray>(<key>)</gray>
			<dark_gray>» </dark_gray><gray>Name: <#6569eb><hover:show_text:"Click to change name"><click:suggest_command:/pathvisualizer edit particle <key> name ><name-format></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>Permission: <#6569eb><hover:show_text:"Click to change permission"><click:suggest_command:/pathvisualizer edit particle <key> permission ><permission></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>Interval: <#6569eb><hover:show_text:"Click to change interval"><click:suggest_command:/pathvisualizer edit particle <key> interval ><interval></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>Point-Distance: <#6569eb><hover:show_text:"Click to change point-distance"><click:suggest_command:/pathvisualizer edit particle <key> point-distance ><point-distance></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>Particle: <#6569eb><hover:show_text:"Click to change particle"><click:suggest_command:/pathvisualizer edit particle <key> particle ><particle></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>Particle-Steps: <#6569eb><hover:show_text:"Click to change particle-steps"><click:suggest_command:/pathvisualizer edit particle-steps <key> particle ><particle-steps></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>Amount: <#6569eb><hover:show_text:"Click to change amount"><click:suggest_command:/pathvisualizer edit particle <key> particle ><amount></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>Speed: <#6569eb><hover:show_text:"Click to change speed"><click:suggest_command:/pathvisualizer edit particle <key> particle ><speed></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>Offset: <#6569eb><hover:show_text:"Click to change offset"><click:suggest_command:/pathvisualizer edit particle <key> particle ><offset></click></hover></#6569eb>""")
	public static final Message CMD_VIS_INFO_PARTICLES = new Message("commands.path_visualizer.info.particle_visualizer");


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
	public static final Message E_EDGE_TOOL_CANCELLED = new Message("editor.toolbar.edge_tool.cancelled");
	@MessageMeta(value = "<msg:prefix><gray>Edges directed: <#6569eb><value><#6569eb>", placeholders = "value")
	public static final Message E_EDGE_TOOL_DIR_TOGGLE = new Message("editor.toolbar.edge_tool.directed");
	@MessageMeta("<white><u>Assign Group</u></white>")
	public static final Message E_GROUP_TOOL_N = new Message("editor.toolbar.group_tool.name");
	@MessageMeta
	public static final Message E_GROUP_TOOL_L = new Message("editor.toolbar.group_tool.lore");
	@MessageMeta("<white><u>Assign Last Group</u></white>")
	public static final Message E_LAST_GROUP_TOOL_N = new Message("editor.toolbar.last_group_tool.name");
	@MessageMeta
	public static final Message E_LAST_GROUP_TOOL_L = new Message("editor.toolbar.last_group_tool.lore");
	@MessageMeta("<white><u>Curve Tool</u></white>")
	public static final Message E_CURVE_TOOL_N = new Message("editor.toolbar.curve_tool.name");
	@MessageMeta("<gray>Sets the curve strength for\n<gray>particle trails that pass\n<gray>this node.")
	public static final Message E_CURVE_TOOL_L = new Message("editor.toolbar.curve_tool.lore");
	@MessageMeta("<white><u>Permission Tool</u></white>")
	public static final Message E_PERM_TOOL_N = new Message("editor.toolbar.permission_tool.name");
	@MessageMeta("<gray>Sets a permission for the\n<gray>clicked Nodes.")
	public static final Message E_PERM_TOOL_L = new Message("editor.toolbar.permission_tool.lore");
	@MessageMeta("<white><u>Teleport Tool</u></white>")
	public static final Message E_TP_TOOL_N = new Message("editor.toolbar.teleport_tool.name");
	@MessageMeta("<gray>Teleports you to the\n<gray>nearest node.")
	public static final Message E_TP_TOOL_L = new Message("editor.toolbar.teleport_tool.lore");
	@MessageMeta("Assign Node Groups")
	public static final Message E_SUB_GROUP_TITLE = new Message("editor.groups.title");
	@MessageMeta("<green>Create New Group</green>")
	public static final Message E_SUB_GROUP_NEW_N = new Message("editor.groups.new.name");
	@MessageMeta("<gray>Create a new nodegroup and\n<gray>assign it to this node.")
	public static final Message E_SUB_GROUP_NEW_L = new Message("editor.groups.new.lore");
	@MessageMeta("<red>Reset Groups</red>")
	public static final Message E_SUB_GROUP_RESET_N = new Message("editor.groups.reset.name");
	@MessageMeta("<gray>Reset all groups for the\n<gray>selected node.")
	public static final Message E_SUB_GROUP_RESET_L = new Message("editor.groups.reset.lore");
	@MessageMeta(value = "<name>",
			placeholders = {"id", "name", "name-format", "discoverable", "search-terms"})
	public static final Message E_SUB_GROUP_ENTRY_N = new Message("editor.groups.entry.name");
	@MessageMeta(value = """
			<dark_gray>» </dark_gray><gray>Name: <name></gray>
			<dark_gray>» </dark_gray><gray>Findable: <discoverable></gray>
			<dark_gray>» </dark_gray><gray>Roadmap: <roadmap></gray>
			<dark_gray>» </dark_gray><gray>Search terms: <search-terms></gray>""",
			placeholders = {"id", "name", "name-format", "discoverable", "search-terms"})
	public static final Message E_SUB_GROUP_ENTRY_L = new Message("editor.groups.entry.lore");


	@MessageMeta(value = "<msg:prefix><gray>Target reached.")
	public static final Message TARGET_FOUND = new Message("general.target_reached");
	@MessageMeta(value = "<roadmap>: <percent>", placeholders = {"roadmap", "percent"})
	public static final Message LOCATION_FOUND_SINGLE_RM_PERCENT_FORMAT = new Message("general.target_discovered.percent");
	@MessageMeta(value = "", placeholders = {"name", "roadmaps"})
	public static final Message LOCATION_FOUND_TITLE_1 = new Message("general.target_discovered.title");
	@MessageMeta(value = "You found <name>", placeholders = {"name", "roadmaps"})
	public static final Message LOCATION_FOUND_TITLE_2 = new Message("general.target_discovered.subtitle");
	@MessageMeta(value = "Discovered: <roadmaps>", placeholders = {"name", "roadmaps"})
	public static final Message LOCATION_FOUND_AB = new Message("general.target_discovered.actionbar");

	public static Message formatBool(boolean val) {
		return val ? GEN_TRUE : GEN_FALSE;
	}

	public static Component formatNodeSelection(CommandSender sender, Collection<Node> nodes) {
		return formatGroupInHover(sender, GEN_NODE_SEL, nodes, node -> Component.text("#" + node.getNodeId()));
	}

	public static Component formatNodeGroups(CommandSender sender, Collection<NodeGroup> groups) {
		return formatGroupInHover(sender, GEN_GROUP_SEL, groups, NodeGroup::getDisplayName);
	}

	public static <T> Component formatGroupConcat(CommandSender sender, Message placeHolder, Collection<T> collection, Function<T, ComponentLike> converter) {
		return placeHolder.format(
				Placeholder.parsed("amount", collection.size() + ""),
				Placeholder.component("list", Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
						collection.stream().map(converter).collect(Collectors.toList())))
		).asComponent(sender);
	}

	public static <T> Component formatGroupInHover(CommandSender sender, Message placeHolder, Collection<T> collection, Function<T, ComponentLike> converter) {
		return placeHolder.format(Placeholder.parsed("amount", collection.size() + "")).asComponent(sender)
				.hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
						collection.stream().map(converter).collect(Collectors.toList()))));
	}

	public static FormattedMessage formatParticle(Particle particle, Object data) {
		return data == null ?
				GEN_PARTICLE.format(TagResolver.builder()
						.tag("particle", Tag.inserting(Component.text(particle.toString())))
						.build()) :
				GEN_PARTICLE_META.format(TagResolver.builder()
						.tag("particle", Tag.inserting(Component.text(particle.toString())))
						.tag("meta", Tag.inserting(Component.text(data.toString())))
						.build());
	}

	public static FormattedMessage formatVector(Vector vector) {
		return GEN_VECTOR.format(TagResolver.builder()
				.tag("x", Tag.preProcessParsed(String.format("%,.2f", vector.getX())))
				.tag("y", Tag.preProcessParsed(String.format("%,.2f", vector.getY())))
				.tag("z", Tag.preProcessParsed(String.format("%,.2f", vector.getZ())))
				.build());
	}

	public static FormattedMessage formatVector(String x, String y, String z) {
		return GEN_VECTOR.format(TagResolver.builder()
				.tag("x", Tag.preProcessParsed(x))
				.tag("y", Tag.preProcessParsed(y))
				.tag("z", Tag.preProcessParsed(z))
				.build());
	}

	public static FormattedMessage formatPermission(@Nullable String permission) {
		return permission == null ?
				GEN_NULL.format() :
				GEN_PERMISSION.format(TagResolver.resolver("permission", Tag.inserting(Component.text(permission))));
	}

	public static FormattedMessage formatKey(@Nullable NamespacedKey key) {
		return key == null ?
				GEN_NULL.format() :
				GEN_KEY.format(TagResolver.builder()
						.tag("namespace", Tag.preProcessParsed(key.getNamespace()))
						.tag("key", Tag.preProcessParsed(key.getKey())).build());
	}
}
