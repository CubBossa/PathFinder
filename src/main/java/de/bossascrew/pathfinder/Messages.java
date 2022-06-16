package de.bossascrew.pathfinder;

import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageMeta;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class Messages {

	public static final Message COL_SEVERE = new Message("colors.severe");
	public static final Message COL_BACKGROUND = new Message("colors.background");
	public static final Message COL_FILL = new Message("colors.default");
	public static final Message COL_BASE_A = new Message("colors.baseA");
	public static final Message COL_BASE_B = new Message("colors.baseB");
	public static final Message COL_ROADMAP = new Message("colors.roadmap");
	public static final Message COL_NODE = new Message("colors.node");
	public static final Message COL_EDGE = new Message("colors.edge");


	public static final Message GEN_TRUE = new Message("general.true");
	public static final Message GEN_FALSE = new Message("general.false");
	@MessageMeta(placeholders = {"x", "y", "z"})
	public static final Message GEN_VECTOR = new Message("general.vector");
	public static final Message GEN_KEY = new Message("general.key");
	public static final Message GEN_PERMISSION = new Message("general.permission");
	public static final Message GEN_NULL = new Message("general.null");


	@MessageMeta(value = """
			Roadmap: <name> (#<id>)
			<col:colors.background>» <col:colors.default>Name: <hover:show_text:"Click to change name"><click:suggest_command:/roadmap rename #<id> <new name>><name></click></hover>
			<col:colors.background>» <col:colors.default>World: <col:colors.baseA><hover:show_text:"Click to change world"><click:suggest_command:/roadmap setworld #<id> <new world>><world></click></hover>
			<col:colors.background>» <col:colors.default>Findable: <col:colors.baseA><hover:show_text:"Click to change findable state"><click:suggest_command:/roadmap set findable #<id> true|false><findable></click></hover>
			<col:colors.background>» <col:colors.default>Find Distance: <col:colors.baseA><hover:show_text:"Click to change find distance"><click:suggest_command:/roadmap set find-distance #<id> <distance>><find-distance></click></hover>
			<col:colors.background>» <col:colors.baseB>Particles:
			<col:colors.background>  » <col:colors.default>Curve length: <col:colors.baseA><hover:show_text:"Click to change curve length"><click:suggest_command:/roadmap set curve-length #<id> <curve-length>><find-distance></click></hover>
			<col:colors.background>  » <col:colors.default>Particles: <col:colors.baseA><hover:show_text:"Click to change path-visualizer"><click:suggest_command:/roadmap set path-visualizer #<id> <pre><path-visualizer></pre>><path-visualizer></click></hover>
			<col:colors.background>  » <col:colors.default>Edit Particles: <col:colors.baseA><hover:show_text:"Click to change editmode-visualizer"><click:suggest_command:/roadmap set editmode-visualizer #<id> <pre><editmode-visualizer></pre>><editmode-visualizer></click></hover>
			""", placeholders = {"name", "id", "world", "findable", "find-distance", "curve-length", "path-visualizer", "editmode-visualizer"})
	public static final Message CMD_RM_INFO = new Message("commands.roadmap.info.header");
	@MessageMeta("<msg:prefix><col:colors.roadmap>Roadmap <col:colors.default>selected: <name>")
	public static final Message CMD_RM_SELECT = new Message("commands.roadmap.select");
	@MessageMeta("<msg:prefix><col:colors.roadmap>Roadmap <col:colors.default>deselected.")
	public static final Message CMD_RM_DESELECT = new Message("commands.roadmap.deselect");
	@MessageMeta("<col:colors.severe>Could not create Roadmap. Check out console for details.")
	public static final Message CMD_RM_CREATE_FAIL = new Message("commands.roadmap.create.fail");
	@MessageMeta(value = "<col:prefix>Successfully created Roadmap <name>.", placeholders = "name")
	public static final Message CMD_RM_CREATE_SUCCESS = new Message("commands.roadmap.create.success");

	@MessageMeta(value = "<col:prefix>Player <name> found <selection>.", placeholders = {"name", "selection"})
	public static final Message CMD_RM_FORCE_FIND = new Message("commands.roadmap.force_find");

	@MessageMeta(value = "<col:prefix>Successfully created Node <name>.", placeholders = "name")
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
	@MessageMeta(value = "<col:prefix>Search terms for <name>:\n<col:colors.background>»<values>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_LIST = new Message("commands.node_group.terms.list");
	@MessageMeta(value = "<col:prefix>Successfully added search terms to <name>: <values>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_ADD = new Message("commands.node_group.terms.add");
	@MessageMeta(value = "<col:prefix>Successfully removed search terms from <name>: <values>", placeholders = {"name", "values"})
	public static final Message CMD_NG_TERMS_REMOVE = new Message("commands.node_group.terms.remove");

	public static final Message CMD_CANCEL = new Message("commands.cancel_path");

	public static FormattedMessage formatBoolean(boolean val) {

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

	}

	public static FormattedMessage formatKey(@Nullable NamespacedKey key) {

	}
}
