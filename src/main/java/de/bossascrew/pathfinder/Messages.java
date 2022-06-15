package de.bossascrew.pathfinder;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageMeta;

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

	public static final Message CMD_CANCEL = new Message("commands.cancel_path");
}
