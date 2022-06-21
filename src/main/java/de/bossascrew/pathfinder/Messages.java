package de.bossascrew.pathfinder;

import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageFile;
import de.cubbossa.translations.MessageMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

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
	@MessageMeta(value = "<#6569eb>null</#6569eb>")
	public static final Message GEN_NULL = new Message("general.null");
	@MessageMeta("<green>Accept</green>")
	public static final Message GEN_GUI_ACCEPT_N = new Message("general.gui.accept.name");
	public static final Message GEN_GUI_ACCEPT_L = new Message("general.gui.accept.lore");
	@MessageMeta("<yellow>Warning")
	public static final Message GEN_GUI_WARNING_N = new Message("general.gui.warning.name");
	public static final Message GEN_GUI_WARNING_L = new Message("general.gui.warning.lore");

	public static final Message ERROR_PARSE_STRING = new Message("error.parse.string");
	public static final Message ERROR_PARSE_INTEGER = new Message("error.parse.integer");
	public static final Message ERROR_PARSE_DOUBLE = new Message("error.parse.double");
	public static final Message ERROR_PARSE_PERCENT = new Message("error.parse.percent");
	public static final Message ERROR_PARSE_KEY = new Message("error.parse.namespaced_key");

	@MessageMeta(placeholders = "error")
	public static final Message RELOAD_ERROR = new Message("command.reload.error");
	@MessageMeta(value = "<ins:prefix>Successfully reloaded in <#8f65eb><ms></#8f65eb><#7b42f5>ms</#7b42f5>.", placeholders = "ms")
	public static final Message RELOAD_SUCCESS = new Message("command.reload.success");

	@MessageMeta(value = """
			<#7b42f5>Roadmap:</7b42f5> <name> <gray>(<id>)</gray>
			<dark_gray>» </dark_gray><gray>Name: <#6569eb><hover:show_text:"Click to change name"><click:suggest_command:/roadmap rename <id> [new name]><name-format></click></hover></#6569eb>
			<dark_gray>» </dark_gray><gray>World: <#6569eb><hover:show_text:"Click to change world"><click:suggest_command:/roadmap setworld <id> [new world]><world></click></hover>
			<dark_gray>» </dark_gray><gray>Findable: <#6569eb><hover:show_text:"Click to change findable state"><click:suggest_command:/roadmap set findable <id> true|false><findable></click></hover>
			<dark_gray>» </dark_gray><gray>Find Distance: <#6569eb><hover:show_text:"Click to change find distance"><click:suggest_command:/roadmap set find-distance <id> [distance]><find-distance></click></hover>
			<#8265eb>Particles:</#8265eb>
			<dark_gray>  » </dark_gray><gray>Curve length: <#6569eb><hover:show_text:"Click to change curve length"><click:suggest_command:/roadmap set curve-length <id> [curve-length]><curve-length></click></hover>
			<dark_gray>  » </dark_gray><gray>Particles: <#6569eb><hover:show_text:"Click to change path-visualizer"><click:suggest_command:/roadmap set path-visualizer <id> [path-visualizer]><path-visualizer></click></hover>
			""", placeholders = {"name", "id", "name-format", "world", "findable", "find-distance", "curve-length", "path-visualizer"})
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
			placeholders = {"id", "name", "world", "findable", "find-distance", "curve-length", "path-visualizer"})
	public static final Message CMD_RM_LIST_ENTRY = new Message("commands.roadmap.list.entry");
	@MessageMeta(value = "<gray>» </gray><name> <gray>(<id>)</gray> <white><i>Selected</i></white>",
			placeholders = {"id", "name", "world", "findable", "find-distance", "curve-length", "path-visualizer"})
	public static final Message CMD_RM_LIST_SELECTED = new Message("commands.roadmap.list.entry_selected");
	@MessageMeta(value = "<gradient:black:dark_gray:black>------------<gray> <click:run_command:/roadmap list <prev-page>>←</click> <page>/<pages> <click:run_command:/roadmap list <next-page>>→</click> </gray>-------------</gradient>",
			placeholders = {"page", "next-page", "prev-page"})
	public static final Message CMD_RM_LIST_FOOTER = new Message("commands.roadmap.list.footer");
	@MessageMeta("<red>No roadmap found. Create a new roadmap with <click:suggest_command:/roadmap create>/roadmap create <pre><key></pre></click>.")
	public static final Message CMD_RM_EM_CREATE = new Message("commands.roadmap.editmode.create_new");
	@MessageMeta(value = "<msg:prefix><gray>Nearest <#8265eb>roadmap</#8265eb> <roadmap> selected.</gray>", placeholders = "roadmap")
	public static final Message CMD_RM_EM_SELECTED = new Message("commands.roadmap.editmode.selected");
	@MessageMeta(value = "<red>You have to select a <#8265eb>roadmap</#8265eb> first.")
	public static final Message CMD_RM_EM_SELECT = new Message("commands.roadmap.editmode.select");
	@MessageMeta(value = "<ins:prefix>Editmode activated for <#8265eb><roadmap></#8265eb>.", placeholders = {"roadmap"})
	public static final Message CMD_RM_EM_ACTIVATED = new Message("commands.roadmap.editmode.activated");
	@MessageMeta(value = "<ins:prefix>Player <name> found <selection>.", placeholders = {"name", "selection"})
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
	@MessageMeta(value = "<msg:prefix><gray>Successfully set findable = '<#8f65eb><value></#8f65eb>' for <#8265eb><roadmap></#8265eb>.</gray>",
			placeholders = {"roadmap", "value"})
	public static final Message CMD_RM_SET_FINDABLE = new Message("commands.roadmap.set_findable");

	@MessageMeta(value = "<ins:prefix>Successfully created Node <name>.", placeholders = "name")
	public static final Message CMD_N_CREATE = new Message("commands.node.create");
	@MessageMeta(placeholders = "selection")
	public static final Message CMD_N_DELETE = new Message("commands.node.delete");
	@MessageMeta(placeholders = {"selection", "location"})
	public static final Message CMD_N_MOVED = new Message("commands.node.moved");
	@MessageMeta(placeholders = {"selection", "name"})
	public static final Message CMD_N_RENAMED = new Message("commands.node.renamed");
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

	@MessageMeta(placeholders = "name")
	public static final Message CMD_NG_ALREADY_EXISTS = new Message("commands.node_group.already_exists");
	@MessageMeta(placeholders = "name")
	public static final Message CMD_NG_CREATE = new Message("commands.node_group.create");
	@MessageMeta(placeholders = "name")
	public static final Message CMD_NG_DELETE = new Message("commands.node_group.delete");
	@MessageMeta(placeholders = {"roadmap", "page"})
	public static final Message CMD_NG_LIST_HEADER = new Message("commands.node_group.list.header");
	@MessageMeta(placeholders = {"roadmap", "page", "id", "name", "size", "findable"})
	public static final Message CMD_NG_LIST_LINE = new Message("commands.node_group.list.line");
	@MessageMeta(placeholders = {"roadmap", "page"})
	public static final Message CMD_NG_LIST_FOOTER = new Message("commands.node_group.list.footer");
	@MessageMeta(placeholders = {"name", "value"})
	public static final Message CMD_NG_SET_NAME = new Message("commands.node_group.set_name");
	@MessageMeta(placeholders = {"name", "value"})
	public static final Message CMD_NG_SET_FINDABLE = new Message("commands.node_group.set_findable");
	@MessageMeta(value = "<ins:prefix>Search terms for <name>:\n<ins:colors.background>»<values>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_LIST = new Message("commands.node_group.terms.list");
	@MessageMeta(value = "<ins:prefix>Successfully added search terms to <name>: <values>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_ADD = new Message("commands.node_group.terms.add");
	@MessageMeta(value = "<ins:prefix>Successfully removed search terms from <name>: <values>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_REMOVE = new Message("commands.node_group.terms.remove");

	public static final Message CMD_CANCEL = new Message("commands.cancel_path");

	@MessageMeta("<white><u>Node Tool</u></white>")
	public static final Message E_NODE_TOOL_N = new Message("editor.toolbar.node_tool.name");
	public static final Message E_NODE_TOOL_L = new Message("editor.toolbar.node_tool.lore");
	@MessageMeta("<white><u>Edge Tool</u></white>")
	public static final Message E_EDGE_TOOL_N = new Message("editor.toolbar.edge_tool.name");
	public static final Message E_EDGE_TOOL_L = new Message("editor.toolbar.edge_tool.lore");
	public static final Message E_EDGE_TOOL_CANCELLED = new Message("editor.toolbar.edge_tool.cancelled");
	@MessageMeta(value = "<msg:prefix><gray>Edges directed: <#6569eb><value><#6569eb>", placeholders = "value")
	public static final Message E_EDGE_TOOL_DIR_TOGGLE = new Message("editor.toolbar.edge_tool.directed");
	@MessageMeta("<white><u>Assign Group</u></white>")
	public static final Message E_GROUP_TOOL_N = new Message("editor.toolbar.group_tool.name");
	public static final Message E_GROUP_TOOL_L = new Message("editor.toolbar.group_tool.lore");
	@MessageMeta("<white><u>Assign Last Group</u></white>")
	public static final Message E_LAST_GROUP_TOOL_N = new Message("editor.toolbar.last_group_tool.name");
	public static final Message E_LAST_GROUP_TOOL_L = new Message("editor.toolbar.last_group_tool.lore");
	@MessageMeta("<white><u>Curve Tool</u></white>")
	public static final Message E_CURVE_TOOL_N = new Message("editor.toolbar.curve_tool.name");
	public static final Message E_CURVE_TOOL_L = new Message("editor.toolbar.curve_tool.lore");
	@MessageMeta("<white><u>Permission Tool</u></white>")
	public static final Message E_PERM_TOOL_N = new Message("editor.toolbar.permission_tool.name");
	public static final Message E_PERM_TOOL_L = new Message("editor.toolbar.permission_tool.lore");
	@MessageMeta("<white><u>Teleport Tool</u></white>")
	public static final Message E_TP_TOOL_N = new Message("editor.toolbar.teleport_tool.name");
	public static final Message E_TP_TOOL_L = new Message("editor.toolbar.teleport_tool.lore");
	public static final Message E_SUB_GROUP_NEW_N = new Message("editor.groups.new.name");
	public static final Message E_SUB_GROUP_NEW_L = new Message("editor.groups.new.lore");
	public static final Message E_SUB_GROUP_RESET_N = new Message("editor.groups.reset.name");
	public static final Message E_SUB_GROUP_RESET_L = new Message("editor.groups.reset.lore");
	public static final Message E_SUB_GROUP_ENTRY_N = new Message("editor.groups.entry.name");
	public static final Message E_SUB_GROUP_ENTRY_L = new Message("editor.groups.entry.lore");


	public static Message formatBool(boolean val) {
		return val ? GEN_TRUE : GEN_FALSE;
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
