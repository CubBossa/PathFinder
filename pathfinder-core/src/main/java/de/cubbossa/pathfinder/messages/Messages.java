package de.cubbossa.pathfinder.messages;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.Vector;
import de.cubbossa.pathfinder.misc.World;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.NodeSelection;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageBuilder;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("checkstyle:LineLength")
public class Messages {

  public static final Message PREFIX = new MessageBuilder("prefix")
      .withDefault("PathFinder")
      .build();
  public static final Message GEN_TOO_FAST = new MessageBuilder("general.response_pending")
      .withDefault("<prefix>Better slow down, your database is out of breath.")
      .build();
  public static final Message GEN_ERROR = new MessageBuilder("general.error")
      .withDefault("<prefix_negative>{cause}</prefix_negative>")
      .withPlaceholders("cause")
      .build();
  public static final Message GEN_VECTOR = new MessageBuilder("general.vector")
      .withDefault("<text_hl>{vector.x:#.##}<text>,</text> {vector.y:#.##}<text>,</text> {vector.z:#.##}</text_hl>")
      .withPlaceholders("vector")
      .withComment("The numberformat can be specified as argument for x, y and z. Check out https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html for more information on number formatting.")
      .build();
  public static final Message GEN_LOC = new MessageBuilder("general.location")
      .withDefault("<text_hl>{loc.x:#.##}<text>,</text> {loc.y:#.##}<text>,</text> {loc.z:#.##}</text_hl>")
      .withPlaceholders("loc")
      .withComment("The numberformat can be specified as argument for x, y and z. Check out https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html for more information on number formatting.")
      .build();
  public static final Message GEN_PARTICLE = new MessageBuilder("general.particle")
      .withDefault("<text_hl>{particle}</text_hl>")
      .withPlaceholders("particle", "meta")
      .build();
  public static final Message GEN_PARTICLE_META = new MessageBuilder("general.particle")
      .withDefault("<text_hl>{particle} <text>({meta})</text></text_hl>")
      .withPlaceholders("particle", "meta")
      .build();

  public static final Message GEN_NULL = new MessageBuilder("general.null")
      .withDefault("<text_hl>null</text_hl>")
      .build();
  public static final Message GEN_NODE = new MessageBuilder("general.node")
      .withDefault("({node.world}; {node.location})")
      .withPlaceholders("node")
      .build();
  public static final Message GEN_NODE_SEL = new MessageBuilder("general.selection.nodes")
      .withDefault("<white><u>{sel.size} Nodes</u></white>")
      .withPlaceholders("amount")
      .build();
  public static final Message GEN_GROUP_SEL = new MessageBuilder("general.selection.groups")
      .withDefault("<text_hl><u>{sel.size} Groups</u></text_hl>")
      .withPlaceholders("amount")
      .build();
  public static final Message RELOAD_ERROR = new MessageBuilder("command.reload.error")
      .withDefault("<prefix_negative>An error occurred while reloading: {error}</prefix_negative>")
      .withPlaceholder("error")
      .build();
  public static final Message RELOAD_SUCCESS = new MessageBuilder("command.reload.success.general")
      .withDefault("<prefix>Successfully reloaded in <offset_l>{ms}</offset_l><offset>ms</offset>.")
      .withPlaceholders("ms")
      .build();
  public static final Message RELOAD_SUCCESS_LANG = new MessageBuilder("command.reload.success.language")
      .withDefault("<prefix>Successfully reloaded language in <offset_l>{ms}</offset_l><offset>ms</offset>.")
      .withPlaceholders("ms")
      .build();
  public static final Message RELOAD_SUCCESS_FX = new MessageBuilder("command.reload.success.effects")
      .withDefault("<prefix>Successfully reloaded effects in <offset_l>{ms}</offset_l><offset>ms</offset>.")
      .withPlaceholders("ms")
      .build();
  public static final Message RELOAD_SUCCESS_CFG = new MessageBuilder("command.reload.success.config")
      .withDefault("<prefix>Successfully reloaded config files in <offset_l>{ms}</offset_l><offset>ms</offset>.")
      .withPlaceholders("ms")
      .build();

  public static final Message HELP = new MessageBuilder("general.help")
      .withDefault("""
          <header>Pathfinder</header>
          <text>Running <offset>Pathfinder v{version}</offset>.
                
          <text>Require help? Checkout the <text_hl><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></text_hl>.
          <text>Use <text_hl>/pf help</text_hl> to view available commands.
          """)
      .withPlaceholder("version")
      .build();
  public static final Message CMD_DUMP_SUCCESS = new MessageBuilder("command.createdump.success")
      .withDefault("<prefix>Dump file successfully created in plugin directory.")
      .build();
  public static final Message CMD_DUMP_FAIL = new MessageBuilder("command.createdump.failure")
      .withDefault("<prefix_negative>Dump file could not be created. Check console for details.</prefix_negative>")
      .build();
  public static final Message CMD_HELP = new MessageBuilder("command.help")
      .withDefault("""
          <header>Pathfinder</header>
          <text>Require help? Checkout the <text_hl><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></text_hl>.
                    
          <white>Commands:</white>
          <bg>» </bg><text><text_hl>/pf editmode</text_hl> - Create, edit and delete waypoints via GUI
          <bg>» </bg><text><text_hl>/pf node</text_hl> - Create, edit and delete waypoints via Commands
          <bg>» </bg><text><text_hl>/pf group</text_hl> - Add behaviour to multiple waypoints
          <bg>» </bg><text><text_hl>/pf visualizer</text_hl> - Compass, particles, placeholders and more
          <bg>» </bg><text><text_hl>/find</text_hl> - Find the shortest way to a nodegroup
          """)
      .build();

  public static final Message CMD_INCOMPLETE = new MessageBuilder("command.error.incomplete.title")
      .withDefault("<red>Your command is incomplete. Use one of the syntaxes below:")
      .build();
  public static final Message CMD_INCOMPLETE_LINE = new MessageBuilder("command.error.incomplete.line")
      .withDefault("<bg>» </bg>{cmd}")
      .withPlaceholders("cmd")
      .build();

  public static final Message INFO = new MessageBuilder("commands.info")
      .withDefault("""
          <header>Pathfinder</header>
          <bg>» </bg><text>Current version: <text_hl>{version}</text_hl>
          <bg>» </bg><text>Spigot-Page: <url><text_hl>https://www.spigotmc.org/resources/gps-pathfinder-minecraft-pathfinding-tool.104961/</text_hl></url>
          """)
      .withPlaceholders("authors", "version", "api-version")
      .build();

  public static final Message MODULES = new MessageBuilder("commands.modules")
      .withDefault("""
          <offset>Active Modules:</offset>
          {modules:"<newline>":"<bg>» </bg>"}""")
      .withPlaceholder("modules")
      .build();

  public static final Message CMD_FORCE_FIND = new MessageBuilder("commands.force_find")
      .withDefault("<prefix>Player {target} discovered <text_hl>{discovery}</text_hl>.")
      .withPlaceholder("target", "discovery")
      .build();
  public static final Message CMD_FORCE_FORGET = new MessageBuilder("commands.force_forget")
      .withDefault("<prefix>Player {target} forgot about <text_hl>{discovery}</text_hl>.")
      .withPlaceholders("target", "discovery")
      .build();

  public static final Message CMD_N_CREATE = new MessageBuilder("commands.node.create")
      .withDefault("<prefix>Successfully created Node at {node.loc}.")
      .withPlaceholders("node")
      .build();
  public static final Message CMD_N_DELETE = new MessageBuilder("commands.node.delete")
      .withDefault("<prefix>Successfully deleted {selection}.")
      .withPlaceholders("selection")
      .build();
  public static final Message CMD_N_UPDATED = new MessageBuilder("commands.node.moved")
      .withDefault("<prefix>Updated {selection}.")
      .withPlaceholders("selection", "location")
      .build();
  public static final Message CMD_N_INFO = new MessageBuilder("commands.node.info")
      .withDefault("""
          <offset>Node #<id></offset>
          <bg>» </bg><text>Position: <text_hl>{node.loc}</text_hl> ({node.loc.world})
          <bg>» </bg><text>Edges: {node.edges}
          <bg>» </bg><text>Groups: {node.groups}
          """)
      .withPlaceholders("id", "groups", "position", "world", "edges")
      .build();
  public static final Message CMD_N_INFO_NO_SEL = new MessageBuilder("commands.node.info_no_selection")
      .withDefault("<prefix_negative>No nodes found to display. Check your selection query.</prefix_negative>")
      .build();
  public static final Message CMD_N_ADD_GROUP = new MessageBuilder("commands.node.add_group")
      .withDefault("<prefix>Added {nodes} to group <text_hl>{group}</text_hl>.")
      .withPlaceholders("nodes", "group")
      .build();
  public static final Message CMD_N_REMOVE_GROUP = new MessageBuilder("commands.node.remove_groups")
      .withDefault("<prefix>Removed {nodes} from group <text_hl>{group}</text_hl>.")
      .withPlaceholders("nodes", "group")
      .build();
  public static final Message CMD_N_CLEAR_GROUPS = new MessageBuilder("commands.node.clear_groups")
      .withDefault("<prefix>Cleared all groups for {nodes}.")
      .withPlaceholders("nodes")
      .build();

  public static final Message CMD_N_LIST = new MessageBuilder("commands.node.list")
      .withDefault("""
          <header><primary_l>Waypoints</primary_l></header>
          <nodes><hover:show_text:'<text>Groups: {el.groups}<newline><text>Edges to: {el.edges}<newline><text>Click for more information'><click:run_command:'/pf nodes "@n[id={node.id}]" info'><text>at {el.loc} ({el.loc.world})</nodes>
          <footer:'/pf listnodes "<selector>"'/>""")
      .withPlaceholders("nodes", "selector", "page", "next-page", "prev-page", "pages")
      .build();

  public static final Message CMD_N_CONNECT = new MessageBuilder("commands.node.connect.success")
      .withDefault("<prefix>Connected {start} to {end}.")
      .withPlaceholders("start", "end")
      .build();
  public static final Message CMD_N_DISCONNECT = new MessageBuilder("commands.node.disconnect.success")
      .withDefault("<prefix>Disconnected {start} from {end}.")
      .withPlaceholders("start", "end")
      .build();

  public static final Message CMD_NG_CREATE_FAIL = new MessageBuilder("commands.node_group.create_fail")
      .withDefault("<prefix_negative>Could not create Nodegroup. Check out console for details.")
      .build();
  public static final Message CMD_NG_ALREADY_EXISTS = new MessageBuilder("commands.node_group.already_exists")
      .withDefault("<prefix_negative>A node group {group_key} already exists.</prefix_negative>")
      .withPlaceholders("group_key")
      .build();
  public static final Message CMD_NG_CREATE = new MessageBuilder("commands.node_group.create")
      .withDefault("<prefix>Node group <text_hl>{group.key}</text_hl> created.")
      .withPlaceholders("group")
      .build();
  public static final Message CMD_NG_DELETE = new MessageBuilder("commands.node_group.delete")
      .withDefault("<prefix>Node group <text_hl>{group_key}</text_hl> deleted.")
      .withPlaceholders("group_key")
      .build();
  public static final Message CMD_NG_DELETE_GLOBAL = new MessageBuilder("commands_node_group.delete_fail_global")
      .withDefault("<prefix_negative>You cannot delete the global node group.</prefix_negative>")
      .withComment("Indicates, that the global nodegroup cannot be deleted by command.")
      .build();
  public static final Message CMD_NG_INFO = new MessageBuilder("commands.node_group.info")
      .withDefault("""
          <primary_l>Group '{group.key}'</primary_l>
          <bg>» </bg><text>Size: {group.nodes}
          <bg>» </bg><text>Weight: <text_hl>{group.weight}</text_hl>
          <group.modifiers:'<newline>'><bg>» </bg><text></group.modifiers>
          """)
      .withPlaceholders("group")
      .build();
  public static final Message CMD_NG_LIST = new MessageBuilder("commands.node_group.list")
      .withDefault("""
          <header>Node-Groups</header>
          <groups:'<newline>'><bg> » </bg>{el.key} <text>(Weight: {el.weight})</text></groups>
          <footer:'/pf listgroups'/>
          """)
      .withPlaceholders("groups", "page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_NG_MODIFY_SET = new MessageBuilder("commands.node_group.modify.set")
      .withDefault("<prefix>Added modifier <text_hl>{type}</text_hl> to group <text_hl>{group}</text_hl>.")
      .withPlaceholders("type", "group")
      .build();
  public static final Message CMD_NG_MODIFY_REMOVE = new MessageBuilder("commands.node_group.modify.remove")
      .withDefault("<prefix>Removed modifier <text_hl>{type}<text_hl> from group <text_hl>{group}</text_hl>.")
      .withPlaceholders("type", "group")
      .build();
  public static final Message CMD_NG_MOD_CURVELEN = new MessageBuilder("commands.node_group.modifier.curvelength")
      .withDefault("Curve length: {length:#.##}")
      .withPlaceholder("length", "Use java number formatting to provide custom formatting.")
      .build();
  public static final Message CMD_NG_MOD_DISCOVER = new MessageBuilder("commands.node_group.modifier.discoverable")
      .withDefault("Discover as: {name}")
      .withPlaceholder("name", "The name that is being shown when discovering this group.")
      .build();
  public static final Message CMD_NG_MOD_DISCOVERIES = new MessageBuilder("commands.node_group.modifier.discover-progress")
      .withDefault("Discover progress as: {name}")
      .withPlaceholder("name", "The name that is being shown when running /discoveries.")
      .build();
  public static final Message CMD_NG_MOD_FINDDIST = new MessageBuilder("commands.node_group.modifier.finddistance")
      .withDefault("Find distance: {distance:#.##}")
      .withPlaceholder("distance", "Use java number formatting to provide custom formatting.")
      .build();
  public static final Message CMD_NG_MOD_SEARCH = new MessageBuilder("commands.node_group.modifier.navigable")
      .withDefault("Search terms: <terms:', '>{el}</terms>")
      .withPlaceholder("terms", "A list tag for all search terms, use <terms[:between]>", List.class)
      .build();
  public static final Message CMD_NG_MOD_PERM = new MessageBuilder("commands.node_group.modifier.permission")
      .withDefault("Permission: {permission}")
      .withPlaceholder("permission")
      .build();
  public static final Message CMD_NG_MOD_VIS = new MessageBuilder("commands.node_group.modifier.visualizer")
      .withDefault("Visualizer: {visualizer}")
      .withPlaceholder("visualizer")
      .build();

  public static final Message CMD_FIND = new MessageBuilder("commands.find.success")
      .withDefault("<prefix>Navigation started.  <click:run_command:/cancelpath><button>/cancelpath</button></click>")
      .build();
  public static final Message CMD_DISCOVERIES_LIST = new MessageBuilder("commands.discoveries.list")
      .withDefault("""
          <header><primary_l>Discoveries</primary_l></header>
          <discoveries:'<newline>'><bg>» </bg>{el.name}: {el.percentage:#.##}%</discoveries>
          <footer:'/discoveries'/>""")
      .withPlaceholders("discoveries", "page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_FIND_EMPTY = new MessageBuilder("commands.find.no_nodes_found")
      .withDefault("<prefix>No matching waypoints could be found.")
      .build();
  public static final Message CMD_FIND_TOO_FAR = new MessageBuilder("commands.find.too_far_away")
      .withDefault("<prefix>The given location is too far away from any waypoint.")
      .build();
  public static final Message CMD_FIND_BLOCKED = new MessageBuilder("commands.find.no_path_found")
      .withDefault("<prefix>No possible way could be found to reach that target.")
      .build();
  public static final Message CMD_FIND_UNKNOWN = new MessageBuilder("commands.find.unknown_error")
      .withDefault("<prefix_negative>An unknown error occurred.</prefix_negative>")
      .build();
  public static final Message CMD_CANCEL = new MessageBuilder("commands.cancel_path")
      .withDefault("<prefix>Navigation cancelled.")
      .build();
  public static final Message CMD_FINDP_OFFLINE = new MessageBuilder("commands.find_player.target_offline")
      .withDefault("<prefix_negative>Player not found.</prefix_negative>")
      .build();
  public static final Message CMD_FINDP_NO_SELF = new MessageBuilder("commands.find_player.no_requests_to_self")
      .withDefault("<prefix_negative>You cannot make requests to yourself.</prefix_negative>")
      .build();
  public static final Message CMD_FINDP_NO_REQ = new MessageBuilder("commands.find_player.no_requests")
      .withDefault("<prefix_negative>No requests found.</prefix_negative>")
      .build();
  public static final Message CMD_FINDP_ALREADY_REQ = new MessageBuilder("commands.find_player.already_requested")
      .withDefault("<prefix_negative>Navigation already requested.</prefix_negative>")
      .build();
  public static final Message CMD_FINDP_REQUEST = new MessageBuilder("commands.find_player.request")
      .withDefault("<prefix>Made a request to navigate to <text_hl>{target}</text_hl>.")
      .withPlaceholders("requester", "target")
      .build();
  public static final Message CMD_FINDP_REQUESTED = new MessageBuilder("commands.find_player.requested")
      .withDefault("<prefix><text_hl>{requester}</text_hl> asked to navigate to you. <click:run_command:'/fpaccept <requester>'><btn_accept>accept</btn_accept></click>  <click:run_command:'/fpdecline <requester>'><btn_decline>decline</btn_decline></click>")
      .withPlaceholders("requester", "target")
      .build();
  public static final Message CMD_FINDP_ACCEPT = new MessageBuilder("commands.find_player.accept")
      .withDefault("<prefix>Request accepted.")
      .withPlaceholders("requester", "target")
      .build();
  public static final Message CMD_FINDP_ACCEPTED = new MessageBuilder("commands.find_player.accepted")
      .withDefault("<prefix><text_hl>{target}</text_hl> accepted your navigate request.")
      .withPlaceholders("requester", "target")
      .build();
  public static final Message CMD_FINDP_DECLINE = new MessageBuilder("commands.find_player.decline")
      .withDefault("<prefix>Request declined.")
      .withPlaceholders("requester", "target")
      .build();
  public static final Message CMD_FINDP_DECLINED = new MessageBuilder("commands.find_player.declined")
      .withDefault("<prefix><text_hl>{target}</text_hl> declined your navigate request.")
      .withPlaceholders("requester", "target")
      .build();
  public static final Message CMD_FINDP_EXPIRED = new MessageBuilder("commands.find_player.request_expired")
      .withDefault("<prefix>Your request expired.")
      .build();
  public static final Message DISCOVERY_DISCOVER = new MessageBuilder("discovery.discover")
      .withDefault("You discovered: {discoverable}")
      .withPlaceholders("player", "discoverable", "group")
      .build();
  public static final Message DISCOVERY_PROG = new MessageBuilder("discovery.progress")
      .withDefault("{percentage:#.##}% of {name}")
      .withPlaceholders("player", "discoverable", "group", "name", "percentage", "ratio", "count-found", "count-all")
      .build();
  public static final Message DISCOVERY_FORGET = new MessageBuilder("discovery.forget")
      .withDefault("<prefix>You forgot all about {discoverable}")
      .withPlaceholders("player", "discoverable", "group")
      .build();


  public static final Message CMD_VIS_LIST = new MessageBuilder("commands.path_visualizer.list")
      .withDefault("""
          <header>Visualizer</header>
          <visualizers:'<newline>'><bg> » </bg><primary_l>{el.key}</primary_l> <text>({el.type})</text></visualizers>
          <footer:'/pf listvisualizers'/>""")
      .withPlaceholders("page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_VIS_NO_TYPE_FOUND = new MessageBuilder("commands.path_visualizer.info.no_type")
      .withDefault("<prefix_negative>Could not show information to visualizer. Type could not be resolved.</prefix_negative>")
      .build();
  public static final Message CMD_VIS_NO_INFO = new MessageBuilder("commands.path_visualizer.info.no_info")
      .withDefault("<prefix_negative>Could not show information to visualizer. No message layout provided.</prefix_negative>")
      .build();

  public static final Message CMD_VIS_CREATE_SUCCESS = new MessageBuilder("commands.path_visualizer.create.success")
      .withDefault("<prefix>Successfully created Visualizer <text_hl>{visualizer_key}</text_hl> of type <text_hl>{type}</text_hl>.")
      .withPlaceholders("visualizer_key", "type")
      .build();
  public static final Message CMD_VIS_NAME_EXISTS = new MessageBuilder("commands.path_visualizer.create.already_exists")
      .withDefault("<prefix_negative>Another visualizer with this name already exists.")
      .build();
  public static final Message CMD_VIS_DELETE_SUCCESS = new MessageBuilder("commands.path_visualizer.delete.success")
      .withDefault("<prefix>Successfully deleted Visualizer <offset>{visualizer_key}</offset>.")
      .withPlaceholders("visualizer_key")
      .build();
  public static final Message CMD_VIS_DELETE_ERROR = new MessageBuilder("commands.path_visualizer.delete.error")
      .withDefault("<prefix_negative>An unknown error occurred while deleting a visualizer. Please check the console for more information.")
      .build();
  public static final Message CMD_VIS_SET_PROP = new MessageBuilder("commands.path_visualizer.set_property")
      .withDefault("<prefix>Changed {property} for <text_hl>{vis.key}</text_hl> from <text_hl>{old-value}</text_hl> to <text_hl>{value}</text_hl>.")
      .withPlaceholders("vis", "visualizer", "property", "value", "old-value")
      .build();
  public static final Message CMD_VIS_SET_PROP_ERROR = new MessageBuilder("commands.path_visualizer.set_property_error")
      .withDefault("<prefix_negative>Could not set property {property} for visualizer.")
      .withPlaceholders("vis", "visualizer", "property")
      .build();
  public static final Message CMD_VIS_IMPORT_EXISTS = new MessageBuilder("commands.path_visualizer.import.already_exists")
      .withDefault("<prefix_negative>Could not import file, another visualizer with this key already exists.</prefix_negative>")
      .build();
  public static final Message CMD_VIS_IMPORT_NOT_EXISTS = new MessageBuilder("commands.path_visualizer.import.file_doesnt_exist")
      .withDefault("<prefix_negative>Could not import file, there is no example file with this name.</prefix_negative>")
      .build();
  public static final Message CMD_VIS_IMPORT_SUCCESS = new MessageBuilder("commands.path_visualizer.import.successful")
      .withDefault("<prefix>Successfully imported Visualizer <text_hl>{visualizer}</text_hl>")
      .withPlaceholders("vis", "visualizer")
      .build();
  public static final Message CMD_VIS_COMBINED_INFO = new MessageBuilder("commands.path_visualizer.type.combined.info")
      .withDefault("""
          <primary_l>Visualizer: {vis.key}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle <key> permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Children:
          <entries:"<newline>"><br><bg>  » </bg>{el}</entries>""")
      .withPlaceholders("vis", "visualizer")
      .withPlaceholder("entries[:separator]")
      .build();
  public static final Message CMD_VIS_COMBINED_ADD = new MessageBuilder("commands.path_visualizer.type.combined.add")
      .withDefault("<prefix>Added {child} as child to {visualizer}.")
      .withPlaceholders("child", "visualizer")
      .build();
  public static final Message CMD_VIS_COMBINED_REMOVE = new MessageBuilder("commands.path_visualizer.type.combined.remove")
      .withDefault("<prefix>Removed {child} from children for {visualizer}.")
      .build();
  public static final Message CMD_VIS_COMBINED_CLEAR = new MessageBuilder("commands.path_visualizer.type.combined.clear")
      .withDefault("<prefix>Cleared all children for {visualizer}.")
      .build();
  public static final Message CMD_VIS_INFO_PARTICLES = new MessageBuilder("commands.path_visualizer.type.particle_visualizer.info")
      .withDefault("""
          <primary_l>Visualizer: {vis.key}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle <key> permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Interval: <text_hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit particle <key> interval">{interval}</click></hover></text_hl>
          <bg>» </bg><text>Point-Distance: <text_hl><hover:show_text:"Click to change point-distance"><click:suggest_command:"/pathvisualizer edit particle <key> point-distance">{point-distance}</click></hover></text_hl>
          <bg>» </bg><text>Particle: <text_hl><hover:show_text:"Click to change particle"><click:suggest_command:"/pathvisualizer edit particle <key> particle">{particle}</click></hover></text_hl>
          <bg>» </bg><text>Particle-Steps: <text_hl><hover:show_text:"Click to change particle-steps"><click:suggest_command:"/pathvisualizer edit particle-steps <key> particle">{particle-steps}</click></hover></text_hl>
          <bg>» </bg><text>Amount: <text_hl><hover:show_text:"Click to change amount"><click:suggest_command:"/pathvisualizer edit particle <key> particle">{amount}</click></hover></text_hl>
          <bg>» </bg><text>Speed: <text_hl><hover:show_text:"Click to change speed"><click:suggest_command:"/pathvisualizer edit particle <key> speed">{speed}</click></hover></text_hl>
          <bg>» </bg><text>Offset: <text_hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit particle <key> particle">{offset}</click></hover></text_hl>""")
      .withPlaceholders(
          "vis", "visualizer", "interval", "point-distance",
          "particle", "particle-steps", "amount", "speed", "offset")
      .build();


  public static final Message CMD_VIS_COMPASS_INFO = new MessageBuilder("commands.path_visualizer.type.compass.info")
      .withDefault("""
          <primary_l>Visualizer: {vis.key}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Interval: <text_hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval">{interval}</click></hover></text_hl>
          <bg>» </bg><text>Marker:
              <bg>» </bg><text>Target: {marker-target}
              <bg>» </bg><text>North: {marker-north}
              <bg>» </bg><text>East: {marker-east}
              <bg>» </bg><text>South: {marker-south}
              <bg>» </bg><text>West: {marker-west}
          <bg>» </bg><text>Background: {background}
          <bg>» </bg><text>Color: {color_value}
          <bg>» </bg><text>Overlay: {overlay}""")
      .withPlaceholders("vis", "visualizer", "marker-north", "marker-south", "marker-east", "marker-west",
          "marker-target", "background", "color_value", "overlay")
      .build();

  public static final Message CMD_ADV_VIS_INFO_PARTICLES = new MessageBuilder("commands.path_visualizer.type.advanced_particle_visualizer.info")
      .withDefault("""
          <primary_l>Visualizer: {vis.key}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Interval: <text_hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval">{interval}</click></hover></text_hl>
          <bg>» </bg><text>Point-Distance: <text_hl><hover:show_text:"Click to change point-distance"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> point-distance">{point-distance}</click></hover></text_hl>
          <bg>» </bg><text>Particle: <text_hl><hover:show_text:"Click to change particle"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle">{particle}</click></hover></text_hl>
          <bg>» </bg><text>Particle-Data: <text_hl><hover:show_text:"Click to change particle-Data"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle-data">{particle-data}</click></hover></text_hl>
          <bg>» </bg><text>Amount: <text_hl><hover:show_text:"Click to change amount"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle">{amount}</click></hover></text_hl>
          <bg>» </bg><text>Speed: <text_hl><hover:show_text:"Click to change speed"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle">{speed}</click></hover></text_hl>
          <bg>» </bg><text>Offset:
              <bg>» </bg><text>X: <text_hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-x">{offset-x}</click></hover></text_hl>
              <bg>» </bg><text>Y: <text_hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-y">{offset-y}</click></hover></text_hl>
              <bg>» </bg><text>Z: <text_hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-z">{offset-z}</click></hover></text_hl>
          <bg>» </bg><text>Path Offset (e.g. to make Spirals):
              <bg>» </bg><text>X: <text_hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-x">{path-x}</click></hover></text_hl>
              <bg>» </bg><text>Y: <text_hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-y">{path-y}</click></hover></text_hl>
              <bg>» </bg><text>Z: <text_hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-z">{path-z}</click></hover></text_hl>""")
      .withPlaceholders("vis", "visualizer", "interval", "point-distance",
          "particle", "particle-steps", "amount", "speed", "offset-x", "offset-y", "offset-z",
          "path-x", "path-y", "path-z")
      .build();

  public static final Message CMD_VIS_PAPI_INFO = new MessageBuilder("commands.path_visualizer.type.placeholder_api.info")
      .withDefault("""
          <primary_l>Visualizer: {vis.key}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Interval: <text_hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval">{interval}</click></hover></text_hl>
          <bg>» </bg><text>Placeholder:</text>
              <bg>» </bg><text>North: <text_hl><hover:show_text:"Click to change format-north"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-north">{format-north}</click></hover></text_hl>
              <bg>» </bg><text>North-East: <text_hl><hover:show_text:"Click to change format-northeast"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-northeast">{format-northeast}</click></hover></text_hl>
              <bg>» </bg><text>East: <text_hl><hover:show_text:"Click to change format-east"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-east">{format-east}</click></hover></text_hl>
              <bg>» </bg><text>South-East: <text_hl><hover:show_text:"Click to change format-southeast"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-southeast">{format-southeast}</click></hover></text_hl>
              <bg>» </bg><text>South: <text_hl><hover:show_text:"Click to change format-south"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-south">{format-south}</click></hover></text_hl>
              <bg>» </bg><text>South-West: <text_hl><hover:show_text:"Click to change format-southwest"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-southwest">{format-southwest}</click></hover></text_hl>
              <bg>» </bg><text>West: <text_hl><hover:show_text:"Click to change format-west"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-west">{format-west}</click></hover></text_hl>
              <bg>» </bg><text>North-West: <text_hl><hover:show_text:"Click to change format-northwest"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-northwest">{format-northwest}</click></hover></text_hl>
              <bg>» </bg><text>Distance: <text_hl><hover:show_text:"Click to change format-distance"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-distance">{format-distance}</click></hover></text_hl>""")
      .withPlaceholders(
          "vis", "visualizer", "interval", "format-north", "format-north-east",
          "format-east", "format-south-east", "format-south", "format-south-west", "format-west", "format-north-west",
          "format-distance"
      )
      .build();

  public static final Message E_NODE_TOOL_N = new MessageBuilder("editor.toolbar.node_tool.name")
      .withDefault("<text_l><u>Node Tool</u></text_l>")
      .build();
  public static final Message E_NODE_TOOL_L = new MessageBuilder("editor.toolbar.node_tool.lore")
      .withDefault("""
          <text>» <accent>right-click:</accent> Create node</text>
          <text>» <accent>left-click:</accent> Delete clicked node</text>
          <text>» <accent>left-click air:</accent> Activate chain mode</text>""")
      .build();
  public static final Message E_EDGEDIR_TOOL_N = new MessageBuilder("editor.toolbar.edge_directed_toggle.name")
      .withDefault("<text_l><u>Edges Directed: <text_hl><value:true:false></text_hl></u></text_l>")
      .withPlaceholder("value", "Choice Placeholder, usage: <value:show-this-if-true:show-this-if-false>")
      .build();
  public static final Message E_EDGEDIR_TOOL_L = new MessageBuilder("editor.toolbar.edge_directed_toggle.lore")
      .withDefault("""
          <text>An edge is directed if its
          color goes from red to blue.
          Players can cross this section only
          in that direction, like a one way road.""")
      .build();
  public static final Message E_NODE_CHAIN_NEW = new MessageBuilder("editor.node_tool.chain.new")
      .withDefault("<prefix>Node chain completed.")
      .build();
  public static final Message E_NODE_CHAIN_START = new MessageBuilder("editor.node_tool.chain.new_start")
      .withDefault("<prefix>Chain started.")
      .build();
  public static final Message E_NODE_TOOL_DIR_TOGGLE = new MessageBuilder("editor.toolbar.node_tool.directed")
      .withDefault("<prefix>Edges directed: <text_hl>{value}<text_hl>")
      .withPlaceholders("value")
      .build();
  public static final Message E_GROUP_TOOL_N = new MessageBuilder("editor.toolbar.group_tool.name")
      .withDefault("<text_l><u>Assign Group</u></text_l>")
      .build();
  public static final Message E_GROUP_TOOL_L = new MessageBuilder("editor.toolbar.group_tool.lore")
      .withDefault("")
      .build();
  public static final Message E_MULTI_GROUP_TOOL_N = new MessageBuilder("editor.toolbar.multi_group_tool.name")
      .withDefault("<text_l><u>Multi Group Tool</u></text_l>")
      .build();
  public static final Message E_MULTI_GROUP_TOOL_L = new MessageBuilder("editor.toolbar.multi_group_tool.lore")
      .withDefault("""
          <text>Assign and remove multiple
          <text>groups at once.
                    
          <text>» <accent>right-click air:</accent> Open GUI</text>
          <text>» <accent>right-click node:</accent> Add groups</text>
          <text>» <accent>left-click node:</accent> Remove groups</text>""")
      .build();
  public static final Message E_TP_TOOL_N = new MessageBuilder("editor.toolbar.teleport_tool.name")
      .withDefault("<text_l><u>Teleport Tool</u></text_l>")
      .build();
  public static final Message E_TP_TOOL_L = new MessageBuilder("editor.toolbar.teleport_tool.lore")
      .withDefault("<text>Teleports you to the<newline><text>nearest node.")
      .build();
  public static final Message E_SUB_GROUP_TITLE = new MessageBuilder("editor.groups.title")
      .withDefault("Assign Node Groups")
      .build();
  public static final Message E_SUB_GROUP_INFO_N = new MessageBuilder("editor.groups.info.name")
      .withDefault("<accent>Info</accent>")
      .build();
  public static final Message E_SUB_GROUP_INFO_L = new MessageBuilder("editor.groups.info.lore")
      .withDefault("<text>Click to toggle groups on or off.</text><newline><text>Create a new nodegroup with<newline><text>» <accent>/pf creategroup <key>")
      .build();
  public static final Message E_SUB_GROUP_RESET_N = new MessageBuilder("editor.groups.reset.name")
      .withDefault("<prefix_negative>Reset Groups</prefix_negative>")
      .build();
  public static final Message E_SUB_GROUP_RESET_L = new MessageBuilder("editor.groups.reset.lore")
      .withDefault("<text>Reset all groups for the<newline><text>selected node.")
      .build();
  public static final Message E_SUB_GROUP_ENTRY_N = new MessageBuilder("editor.groups.entry.name")
      .withDefault("<primary_l>{group.key}</primary_l>")
      .withPlaceholders("group")
      .build();
  public static final Message E_SUB_GROUP_ENTRY_L = new MessageBuilder("editor.groups.entry.lore")
      .withDefault("""
          <bg>» </bg><text>Weight: </text><text_hl>{group.weight:#.##}</text_hl>
          <group.modifiers:"<newline>"><bg>» </bg><el/></group.modifiers>""")
      .withPlaceholders("group")
      .build();
  public static final Message TARGET_FOUND = new MessageBuilder("general.target_reached")
      .withDefault("<prefix>Target reached.")
      .build();

  public static final Message EDITM_NG_DELETED = new MessageBuilder("editmode.group_deleted")
      .withDefault("<prefix_negative>Your currently edited group was deleted by another user.")
      .build();

  public static List<TinyObjectMapping> objectResolvers() {

    ArrayList<TinyObjectMapping> result = new ArrayList<>();

    result.add(TinyObjectMapping.builder(PathVisualizer.class)
        .withFallbackConversion(PathVisualizer::getKey)
        .with("key", PathVisualizer::getKey)
        .with("type", v -> PathFinder.get().getStorage().loadVisualizerType(v.getKey()).join())
        .with("permission", PathVisualizer::getPermission)
        .build());

    result.add(TinyObjectMapping.builder(VisualizerType.class)
        .withFallbackConversion(VisualizerType::getKey)
        .with("key", VisualizerType::getKey)
        .with("command", VisualizerType::getCommandName)
        .build());

    result.add(TinyObjectMapping.builder(NodeSelection.class)
        .withFallbackConversion(n -> GEN_NODE_SEL.insertObject("sel", n))
        .with("size", NodeSelection::size)
        .with("ids", NodeSelection::getIds)
        .build());

    result.add(TinyObjectMapping.builder(NodeGroup.class)
        .withFallbackConversion(NodeGroup::getKey)
        .with("key", NodeGroup::getKey)
        .with("weight", NodeGroup::getWeight)
        .with("nodes", ArrayList::new)
        .build());

    result.add(TinyObjectMapping.builder(Node.class)
        .withFallbackConversion(Node::getNodeId)
        .with("edges", Node::getEdges)
        .with("loc", Node::getLocation)
        .with("location", Node::getLocation)
        .with("groups", n -> n instanceof GroupedNode gn ? gn.groups() : new ArrayList<>())
        .build());

    result.add(TinyObjectMapping.builder(World.class)
        .withFallbackConversion(World::getName)
        .with("name", World::getName)
        .with("id", World::getUniqueId)
        .build());
    result.add(TinyObjectMapping.builder(Location.class)
        .withFallbackConversion(e -> GEN_LOC.insertObject("loc", e))
        .with("world", Location::getWorld)
        .build());
    result.add(TinyObjectMapping.builder(Vector.class)
        .withFallbackConversion(e -> GEN_VECTOR.insertObject("vector", e))
        .with("x", Vector::getX)
        .with("y", Vector::getY)
        .with("z", Vector::getZ)
        .build());

    return result;
  }
}
