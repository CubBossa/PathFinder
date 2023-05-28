package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.nodegroup.SimpleNodeGroup;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageBuilder;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
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
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("checkstyle:LineLength")
public class Messages {

  public static final Message PREFIX = new MessageBuilder("prefix")
      .withDefault("<offset>PathFinder</offset> <dark_gray>»</dark_gray> <gray>")
      .build();
  public static final Message GEN_TRUE = new MessageBuilder("general.true")
      .withDefault("<offset_light>true</offset_light>")
      .build();
  public static final Message GEN_FALSE = new MessageBuilder("general.false")
      .withDefault("<offset_light>false</offset_light>")
      .build();
  public static final Message GEN_VECTOR = new MessageBuilder("general.vector")
      .withDefault("<offset_light><x:#.##><gray>,</gray> <y:#.##><gray>,</gray> <z:#.##></offset_light>")
      .withPlaceholders("x", "y", "z")
      .withComment("The numberformat can be specified as argument for x, y and z. Check out https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html for more information on number formatting.")
      .build();
  public static final Message GEN_PERMISSION = new MessageBuilder("general.permission")
      .withDefault("<main><permission></main>")
      .build();
  public static final Message GEN_PARTICLE = new MessageBuilder("general.particle")
      .withDefault("<main><particle></main>")
      .withPlaceholders("particle", "meta")
      .build();
  public static final Message GEN_PARTICLE_META = new MessageBuilder("general.particle")
      .withDefault("<main><particle> <gray>(<meta>)</gray></main>")
      .withPlaceholders("particle", "meta")
      .build();

  public static final Message GEN_NULL = new MessageBuilder("general.null")
      .withDefault("<main>null</main>")
      .build();
  public static final Message GEN_NODE = new MessageBuilder("general.node")
      .withDefault("(<world>; <location>)")
      .withPlaceholders("id", "world", "location")
      .build();
  public static final Message GEN_NODE_SEL = new MessageBuilder("general.selection.nodes")
      .withDefault("<white><u><amount> Nodes</u></white>")
      .withPlaceholders("amount")
      .build();
  public static final Message GEN_GROUP_SEL = new MessageBuilder("general.selection.groups")
      .withDefault("<white><u><amount> Groups</u></white>")
      .withPlaceholders("amount")
      .build();
  public static final Message RELOAD_ERROR = new MessageBuilder("command.reload.error")
      .withDefault("<negative>An error occurred while reloading: <error></negative>")
      .withPlaceholder("error")
      .build();
  public static final Message RELOAD_SUCCESS = new MessageBuilder("command.reload.success.general")
      .withDefault("<msg:prefix>Successfully reloaded in <offset_light><ms></offset_light><offset>ms</offset>.")
      .withPlaceholders("ms")
      .build();
  public static final Message RELOAD_SUCCESS_LANG = new MessageBuilder("command.reload.success.language")
      .withDefault("<msg:prefix>Successfully reloaded language in <offset_light><ms></offset_light><offset>ms</offset>.")
      .withPlaceholders("ms")
      .build();
  public static final Message RELOAD_SUCCESS_FX = new MessageBuilder("command.reload.success.effects")
      .withDefault("<msg:prefix>Successfully reloaded effects in <offset_light><ms></offset_light><offset>ms</offset>.")
      .withPlaceholders("ms")
      .build();
  public static final Message RELOAD_SUCCESS_CFG = new MessageBuilder("command.reload.success.config")
      .withDefault("<msg:prefix>Successfully reloaded config files in <offset_light><ms></offset_light><offset>ms</offset>.")
      .withPlaceholders("ms")
      .build();

  public static final Message HELP = new MessageBuilder("general.help")
      .withDefault("""
          <gradient:black:dark_gray:black>------------ <offset>Pathfinder</offset> ------------</gradient>
          <gray>Running <offset>Pathfinder v<version></offset>.
                
          <gray>Require help? Checkout the <warm><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></warm>.
          <gray>Use <warm>/pf help</warm> to view available commands.
          """)
      .withPlaceholder("version")
      .build();
  public static final Message CMD_HELP = new MessageBuilder("command.help")
      .withDefault("""
          <gradient:black:dark_gray:black>------------ <offset>Pathfinder</offset> ------------</gradient>
          <gray>Require help? Checkout the <warm><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></warm>.
                    
          <white>Commands:</white>
          <dark_gray>» </dark_gray><gray><warm>/roadmap</warm> - Group waypoints in roadmaps
          <dark_gray>» </dark_gray><gray><warm>/nodegroup</warm> - Add behaviour to multiple waypoints
          <dark_gray>» </dark_gray><gray><warm>/waypoint</warm> - Create, edit and delete waypoints
          <dark_gray>» </dark_gray><gray><warm>/pathvisualizer</warm> - Compass, particles, placeholders and more
          <dark_gray>» </dark_gray><gray><warm>/gps</warm> - Find the shortest way to a nodegroup
          """)
      .build();

  public static final Message CMD_INCOMPLETE = new MessageBuilder("command.error.incomplete.title")
      .withDefault("<red>Your command is incomplete. Use one of the syntaxes below:")
      .build();
  public static final Message CMD_INCOMPLETE_LINE = new MessageBuilder("command.error.incomplete.line")
      .withDefault("<dark_gray>» </dark_gray><cmd>")
      .withPlaceholders("cmd")
      .build();

  public static final Message INFO = new MessageBuilder("commands.info")
      .withDefault("""
          <gradient:black:dark_gray:black>------------ <offset>Pathfinder</offset> ------------</gradient>
          <dark_gray>» </dark_gray><gray>Authors: <warm><authors></warm>
          <dark_gray>» </dark_gray><gray>Current version: <warm><version></warm>
          <dark_gray>» </dark_gray><gray>Main Game version: <warm><api-version></warm>
          <dark_gray>» </dark_gray><gray>Spigot-Page: <warm><u><click:open_url:"https://www.spigotmc.org/resources/gps-pathfinder-minecraft-pathfinding-tool.104961/">https://www.spigotmc.org/...</click></u></warm>
          """)
      .withPlaceholders("authors", "version", "api-version")
      .build();

  public static final Message MODULES = new MessageBuilder("commands.modules")
      .withDefault("""
          <offset>Active Modules:</offset>
          <modules:"\n":"<dark_gray>» </dark_gray>">""")
      .withPlaceholder("modules")
      .build();

  public static final Message CMD_RM_FORCE_FIND = new MessageBuilder("commands.roadmap.force_find")
      .withDefault("<msg:prefix>Player <name> discovered <discovery>.")
      .withPlaceholder("name", "discovery")
      .build();
  public static final Message CMD_RM_FORCE_FORGET = new MessageBuilder("commands.roadmap.force_forget")
      .withDefault("<msg:prefix>Player <name> forgot about <discovery>.")
      .withPlaceholders("name", "discovery")
      .build();

  public static final Message CMD_N_CREATE = new MessageBuilder("commands.node.create")
      .withDefault("<msg:prefix>Successfully created Node #<id>.")
      .withTranslation(Locale.GERMAN, "<msg:prefix>Wegpunkt #<id> erfolgreich erstellt.")
      .withPlaceholders("id")
      .build();
  public static final Message CMD_N_DELETE = new MessageBuilder("commands.node.delete")
      .withDefault("<msg:prefix>Successfully deleted <selection>.")
      .withPlaceholders("selection")
      .build();
  public static final Message CMD_N_UPDATED = new MessageBuilder("commands.node.moved")
      .withDefault("<msg:prefix>Updated <selection>.")
      .withPlaceholders("selection", "location")
      .build();
  public static final Message CMD_N_INFO = new MessageBuilder("commands.node.info")
      .withDefault("""
          <offset>Node #<id></offset> <gray>(<roadmap>)</gray>
          <dark_gray>» </dark_gray><gray>Position: <main><position></main> (<world>)
          <dark_gray>» </dark_gray><gray>Curve-Length: <main><curve-length></main>
          <dark_gray>» </dark_gray><gray>Edges: <edges>
          <dark_gray>» </dark_gray><gray>Groups: <groups>
          """)
      .withPlaceholders("id", "roadmap", "groups", "position", "world", "curve-length", "edges")
      .build();
  public static final Message CMD_N_INFO_NO_SEL = new MessageBuilder("commands.node.info_no_selection")
      .withDefault("<negative>No nodes found to display. Check your selection query.</negative>")
      .build();
  public static final Message CMD_N_ADD_GROUP = new MessageBuilder("commands.node.add_group")
      .withDefault("<msg:prefix>Added <nodes> to group <group>.")
      .withPlaceholders("nodes", "group")
      .build();
  public static final Message CMD_N_REMOVE_GROUP = new MessageBuilder("commands.node.remove_groups")
      .withDefault("<msg:prefix>Removed <nodes> from group <group>.")
      .withPlaceholders("nodes", "group")
      .build();
  public static final Message CMD_N_CLEAR_GROUPS = new MessageBuilder("commands.node.clear_groups")
      .withDefault("<msg:prefix>Cleared all groups for <nodes>.")
      .withPlaceholders("nodes")
      .build();

  public static final Message CMD_N_LIST_HEADER = new MessageBuilder("commands.node.list.header")
      .withDefault("<gradient:black:dark_gray:black>------------ <offset>Waypoints</offset> ------------</gradient>")
      .withPlaceholders("roadmap-key", "roadmap-name", "page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_N_LIST_ELEMENT = new MessageBuilder("commands.node.list.element")
      .withDefault("<dark_gray>» </dark_gray><hover:show_text:'<gray>Groups: <groups><newline><gray>Edges to: <edges><newline><gray>Click for more information'><click:run_command:/waypoint info \"@n[id=<id>]\"><gray>#<id> at <position> (<world>)")
      .withPlaceholders("id", "position", "world", "curve-length", "edges", "groups")
      .build();
  public static final Message CMD_N_LIST_FOOTER = new MessageBuilder("commands.node.list.footer")
      .withDefault("<gradient:black:dark_gray:black>------------<gray> <click:run_command:/waypoint list \"<selector>\" <prev-page>>←</click> <page>/<pages> <click:run_command:/waypoint list \"<selector>\" <next-page>>→</click> </gray>-------------</gradient>")
      .withPlaceholders("roadmap-key", "roadmap-name", "page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_N_CONNECT = new MessageBuilder("commands.node.connect.success")
      .withDefault("<msg:prefix>Connected <start> to <end>.")
      .withPlaceholders("start", "end")
      .build();
  public static final Message CMD_N_DISCONNECT = new MessageBuilder("commands.node.disconnect.success")
      .withDefault("<msg:prefix>Disconnected <start> from <end>.")
      .withPlaceholders("start", "end")
      .build();

  public static final Message CMD_NG_CREATE_FAIL = new MessageBuilder("commands.node_group.create_fail")
      .withDefault("<negative>Could not create Nodegroup. Check out console for details.")
      .build();
  public static final Message CMD_NG_ALREADY_EXISTS = new MessageBuilder("commands.node_group.already_exists")
      .withDefault("<negative>A node group with this namespaced key (<name>) already exists.</negative>")
      .withPlaceholders("name")
      .build();
  public static final Message CMD_NG_CREATE = new MessageBuilder("commands.node_group.create")
      .withDefault("<msg:prefix>Node group created: <name>.")
      .withPlaceholders("name")
      .build();
  public static final Message CMD_NG_DELETE = new MessageBuilder("commands.node_group.delete")
      .withDefault("<msg:prefix>Node group deleted: <name>.")
      .withPlaceholders("name")
      .build();
  public static final Message CMD_NG_DELETE_GLOBAL = new MessageBuilder("commands_node_group.delete_fail_global")
      .withDefault("<negative>You cannot delete the global node group.</negative>")
      .withTranslation(Locale.GERMAN, "<negative>Du kannst die globale Wegpunktgruppe nicht löschen.</negative>")
      .withComment("Indicates, that the global nodegroup cannot be deleted by command.")
      .build();
  public static final Message CMD_NG_INFO = new MessageBuilder("commands.node_group.info")
      .withDefault("""
          <offset>Group '<key>'</offset>
          <dark_gray>» </dark_gray><gray>Nodes: <main><nodes></main>
          <dark_gray>» </dark_gray><gray>Weight: <main><weight></main>
          <modifiers:"\n":"<dark_gray>» </dark_gray><gray>"/>
          """)
      .withPlaceholders("modifiers", "key", "nodes", "weight")
      .build();
  public static final Message CMD_NG_LIST_HEADER = new MessageBuilder("commands.node_group.list.header")
      .withDefault("<gradient:black:dark_gray:black>------------ <offset>Node-Groups</offset> ------------</gradient>")
      .withPlaceholders("page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_NG_LIST_LINE = new MessageBuilder("commands.node_group.list.line")
      .withDefault("<dark_gray> » </dark_gray><name> <gray>(<key>)</gray>")
      .withPlaceholders("page", "key", "name", "size", "discoverable")
      .build();
  public static final Message CMD_NG_LIST_FOOTER = new MessageBuilder("commands.node_group.list.footer")
      .withDefault("<gradient:black:dark_gray:black>------------<gray> <click:run_command:/nodegroup list <prev-page>>←</click> <page>/<pages> <click:run_command:/nodegroup list <next-page>>→</click> </gray>-------------</gradient>")
      .withPlaceholders("page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_NG_MODIFY_SET = new MessageBuilder("commands.node_group.modify.set")
      .withDefault("<msg:prefix>Added modifier '<type>' to froup '<group>'.")
      .withPlaceholders("type", "group")
      .build();
  public static final Message CMD_NG_MODIFY_REMOVE = new MessageBuilder("commands.node_group.modify.remove")
      .withDefault("<msg:prefix>Removed modifier '<type>' from group '<group>'.")
      .withPlaceholders("type", "group")
      .build();
  public static final Message CMD_NG_MOD_CURVELEN = new MessageBuilder("commands.node_group.modifier.curvelength")
      .withDefault("Curve length: <length:#.##>")
      .withPlaceholder("length", "Use java number formatting to provide custom formatting.")
      .build();
  public static final Message CMD_NG_MOD_DISCOVER = new MessageBuilder("commands.node_group.modifier.discoverable")
      .withDefault("Discover as: <name>")
      .withPlaceholder("name", "The name that is being shown when discovering this group.")
      .build();
  public static final Message CMD_NG_MOD_FINDDIST = new MessageBuilder("commands.node_group.modifier.finddistance")
      .withDefault("Find distance: <distance:#.##>")
      .withPlaceholder("distance", "Use java number formatting to provide custom formatting.")
      .build();
  public static final Message CMD_NG_MOD_SEARCH = new MessageBuilder("commands.node_group.modifier.navigable")
      .withDefault("Search terms: <terms:\"<gray>, </gray>\">")
      .withPlaceholder("terms", "A list tag for all search terms, use <terms:between:beforeeach>")
      .build();
  public static final Message CMD_NG_MOD_PERM = new MessageBuilder("commands.node_group.modifier.permission")
      .withDefault("Permission: <permission>")
      .withPlaceholder("permission")
      .build();
  public static final Message CMD_NG_MOD_VIS = new MessageBuilder("commands.node_group.modifier.visualizer")
      .withDefault("Visualizer: <visualizer>")
      .withPlaceholder("visualizer")
      .build();

  public static final Message CMD_FIND = new MessageBuilder("commands.find.success")
      .withDefault("<msg:prefix>Navigation started.  [ <aqua><click:run_command:/cancelpath>CANCEL</click></aqua> ]")
      .build();
  public static final Message CMD_FIND_EMPTY = new MessageBuilder("commands.find.no_nodes_found")
      .withDefault("<msg:prefix>No matching waypoints could be found.")
      .build();
  public static final Message CMD_FIND_TOO_FAR = new MessageBuilder("commands.find.too_far_away")
      .withDefault("<msg:prefix>The given location is too far away from any waypoint.")
      .build();
  public static final Message CMD_FIND_BLOCKED = new MessageBuilder("commands.find.no_path_found")
      .withDefault("<msg:prefix>No possible way could be found to reach that target.")
      .build();
  public static final Message CMD_CANCEL = new MessageBuilder("commands.cancel_path")
      .withDefault("<msg:prefix>Navigation cancelled.")
      .build();


  public static final Message CMD_VIS_LIST_HEADER = new MessageBuilder("commands.path_visualizer.list.header")
      .withDefault("<gradient:black:dark_gray:black>------------ <offset>Visualizer</offset> ------------</gradient>")
      .withPlaceholders("page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_VIS_LIST_ENTRY = new MessageBuilder("commands.path_visualizer.list.entry")
      .withDefault("<dark_gray> » </dark_gray><name> <gray>(<key>)</gray>")
      .withPlaceholders("key", "name", "world", "discoverable", "find-distance", "curve-length", "path-visualizer")
      .build();
  public static final Message CMD_VIS_LIST_FOOTER = new MessageBuilder("commands.path_visualizer.list.footer")
      .withDefault("<gradient:black:dark_gray:black>------------<gray> <click:run_command:/roadmap list <prev-page>>←</click> <page>/<pages> <click:run_command:/roadmap list <next-page>>→</click> </gray>-------------</gradient>")
      .withPlaceholders("page", "next-page", "prev-page", "pages")
      .build();
  public static final Message CMD_VIS_NO_TYPE_FOUND = new MessageBuilder("commands.path_visualizer.info.no_type")
      .withDefault("<negative>Could not show information to visualizer. Type could not be resolved.</negative>")
      .withTranslation(Locale.GERMAN, "<negative>Konnte Visualizer nicht anzeigen. Keine Typ-Information gefunden.</negative>")
      .build();
  public static final Message CMD_VIS_NO_INFO = new MessageBuilder("commands.path_visualizer.info.no_info")
      .withDefault("<negative>Could not show information to visualizer. No message layout provided.</negative>")
      .withTranslation(Locale.GERMAN, "<negative>Konnte Visualizer nicht anzeigen. Kein Nachrichtenformat gefunden.</negative>")
      .build();

  public static final Message CMD_VIS_CREATE_SUCCESS = new MessageBuilder("commands.path_visualizer.create.success")
      .withDefault("<msg:prefix>Successfully created Visualizer <offset><name></offset> (<name-format>) of type '<type>'.")
      .withPlaceholders("key", "type")
      .build();
  public static final Message CMD_VIS_NAME_EXISTS = new MessageBuilder("commands.path_visualizer.create.already_exists")
      .withDefault("<negative>Another visualizer with this name already exists.")
      .build();
  public static final Message CMD_VIS_DELETE_SUCCESS = new MessageBuilder("commands.path_visualizer.delete.success")
      .withDefault("<msg:prefix>Successfully deleted Visualizer <offset><name></offset>.")
      .withPlaceholders("key")
      .build();
  public static final Message CMD_VIS_DELETE_ERROR = new MessageBuilder("commands.path_visualizer.delete.error")
      .withDefault("<negative>An unknown error occurred while deleting a visualizer. Please check the console for more information.")
      .build();
  public static final Message CMD_VIS_SET_PROP = new MessageBuilder("commands.path_visualizer.set_property")
      .withDefault("<msg:prefix>Changed <property> for <name> from <old-value> to <value>.")
      .withPlaceholders("key", "type", "property", "value", "old-value")
      .build();
  public static final Message CMD_VIS_SET_PROP_ERROR = new MessageBuilder("commands.path_visualizer.set_property_error")
      .withDefault("<negative>Could not set property <property> for visualizer.")
      .withPlaceholders("key", "property")
      .build();
  public static final Message CMD_VIS_IMPORT_EXISTS = new MessageBuilder("commands.path_visualizer.import.already_exists")
      .withDefault("<negative>Could not import file, another visualizer with this key already exists.</negative>")
      .build();
  public static final Message CMD_VIS_IMPORT_NOT_EXISTS = new MessageBuilder("commands.path_visualizer.import.file_doesnt_exist")
      .withDefault("<negative>Could not import file, there is no example file with this name.</negative>")
      .build();
  public static final Message CMD_VIS_IMPORT_SUCCESS = new MessageBuilder("commands.path_visualizer.import.successful")
      .withDefault("<msg:prefix>Successfully imported Visualizer: <name>")
      .withPlaceholders("key")
      .build();
  public static final Message CMD_VIS_COMBINED_INFO = new MessageBuilder("commands.path_visualizer.type.combined.info")
      .withDefault("""
          <offset>Visualizer:</offset> <name> <gray>(<key>)</gray>
          <dark_gray>» </dark_gray><gray>Name: <main><hover:show_text:"Click to change name"><click:suggest_command:"/pathvisualizer edit particle <key> name"><name-format></click></hover></main>
          <dark_gray>» </dark_gray><gray>Permission: <main><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle <key> permission"><permission></click></hover></main>
          <dark_gray>» </dark_gray><gray>Children:<entries:"":"<br><dark_gray>  » </dark_gray>"/>""")
      .withPlaceholder("entries[:<separator>][:<prefix>][:<suffix>]")
      .build();
  public static final Message CMD_VIS_COMBINED_ADD = new MessageBuilder("commands.path_visualizer.type.combined.add")
      .withDefault("<msg:prefix>Added <child> as child to <visualizer>.")
      .withPlaceholders("child", "visualizer")
      .build();
  public static final Message CMD_VIS_COMBINED_REMOVE = new MessageBuilder("commands.path_visualizer.type.combined.remove")
      .withDefault("<msg:prefix>Removed <child> from children for <visualizer>.")
      .build();
  public static final Message CMD_VIS_COMBINED_CLEAR = new MessageBuilder("commands.path_visualizer.type.combined.clear")
      .withDefault("<msg:prefix>Cleared all children for <visualizer>.")
      .build();
  public static final Message CMD_VIS_INFO_PARTICLES = new MessageBuilder("commands.path_visualizer.type.particle_visualizer.info")
      .withDefault("""
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
      .withPlaceholders(
          "key", "name", "name-format", "type", "permission", "interval", "point-distance",
          "particle", "particle-steps", "amount", "speed", "offset")
      .build();


  public static final Message CMD_VIS_COMPASS_INFO = new MessageBuilder("commands.path_visualizer.type.compass.info")
      .withDefault("""
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
      .withPlaceholders("marker-north", "marker-south", "marker-east", "marker-west",
          "marker-target", "background", "color", "overlay")
      .build();

  public static final Message CMD_ADV_VIS_INFO_PARTICLES = new MessageBuilder("commands.path_visualizer.type.advanced_particle_visualizer.info")
      .withDefault("""
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
      .withPlaceholders("key", "name", "name-format", "type", "permission", "interval", "point-distance",
          "particle", "particle-steps", "amount", "speed", "offset-x", "offset-y", "offset-z",
          "path-x", "path-y", "path-z")
      .build();

  public static final Message CMD_VIS_PAPI_INFO = new MessageBuilder("commands.path_visualizer.type.placeholder_api.info")
      .withDefault("""
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
      .withPlaceholders(
          "key", "name", "name-format", "type", "permission", "interval", "format-north", "format-north-east",
          "format-east", "format-south-east", "format-south", "format-south-west", "format-west", "format-north-west",
          "format-distance"
      )
      .build();

  public static final Message E_NODE_TOOL_N = new MessageBuilder("editor.toolbar.node_tool.name")
      .withDefault("<white><u>Node Tool</u></white>")
      .build();
  public static final Message E_NODE_TOOL_L = new MessageBuilder("editor.toolbar.node_tool.lore")
      .withDefault("""
          <gray>» <yellow>right-click:</yellow> Create node</gray>
          <gray>» <yellow>left-click:</yellow> Delete clicked node</gray>
          <gray>» <yellow>left-click air:</yellow> Activate chain mode</gray>""")
      .build();
  public static final Message E_NODE_CHAIN_ON = new MessageBuilder("editor.node_tool.chain.on")
      .withDefault("<msg:prefix>Chain mode activated. A new node is connected to the latter.")
      .build();
  public static final Message E_NODE_CHAIN_OFF = new MessageBuilder("editor.node_tool.chain.off")
      .withDefault("<msg:prefix>Chain mode cancelled.")
      .build();
  public static final Message E_NODE_CHAIN_NEW = new MessageBuilder("editor.node_tool.chain.new")
      .withDefault("<msg:prefix>New chain started - left-click again to turn off chain mode")
      .build();
  public static final Message E_NODE_CHAIN_START = new MessageBuilder("editor.node_tool.chain.new_start")
      .withDefault("<msg:prefix>Chain start point set.")
      .build();
  public static final Message E_EDGE_TOOL_N = new MessageBuilder("editor.toolbar.edge_tool.name")
      .withDefault("<white><u>Edge Tool</u></white>")
      .build();
  public static final Message E_EDGE_TOOL_L = new MessageBuilder("editor.toolbar.edge_tool.lore")
      .withDefault("""
          <gray>» <yellow>right-click node:</yellow> Connect nodes</gray>
          <gray>» <yellow>left-click node:</yellow> Disconnect all edges</gray>
          <gray>» <yellow>left-click edge:</yellow> Dissolve edge</gray>
          <gray>» <yellow>left-click air:</yellow> Toggle directed</gray>""")
      .build();
  public static final Message E_EDGE_TOOL_CANCELLED = new MessageBuilder("editor.toolbar.edge_tool.cancelled")
      .withDefault("<msg:prefix>Node connection mode cancelled")
      .build();
  public static final Message E_EDGE_TOOL_DIR_TOGGLE = new MessageBuilder("editor.toolbar.edge_tool.directed")
      .withDefault("<msg:prefix>Edges directed: <main><value><main>")
      .withPlaceholders("value")
      .build();
  public static final Message E_GROUP_TOOL_N = new MessageBuilder("editor.toolbar.group_tool.name")
      .withDefault("<white><u>Assign Group</u></white>")
      .build();
  public static final Message E_GROUP_TOOL_L = new MessageBuilder("editor.toolbar.group_tool.lore")
      .withDefault("")
      .build();
  public static final Message E_MULTI_GROUP_TOOL_N = new MessageBuilder("editor.toolbar.multi_group_tool.name")
      .withDefault("<white><u>Mutli Group Tool</u></white>")
      .build();
  public static final Message E_MULTI_GROUP_TOOL_L = new MessageBuilder("editor.toolbar.multi_group_tool.lore")
      .withDefault("""
          <gray>Assign and remove multiple
          <gray>groups at once.
                    
          <gray>» <yellow>right-click air:</yellow> Open GUI</gray>
          <gray>» <yellow>right-click node:</yellow> Add groups</gray>
          <gray>» <yellow>right-click node:</yellow> Remove groups</gray>""")
      .build();
  public static final Message E_TP_TOOL_N = new MessageBuilder("editor.toolbar.teleport_tool.name")
      .withDefault("<white><u>Teleport Tool</u></white>")
      .build();
  public static final Message E_TP_TOOL_L = new MessageBuilder("editor.toolbar.teleport_tool.lore")
      .withDefault("<gray>Teleports you to the\n<gray>nearest node.")
      .build();
  public static final Message E_SUB_GROUP_TITLE = new MessageBuilder("editor.groups.title")
      .withDefault("Assign Node Groups")
      .build();
  public static final Message E_SUB_GROUP_INFO_N = new MessageBuilder("editor.groups.info.name")
      .withDefault("<gold>Info</gold>")
      .build();
  public static final Message E_SUB_GROUP_INFO_L = new MessageBuilder("editor.groups.info.lore")
      .withDefault("<gray>Create a new nodegroup with\n<gray>» <yellow>/nodegroup create <key>")
      .build();
  public static final Message E_SUB_GROUP_RESET_N = new MessageBuilder("editor.groups.reset.name")
      .withDefault("<negative>Reset Groups</negative>")
      .build();
  public static final Message E_SUB_GROUP_RESET_L = new MessageBuilder("editor.groups.reset.lore")
      .withDefault("<gray>Reset all groups for the\n<gray>selected node.")
      .build();
  public static final Message E_SUB_GROUP_ENTRY_N = new MessageBuilder("editor.groups.entry.name")
      .withDefault("<name>")
      .withPlaceholders("key", "name", "name-format", "discoverable", "search-terms")
      .build();
  public static final Message E_SUB_GROUP_ENTRY_L = new MessageBuilder("editor.groups.entry.lore")
      .withDefault("""
          <dark_gray>» </dark_gray><gray>Key: <key></gray>
          <dark_gray>» </dark_gray><gray>Name: <name-format></gray>
          <dark_gray>» </dark_gray><gray>Permission: <permission></gray>
          <dark_gray>» </dark_gray><gray>Navigable: <navigable></gray>
          <dark_gray>» </dark_gray><gray>Discoverable: <discoverable></gray>
          <dark_gray>» </dark_gray><gray>Find distance: <find-distance:#.##></gray>
          <dark_gray>» </dark_gray><gray>Search terms: <search-terms></gray>""")
      .withPlaceholders("key", "name", "name-format", "permission",
          "navigable", "discoverable", "find-distance", "search-terms")
      .build();
  public static final Message TARGET_FOUND = new MessageBuilder("general.target_reached")
      .withDefault("<msg:prefix>Target reached.")
      .build();

  public static final Message EDITM_NG_DELETED = new MessageBuilder("editmode.group_deleted")
      .withDefault("<negative>Your currently edited group was deleted by another user.")
      .build();

  @Setter
  private static AudienceProvider audiences;

  private static Audience audienceSender(CommandSender sender) {
    return sender instanceof Player player
        ? audiences.player(player.getUniqueId())
        : audiences.console();
  }

  public static Message formatBool(boolean val) {
    return val ? GEN_TRUE : GEN_FALSE;
  }

  public static Component formatNodeSelection(CommandSender sender, Collection<Node> nodes) {
    return formatGroupInHover(sender, GEN_NODE_SEL, nodes, node -> formatNode(sender, node));
  }

  public static Component formatNode(CommandSender sender, Node node) {
    return GEN_NODE.formatted(
        Placeholder.parsed("world", node.getLocation().getWorld().getName()),
        Placeholder.component("location", formatVector(node.getLocation().asVector()))
    ).asComponent(audienceSender(sender));
  }

  public static Component formatNodeGroups(CommandSender sender, Collection<SimpleNodeGroup> groups) {
    return formatGroupInHover(sender, GEN_GROUP_SEL, groups, g -> Component.text(g.getKey().toString()));
  }

  public static TagResolver formatModifiers(String key, Collection<Modifier> modifiers) {
    return TagResolver.resolver(key, Messages.formatList(modifiers, modifier -> {
      Optional<ModifierType<Modifier>> type = PathFinderProvider.get().getModifierRegistry().getType(modifier.getKey());
      return type.isPresent() && type.get() instanceof ModifierCommandExtension ext
          ? ext.toComponents(modifier)
          : Component.text("Unknown modifier '" + modifier.getKey() + "'.");
    }));
  }

  public static Component formatThrowable(Throwable throwable) {
    return Component.text(throwable.getMessage(), NamedTextColor.RED);
  }

  public static <T> Component formatGroupConcat(CommandSender sender, Message placeHolder,
                                                Collection<T> collection,
                                                Function<T, ComponentLike> converter) {
    return placeHolder.formatted(TagResolver.resolver(
        Placeholder.unparsed("amount", collection.size() + ""),
        Placeholder.component("list",
            Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
                collection.stream().map(converter).collect(Collectors.toList())))
    )).asComponent(audienceSender(sender));
  }

  public static <T> Component formatGroupInHover(CommandSender sender, Message placeHolder,
                                                 Collection<T> collection,
                                                 Function<T, ComponentLike> converter) {
    return placeHolder.formatted(Placeholder.unparsed("amount", collection.size() + ""))
        .asComponent(audienceSender(sender))
        .hoverEvent(HoverEvent.showText(
            Component.join(JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
                collection.stream().map(converter).collect(Collectors.toList()))));
  }

  public static Message formatParticle(Particle particle, Object data) {
    return data == null
        ? GEN_PARTICLE.formatted(TagResolver.builder()
        .resolver(Placeholder.component("particle", Component.text(particle.toString())))
        .build())
        : GEN_PARTICLE_META.formatted(TagResolver.builder()
        .resolver(Placeholder.component("particle", Component.text(particle.toString())))
        .resolver(Placeholder.component("meta", Component.text(data.toString())))
        .build());
  }

  public static Message formatVector(Vector vector) {
    return GEN_VECTOR.formatted(TagResolver.builder()
        .resolver(Formatter.number("x", vector.getX()))
        .resolver(Formatter.number("y", vector.getY()))
        .resolver(Formatter.number("z", vector.getZ()))
        .build());
  }

  public static Message formatPermission(@Nullable String permission) {
    return permission == null
        ? GEN_NULL.clone()
        : GEN_PERMISSION.formatted(TagResolver.resolver("permission", Tag.inserting(Component.text(permission))));
  }

  public static <T> BiFunction<ArgumentQueue, Context, Tag> formatList(Collection<T> entries,
                                                                       Function<T, ComponentLike> formatter) {
    Collection<ComponentLike> componentLikes = new ArrayList<>();
    for (T entry : entries) {
      componentLikes.add(formatter.apply(entry));
    }
    return formatList(componentLikes);
  }

  public static <T extends ComponentLike> BiFunction<ArgumentQueue, Context, Tag> formatList(Collection<T> entries) {
    return (queue, context) -> {
      MiniMessage mm = PathFinderProvider.get().getMiniMessage();
      ComponentLike separator = Component.text(", ", NamedTextColor.GRAY);
      ComponentLike prefix = null;
      ComponentLike suffix = null;

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
      return Tag.selfClosingInserting(Component.join(JoinConfiguration.builder().separator(separator).build(),
          entries.stream().map(c -> {
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
        return Tag.selfClosingInserting(GEN_NULL.clone());
      }

      TextColor namespaceColor;
      TextColor keyColor;

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
