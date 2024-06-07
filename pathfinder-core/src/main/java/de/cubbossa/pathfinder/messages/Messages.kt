package de.cubbossa.pathfinder.messages

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathFinderProvider
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.misc.Vector
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeSelection
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.tinytranslations.Formattable
import de.cubbossa.tinytranslations.Message
import de.cubbossa.tinytranslations.MessageBuilder
import de.cubbossa.tinytranslations.MessageTranslator
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Keyed
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import java.util.ArrayList
import java.util.Locale
import java.util.function.Function

class Messages {
    fun particle(key: String?, particle: Particle, data: Any?): TagResolver {
        return Placeholder.component(
            key!!, if (data == null
            ) GEN_PARTICLE.formatted(
                TagResolver.builder()
                    .resolver(Placeholder.component("particle", Component.text(particle.toString())))
                    .build()
            )
            else GEN_PARTICLE_META.formatted(
                TagResolver.builder()
                    .resolver(Placeholder.component("particle", Component.text(particle.toString())))
                    .resolver(Placeholder.component("meta", Component.text(data.toString())))
                    .build()
            )
        )
    }

    companion object {
        val PREFIX: Message = MessageBuilder("prefix")
            .withDefault("PathFinder")
            .build()
        val GEN_TOO_FAST: Message = MessageBuilder("general.response_pending")
            .withDefault("<prefix>Better slow down, your database is out of breath.")
            .build()
        val GEN_ERROR: Message = MessageBuilder("general.error")
            .withDefault("<prefix_negative>{error:cause}</prefix_negative>")
            .withPlaceholders("error")
            .build()
        val GEN_LOCATION: Message = MessageBuilder("general.location")
            .withDefault("\\<{loc:x};{loc:y};{loc:z}>")
            .withPlaceholders("loc")
            .build()
        val GEN_PARTICLE: Message = MessageBuilder("general.particle")
            .withDefault("<text_hl>{particle}</text_hl>")
            .withPlaceholders("particle", "meta")
            .build()
        val GEN_PARTICLE_META: Message = MessageBuilder("general.particle")
            .withDefault("<text_hl>{particle} <text>({meta})</text></text_hl>")
            .withPlaceholders("particle", "meta")
            .build()

        val GEN_NODE: Message = MessageBuilder("general.node")
            .withDefault("({node:loc:world}; {node:loc})")
            .withPlaceholders("node")
            .build()
        val GEN_NODE_SEL: Message = MessageBuilder("general.selection.nodes")
            .withDefault("<hover:show_text:\"<sel:content:', '>{el}</sel>\"><white><u>{nsel:size} Node{nsel:size ? '' : 's'}</u></white></hover>")
            .withPlaceholders("nsel")
            .build()
        val RELOAD_ERROR: Message = MessageBuilder("command.reload.error")
            .withDefault("<prefix_negative>An error occurred while reloading: {error}</prefix_negative>")
            .withPlaceholder("error")
            .build()
        val RELOAD_SUCCESS: Message = MessageBuilder("command.reload.success.general")
            .withDefault("<prefix>Successfully reloaded in <offset_l>{ms}</offset_l><offset>ms</offset>.")
            .withPlaceholders("ms")
            .build()
        val RELOAD_SUCCESS_LANG: Message = MessageBuilder("command.reload.success.language")
            .withDefault("<prefix>Successfully reloaded language in <offset_l>{ms}</offset_l><offset>ms</offset>.")
            .withPlaceholders("ms")
            .build()
        val RELOAD_SUCCESS_FX: Message = MessageBuilder("command.reload.success.effects")
            .withDefault("<prefix>Successfully reloaded effects in <offset_l>{ms}</offset_l><offset>ms</offset>.")
            .withPlaceholders("ms")
            .build()
        val RELOAD_SUCCESS_CFG: Message = MessageBuilder("command.reload.success.config")
            .withDefault("<prefix>Successfully reloaded config files in <offset_l>{ms}</offset_l><offset>ms</offset>.")
            .withPlaceholders("ms")
            .build()

        val HELP: Message = MessageBuilder("general.help")
            .withDefault(
                """
          <header>PathFinder</header>
          <text>Running <offset>Pathfinder v{version}</offset>.
                
          <text>Require help? Checkout the <text_hl><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></text_hl>.
          <text>Use <text_hl>/pf help</text_hl> to view available commands.
          
          """.trimIndent()
            )
            .withPlaceholder("version")
            .build()

        val STATE_NOT_RUNNING: Message = MessageBuilder("general.state.not_running")
            .withDefault("<prefix_negative>Pathfinder is not loaded.</prefix_negative>")
            .build()
        val STATE_EDIT: Message = MessageBuilder("general.state.street_map_edited")
            .withDefault("<prefix_negative>The street map is currently being edited by an administrator.</prefix_negative>")
            .build()

        val CMD_DUMP_SUCCESS: Message = MessageBuilder("command.createdump.success")
            .withDefault("<prefix>Dump file successfully created in plugin directory.")
            .build()
        val CMD_DUMP_FAIL: Message = MessageBuilder("command.createdump.failure")
            .withDefault("<prefix_negative>Dump file could not be created. Check console for details.</prefix_negative>")
            .build()
        val CMD_HELP: Message = MessageBuilder("command.help")
            .withDefault(
                """
          <header>PathFinder</header>
          <text>Require help? Checkout the <text_hl><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></text_hl>.
                    
          <white>Commands:</white>
          <bg>» </bg><text><text_hl>/pf editmode</text_hl> - Create, edit and delete waypoints via GUI
          <bg>» </bg><text><text_hl>/pf node</text_hl> - Create, edit and delete waypoints via Commands
          <bg>» </bg><text><text_hl>/pf group</text_hl> - Add behaviour to multiple waypoints
          <bg>» </bg><text><text_hl>/pf visualizer</text_hl> - Compass, particles, placeholders and more
          <bg>» </bg><text><text_hl>/find</text_hl> - Find the shortest way to a nodegroup
          
          """.trimIndent()
            )
            .build()

        val CMD_INCOMPLETE: Message = MessageBuilder("command.error.incomplete.title")
            .withDefault("<negative>Your command is incomplete. Use one of the syntaxes below:")
            .build()
        val CMD_INCOMPLETE_LINE: Message = MessageBuilder("command.error.incomplete.line")
            .withDefault("<bg>» </bg>{cmd}")
            .withPlaceholders("cmd")
            .build()

        val INFO: Message = MessageBuilder("commands.info")
            .withDefault(
                """
          <header>PathFinder</header>
          <bg>» </bg><text>Current version: <text_hl>{version}</text_hl>
          <bg>» </bg><text>Spigot-Page: <url>https://www.spigotmc.org/resources/gps-pathfinder-minecraft-pathfinding-tool.104961/</url>
          
          """.trimIndent()
            )
            .withPlaceholders("authors", "version", "api-version")
            .build()

        val MODULES: Message = MessageBuilder("commands.modules")
            .withDefault(
                """
          <offset>Active Modules:</offset>
          <modules:"<newline>":"<bg>» </bg>">
          """.trimIndent()
            )
            .withPlaceholder("modules")
            .build()

        val CMD_FORCE_FIND: Message = MessageBuilder("commands.force_find")
            .withDefault("<prefix>Player {name} discovered <text_hl>{discovery}</text_hl>.")
            .withPlaceholder("name", "discovery")
            .build()
        val CMD_FORCE_FORGET: Message = MessageBuilder("commands.force_forget")
            .withDefault("<prefix>Player {name} forgot about <text_hl>{discovery}</text_hl>.")
            .withPlaceholders("name", "discovery")
            .build()

        val CMD_N_CREATE: Message = MessageBuilder("commands.node.create")
            .withDefault("<prefix>Successfully created Node #{node:id}.")
            .withTranslation(Locale.GERMAN, "<prefix>Wegpunkt #{node:id} erfolgreich erstellt.")
            .withPlaceholders("node")
            .build()
        val CMD_N_DELETE: Message = MessageBuilder("commands.node.delete")
            .withDefault("<prefix>Successfully deleted {nodes}.")
            .withPlaceholders("selection")
            .build()
        val CMD_N_UPDATED: Message = MessageBuilder("commands.node.moved")
            .withDefault("<prefix>Updated {selection}.")
            .withPlaceholders("selection", "location")
            .build()
        val CMD_N_INFO: Message = MessageBuilder("commands.node.info")
            .withDefault(
                """
          <offset>Node #{node:id}</offset>
          <bg>» </bg><text>Position: <text_hl>{node:loc}</text_hl> (<world>)
          <bg>» </bg><text>Curve-Length: <text_hl>{node:curve-length}</text_hl>
          <bg>» </bg><text>Edges: {node:edges}
          <bg>» </bg><text>Groups: {node:groups}
          
          """.trimIndent()
            )
            .withPlaceholders("node")
            .build()
        val CMD_N_INFO_NO_SEL: Message = MessageBuilder("commands.node.info_no_selection")
            .withDefault("<prefix_negative>No nodes found to display. Check your selection query.</prefix_negative>")
            .build()
        val CMD_N_ADD_GROUP: Message = MessageBuilder("commands.node.add_group")
            .withDefault("<prefix>Added {nodes} to group <text_hl>{group}</text_hl>.")
            .withPlaceholders("nodes", "group")
            .build()
        val CMD_N_REMOVE_GROUP: Message = MessageBuilder("commands.node.remove_groups")
            .withDefault("<prefix>Removed {nodes} from group <text_hl>{group}</text_hl>.")
            .withPlaceholders("nodes", "group")
            .build()
        val CMD_N_CLEAR_GROUPS: Message = MessageBuilder("commands.node.clear_groups")
            .withDefault("<prefix>Cleared all groups for {nodes}.")
            .withPlaceholders("nodes")
            .build()

        val CMD_N_LIST: Message = MessageBuilder("commands.node.list")
            .withDefault(
                """
          <header>Waypoints</header>
          <nodes:'<newline>'><bg>» </bg><hover:show_text:'<text>Groups: {el:groups}<newline><text>Edges to: {el:edges}<newline><text>Click for more information'><click:run_command:'/pf nodes ${'"'}@n[id={el:id}]${'"'} info'><text>at {el:loc}({el:loc:world})</nodes>
          <list_static_footer:'/pf listnodes "{selector}"'></list_static_footer>
          """.trimIndent()
            )
            .withPlaceholders("page", "next-page", "prev-page", "pages")
            .build()
        val CMD_N_CONNECT: Message = MessageBuilder("commands.node.connect.success")
            .withDefault("<prefix>Connected {start} to {end}.")
            .withPlaceholders("start", "end")
            .build()
        val CMD_N_DISCONNECT: Message = MessageBuilder("commands.node.disconnect.success")
            .withDefault("<prefix>Disconnected {start} from {end}.")
            .withPlaceholders("start", "end")
            .build()

        val CMD_NG_CREATE_FAIL: Message = MessageBuilder("commands.node_group.create_fail")
            .withDefault("<prefix_negative>Could not create Nodegroup. Check out console for details.")
            .build()
        val CMD_NG_ALREADY_EXISTS: Message = MessageBuilder("commands.node_group.already_exists")
            .withDefault("<prefix_negative>A node group {input} already exists.</prefix_negative>")
            .withPlaceholders("input")
            .build()
        val CMD_NG_CREATE: Message = MessageBuilder("commands.node_group.create")
            .withDefault("<prefix>Node group <text_hl>{group:key}</text_hl> created.")
            .withPlaceholders("group")
            .build()
        val CMD_NG_DELETE: Message = MessageBuilder("commands.node_group.delete")
            .withDefault("<prefix>Node group <text_hl>{group:key}</text_hl> deleted.")
            .withPlaceholders("group")
            .build()
        val CMD_NG_DELETE_GLOBAL: Message = MessageBuilder("commands_node_group.delete_fail_global")
            .withDefault("<prefix_negative>You cannot delete the global node group.</prefix_negative>")
            .withTranslation(
                Locale.GERMAN,
                "<prefix_negative>Du kannst die globale Wegpunktgruppe nicht löschen.</prefix_negative>"
            )
            .withComment("Indicates, that the global nodegroup cannot be deleted by command.")
            .build()
        val CMD_NG_INFO: Message = MessageBuilder("commands.node_group.info")
            .withDefault(
                """
          <primary_l>Group '{group:key}'</primary_l>
          <bg>» </bg><text>Size: {group:nodes}
          <bg>» </bg><text>Weight: <text_hl>{group:weight}</text_hl>
          <group:modifiers:'
          '><bg>» </bg><text>{el}</text></group>
          
          """.trimIndent()
            )
            .withPlaceholders("group")
            .build()
        val CMD_NG_LIST: Message = MessageBuilder("commands.node_group.list.line")
            .withDefault(
                """
          <header>Node-Groups</header>
          <groups:"
          "><bg> » </bg><text_l>{el:key} </text_l><text>(Weight: {el:weight})</text></groups>
          <gradient:black:dark_gray:black>------------<text> <click:run_command:"/pf listgroups {prev-page}">←</click> {page}/{pages} <click:run_command:"/pf listgroups {next-page}">→</click></text> -------------</gradient>
          """.trimIndent()
            )
            .withPlaceholders("page", "key", "weight", "modifiers")
            .build()
        val CMD_NG_MODIFY_SET: Message = MessageBuilder("commands.node_group.modify.set")
            .withDefault("<prefix>Added modifier <text_hl>{mod:key}</text_hl> to group <text_hl>{group:key}</text_hl>.")
            .withPlaceholders("mod", "group")
            .build()
        val CMD_NG_MODIFY_REMOVE: Message = MessageBuilder("commands.node_group.modify.remove")
            .withDefault("<prefix>Removed modifier <text_hl>{mod:key}<text_hl> from group <text_hl>{group:key}</text_hl>.")
            .withPlaceholders("mod", "group")
            .build()
        val CMD_NG_MOD_CURVELEN: Message = MessageBuilder("commands.node_group.modifier.curvelength")
            .withDefault("Curve length: {length:#.##}")
            .withPlaceholder("length", "Use java number formatting to provide custom formatting.")
            .build()
        val CMD_NG_MOD_DISCOVER: Message = MessageBuilder("commands.node_group.modifier.discoverable")
            .withDefault("Discover as: {name}")
            .withPlaceholder("name", "The name that is being shown when discovering this group.")
            .build()
        val CMD_NG_MOD_DISCOVERIES: Message = MessageBuilder("commands.node_group.modifier.discover-progress")
            .withDefault("Discover progress as: {name}")
            .withPlaceholder("name", "The name that is being shown when running /discoveries.")
            .build()
        val CMD_NG_MOD_FINDDIST: Message = MessageBuilder("commands.node_group.modifier.finddistance")
            .withDefault("Find distance: {distance:#.##}")
            .withPlaceholder("distance", "Use java number formatting to provide custom formatting.")
            .build()
        val CMD_NG_MOD_SEARCH: Message = MessageBuilder("commands.node_group.modifier.navigable")
            .withDefault("Search terms: {terms:'<text>, </text>'}")
            .withPlaceholder("terms", "A list tag for all search terms, use <terms:between:beforeeach>")
            .build()
        val CMD_NG_MOD_PERM: Message = MessageBuilder("commands.node_group.modifier.permission")
            .withDefault("Permission: {permission}")
            .withPlaceholder("permission")
            .build()
        val CMD_NG_MOD_VIS: Message = MessageBuilder("commands.node_group.modifier.visualizer")
            .withDefault("Visualizer: {visualizer}")
            .withPlaceholder("visualizer")
            .build()

        val CMD_FIND: Message = MessageBuilder("commands.find.success")
            .withDefault("<prefix>Navigation started. <button><click:run_command:/cancelpath>/cancelpath</click></button>")
            .build()
        val CMD_DISCOVERIES_ENTRY: Message = MessageBuilder("commands.discoveries.list.entry")
            .withDefault(
                """
          <header>Discoveries</header>
          <discoveries><bg>» </bg>{el:name}: {el:percentage:#.##}%</discoveries>
          {static_list_footer:'/discoveries'}
          """.trimIndent()
            )
            .withPlaceholders("name", "percentage", "ratio")
            .build()
        val CMD_FIND_EMPTY: Message = MessageBuilder("commands.find.no_nodes_found")
            .withDefault("<prefix>No matching waypoints could be found.")
            .build()
        val CMD_FIND_TOO_FAR: Message = MessageBuilder("commands.find.too_far_away")
            .withDefault("<prefix>The given location is too far away from any waypoint.")
            .build()
        val CMD_FIND_BLOCKED: Message = MessageBuilder("commands.find.no_path_found")
            .withDefault("<prefix>No possible way could be found to reach that target.")
            .build()
        val CMD_FIND_UNKNOWN: Message = MessageBuilder("commands.find.unknown_error")
            .withDefault("<prefix_negative>An unknown error occurred.</prefix_negative>")
            .build()
        val CMD_CANCEL: Message = MessageBuilder("commands.cancel_path")
            .withDefault("<prefix>Navigation cancelled.")
            .build()
        val CMD_FINDP_OFFLINE: Message = MessageBuilder("commands.find_player.target_offline")
            .withDefault("<prefix_negative>Player not found.</prefix_negative>")
            .build()
        val CMD_FINDP_NO_SELF: Message = MessageBuilder("commands.find_player.no_requests_to_self")
            .withDefault("<prefix_negative>You cannot make requests to yourself.</prefix_negative>")
            .build()
        val CMD_FINDP_NO_REQ: Message = MessageBuilder("commands.find_player.no_requests")
            .withDefault("<prefix_negative>No requests found.</prefix_negative>")
            .build()
        val CMD_FINDP_ALREADY_REQ: Message = MessageBuilder("commands.find_player.already_requested")
            .withDefault("<prefix_negative>Navigation already requested.</prefix_negative>")
            .build()
        val CMD_FINDP_REQUEST: Message = MessageBuilder("commands.find_player.request")
            .withDefault("<prefix>Made a request to navigate to <text_hl>{target}</text_hl>.")
            .withPlaceholders("requester", "target")
            .build()
        val CMD_FINDP_REQUESTED: Message = MessageBuilder("commands.find_player.requested")
            .withDefault("<prefix><text_hl>{requester}</text_hl> asked to navigate to you. <button><positive><click:run_command:'/fpaccept {requester}'>accept</click></positive></button>  <button><negative><click:run_command:'/fpdecline {requester}'>decline</click></negative></button>")
            .withPlaceholders("requester", "target")
            .build()
        val CMD_FINDP_ACCEPT: Message = MessageBuilder("commands.find_player.accept")
            .withDefault("<prefix>Request accepted.")
            .withPlaceholders("requester", "target")
            .build()
        val CMD_FINDP_ACCEPTED: Message = MessageBuilder("commands.find_player.accepted")
            .withDefault("<prefix><text_hl>{target}</text_hl> accepted your navigate request.")
            .withPlaceholders("requester", "target")
            .build()
        val CMD_FINDP_DECLINE: Message = MessageBuilder("commands.find_player.decline")
            .withDefault("<prefix>Request declined.")
            .withPlaceholders("requester", "target")
            .build()
        val CMD_FINDP_DECLINED: Message = MessageBuilder("commands.find_player.declined")
            .withDefault("<prefix><text_hl>{target}</text_hl> declined your navigate request.")
            .withPlaceholders("requester", "target")
            .build()
        val CMD_FINDP_EXPIRED: Message = MessageBuilder("commands.find_player.request_expired")
            .withDefault("<prefix>Your request expired.")
            .build()
        val DISCOVERY_DISCOVER: Message = MessageBuilder("discovery.discover")
            .withDefault("You discovered: {discoverable}")
            .withTranslation(Locale.GERMAN, "Entdeckt: <discoverable>")
            .withPlaceholders("player", "discoverable", "group")
            .build()
        val DISCOVERY_PROG: Message = MessageBuilder("discovery.progress")
            .withDefault("{percentage:#.##}% of {name}")
            .withPlaceholders(
                "player",
                "discoverable",
                "group",
                "name",
                "percentage",
                "ratio",
                "count-found",
                "count-all"
            )
            .build()
        val DISCOVERY_FORGET: Message = MessageBuilder("discovery.forget")
            .withDefault("<prefix>You forgot all about {discoverable}")
            .withTranslation(Locale.GERMAN, "Du vergisst alles über <discoverable>")
            .withPlaceholders("player", "discoverable", "group")
            .build()


        val CMD_VIS_LIST_ENTRY: Message = MessageBuilder("commands.path_visualizer.list.entry")
            .withDefault(
                """
          <header>Visualizer</header>
          <visualizers:'
          '><dark_gray> » </dark_gray>{el:key} <text>({el:type})</text></visualizers>
          {footer:'/pf listvisualizers'}
          """.trimIndent()
            )
            .withPlaceholders("visualizers")
            .withPlaceholders(*Formattable.LIST_PLACEHOLDERS)
            .build()
        val CMD_VIS_NO_TYPE_FOUND: Message = MessageBuilder("commands.path_visualizer.info.no_type")
            .withDefault("<prefix_negative>Could not show information to visualizer. Type could not be resolved.</prefix_negative>")
            .withTranslation(
                Locale.GERMAN,
                "<prefix_negative>Konnte Visualizer nicht anzeigen. Keine Typ-Information gefunden.</prefix_negative>"
            )
            .build()
        val CMD_VIS_NO_INFO: Message = MessageBuilder("commands.path_visualizer.info.no_info")
            .withDefault("<prefix_negative>Could not show information to visualizer. No message layout provided.</prefix_negative>")
            .withTranslation(
                Locale.GERMAN,
                "<prefix_negative>Konnte Visualizer nicht anzeigen. Kein Nachrichtenformat gefunden.</prefix_negative>"
            )
            .build()

        val CMD_VIS_CREATE_SUCCESS: Message = MessageBuilder("commands.path_visualizer.create.success")
            .withDefault("<prefix>Successfully created Visualizer <text_hl>{visualizer:key}</text_hl> of type <text_hl>{visualizer:type}</text_hl>.")
            .withPlaceholders("visualizer")
            .build()
        val CMD_VIS_NAME_EXISTS: Message = MessageBuilder("commands.path_visualizer.create.already_exists")
            .withDefault("<prefix_negative>Another visualizer with this name already exists.")
            .build()
        val CMD_VIS_DELETE_SUCCESS: Message = MessageBuilder("commands.path_visualizer.delete.success")
            .withDefault("<prefix>Successfully deleted Visualizer <offset>{visualizer:key}</offset>.")
            .withPlaceholders("visualizer")
            .build()
        val CMD_VIS_DELETE_ERROR: Message = MessageBuilder("commands.path_visualizer.delete.error")
            .withDefault("<prefix_negative>An unknown error occurred while deleting a visualizer. Please check the console for more information.")
            .build()
        val CMD_VIS_SET_PROP: Message = MessageBuilder("commands.path_visualizer.set_property")
            .withDefault("<prefix>Changed {property} for <text_hl>{visualizer}</text_hl> from <text_hl>{old-value}</text_hl> to <text_hl>{value}</text_hl>.")
            .withPlaceholders("visualizer", "type", "property", "value", "old-value")
            .build()
        val CMD_VIS_SET_PROP_ERROR: Message = MessageBuilder("commands.path_visualizer.set_property_error")
            .withDefault("<prefix_negative>Could not set property {property} for visualizer.")
            .withPlaceholders("key", "property")
            .build()
        val CMD_VIS_IMPORT_EXISTS: Message = MessageBuilder("commands.path_visualizer.import.already_exists")
            .withDefault("<prefix_negative>Could not import file, another visualizer with this key already exists.</prefix_negative>")
            .build()
        val CMD_VIS_IMPORT_NOT_EXISTS: Message = MessageBuilder("commands.path_visualizer.import.file_doesnt_exist")
            .withDefault("<prefix_negative>Could not import file, there is no example file with this name.</prefix_negative>")
            .build()
        val CMD_VIS_IMPORT_SUCCESS: Message = MessageBuilder("commands.path_visualizer.import.successful")
            .withDefault("<prefix>Successfully imported Visualizer <text_hl>{visualizer}</text_hl>")
            .withPlaceholders("visualizer")
            .build()
        val CMD_VIS_COMBINED_INFO: Message = MessageBuilder("commands.path_visualizer.type.combined.info")
            .withDefault(
                """
          <primary_l>Visualizer: {visualizer}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle {key} permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Children:{<entries:"":"<br><bg>  » </bg>"/>}
          """.trimIndent()
            )
            .withPlaceholder("visualizer")
            .withPlaceholder("entries[:separator][:prefix][:suffix]")
            .build()
        val CMD_VIS_COMBINED_ADD: Message = MessageBuilder("commands.path_visualizer.type.combined.add")
            .withDefault("<prefix>Added {child} as child to {visualizer}.")
            .withPlaceholders("child", "visualizer")
            .build()
        val CMD_VIS_COMBINED_REMOVE: Message = MessageBuilder("commands.path_visualizer.type.combined.remove")
            .withDefault("<prefix>Removed <child> from children for {visualizer}.")
            .build()
        val CMD_VIS_COMBINED_CLEAR: Message = MessageBuilder("commands.path_visualizer.type.combined.clear")
            .withDefault("<prefix>Cleared all children for {visualizer}.")
            .build()
        val CMD_VIS_INFO_PARTICLES: Message = MessageBuilder("commands.path_visualizer.type.particle_visualizer.info")
            .withDefault(
                """
          <primary_l>Visualizer: {visualizer}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle {key} permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Interval: <text_hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit particle {key} interval">{interval}</click></hover></text_hl>
          <bg>» </bg><text>Point-Distance: <text_hl><hover:show_text:"Click to change point-distance"><click:suggest_command:"/pathvisualizer edit particle {key} point-distance">{point-distance}</click></hover></text_hl>
          <bg>» </bg><text>Particle: <text_hl><hover:show_text:"Click to change particle"><click:suggest_command:"/pathvisualizer edit particle {key} particle">{particle}</click></hover></text_hl>
          <bg>» </bg><text>Particle-Steps: <text_hl><hover:show_text:"Click to change particle-steps"><click:suggest_command:"/pathvisualizer edit particle-steps {key} particle">{particle-steps}</click></hover></text_hl>
          <bg>» </bg><text>Amount: <text_hl><hover:show_text:"Click to change amount"><click:suggest_command:"/pathvisualizer edit particle {key} particle">{amount}</click></hover></text_hl>
          <bg>» </bg><text>Speed: <text_hl><hover:show_text:"Click to change speed"><click:suggest_command:"/pathvisualizer edit particle {key} speed">{speed}</click></hover></text_hl>
          <bg>» </bg><text>Offset: <text_hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit particle {key} particle">{offset}</click></hover></text_hl>
          """.trimIndent()
            )
            .withPlaceholders(
                "visualizer", "type", "permission", "interval", "point-distance",
                "particle", "particle-steps", "amount", "speed", "offset"
            )
            .build()


        val CMD_VIS_COMPASS_INFO: Message = MessageBuilder("commands.path_visualizer.type.compass.info")
            .withDefault(
                """
          <primary_l>Visualizer: {visualizer}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Interval: <text_hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} interval">{interval}</click></hover></text_hl>
          <bg>» </bg><text>Marker:
              <bg>» </bg><text>Target: {marker-target}
              <bg>» </bg><text>North: {marker-north}
              <bg>» </bg><text>East: {marker-east}
              <bg>» </bg><text>South: {marker-south}
              <bg>» </bg><text>West: {marker-west}
          <bg>» </bg><text>Background: {background}
          <bg>» </bg><text>Color: {bb_color}
          <bg>» </bg><text>Overlay: {overlay}
          """.trimIndent()
            )
            .withPlaceholders(
                "marker-north", "marker-south", "marker-east", "marker-west",
                "marker-target", "background", "color", "overlay"
            )
            .build()

        val CMD_ADV_VIS_INFO_PARTICLES: Message =
            MessageBuilder("commands.path_visualizer.type.advanced_particle_visualizer.info")
                .withDefault(
                    """
          <primary_l>Visualizer: {visualizer}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Interval: <text_hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} interval">{interval}</click></hover></text_hl>
          <bg>» </bg><text>Point-Distance: <text_hl><hover:show_text:"Click to change point-distance"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} point-distance">{point-distance}</click></hover></text_hl>
          <bg>» </bg><text>Particle: <text_hl><hover:show_text:"Click to change particle"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} particle">{particle}</click></hover></text_hl>
          <bg>» </bg><text>Particle-Data: <text_hl><hover:show_text:"Click to change particle-Data"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} particle-data">{particle-data}</click></hover></text_hl>
          <bg>» </bg><text>Amount: <text_hl><hover:show_text:"Click to change amount"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} particle">{amount}</click></hover></text_hl>
          <bg>» </bg><text>Speed: <text_hl><hover:show_text:"Click to change speed"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} particle">{speed}</click></hover></text_hl>
          <bg>» </bg><text>Offset:
              <bg>» </bg><text>X: <text_hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} offset-x">{offset-x}</click></hover></text_hl>
              <bg>» </bg><text>Y: <text_hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} offset-y">{offset-y}</click></hover></text_hl>
              <bg>» </bg><text>Z: <text_hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} offset-z">{offset-z}</click></hover></text_hl>
          <bg>» </bg><text>Path Offset (e.g. to make Spirals):
              <bg>» </bg><text>X: <text_hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} path-x">{path-x}</click></hover></text_hl>
              <bg>» </bg><text>Y: <text_hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} path-y">{path-y}</click></hover></text_hl>
              <bg>» </bg><text>Z: <text_hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} path-z">{path-z}</click></hover></text_hl>
              """.trimIndent()
                )
                .withPlaceholders(
                    "visualizer", "type", "permission", "interval", "point-distance",
                    "particle", "particle-steps", "amount", "speed", "offset-x", "offset-y", "offset-z",
                    "path-x", "path-y", "path-z"
                )
                .build()

        val CMD_VIS_PAPI_INFO: Message = MessageBuilder("commands.path_visualizer.type.placeholder_api.info")
            .withDefault(
                """
          <primary_l>Visualizer: {visualizer}</primary_l>
          <bg>» </bg><text>Permission: <text_hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} permission">{permission}</click></hover></text_hl>
          <bg>» </bg><text>Interval: <text_hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle {key} interval">{interval}</click></hover></text_hl>
          <bg>» </bg><text>Placeholder:</text>
              <bg>» </bg><text>North: <text_hl><hover:show_text:"Click to change format-north"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-north">{format-north}</click></hover></text_hl>
              <bg>» </bg><text>North-East: <text_hl><hover:show_text:"Click to change format-northeast"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-northeast">{format-northeast}</click></hover></text_hl>
              <bg>» </bg><text>East: <text_hl><hover:show_text:"Click to change format-east"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-east">{format-east}</click></hover></text_hl>
              <bg>» </bg><text>South-East: <text_hl><hover:show_text:"Click to change format-southeast"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-southeast">{format-southeast}</click></hover></text_hl>
              <bg>» </bg><text>South: <text_hl><hover:show_text:"Click to change format-south"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-south">{format-south}</click></hover></text_hl>
              <bg>» </bg><text>South-West: <text_hl><hover:show_text:"Click to change format-southwest"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-southwest">{format-southwest}</click></hover></text_hl>
              <bg>» </bg><text>West: <text_hl><hover:show_text:"Click to change format-west"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-west">{format-west}</click></hover></text_hl>
              <bg>» </bg><text>North-West: <text_hl><hover:show_text:"Click to change format-northwest"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-northwest">{format-northwest}</click></hover></text_hl>
              <bg>» </bg><text>Distance: <text_hl><hover:show_text:"Click to change format-distance"><click:suggest_command:"/pathvisualizer edit placeholderapi {key} format-distance">{format-distance}</click></hover></text_hl>
              """.trimIndent()
            )
            .withPlaceholders(
                "visualizer",
                "name",
                "name-format",
                "type",
                "permission",
                "interval",
                "format-north",
                "format-north-east",
                "format-east",
                "format-south-east",
                "format-south",
                "format-south-west",
                "format-west",
                "format-north-west",
                "format-distance"
            )
            .build()

        val E_NODE_TOOL_N: Message = MessageBuilder("editor.toolbar.node_tool.name")
            .withDefault("<text_l><u>Node Tool</u></text_l>")
            .build()
        val E_NODE_TOOL_L: Message = MessageBuilder("editor.toolbar.node_tool.lore")
            .withDefault(
                """
          <text>» <accent>right-click:</accent> Create node</text>
          <text>» <accent>left-click:</accent> Delete clicked node</text>
          <text>» <accent>left-click air:</accent> Activate chain mode</text>
          """.trimIndent()
            )
            .build()
        val E_EDGEDIR_TOOL_N: Message = MessageBuilder("editor.toolbar.edge_directed_toggle.name")
            .withDefault("<text_l><u>Edges Directed: <text_hl><value:true:false></text_hl></u></text_l>")
            .withPlaceholder("value", "Choice Placeholder, usage: <value:show-this-if-true:show-this-if-false>")
            .build()
        val E_EDGEDIR_TOOL_L: Message = MessageBuilder("editor.toolbar.edge_directed_toggle.lore")
            .withDefault(
                """
          <text>An edge is directed if its
          color goes from red to blue.
          Players can cross this section only
          in that direction, like a one way road.
          """.trimIndent()
            )
            .build()
        val E_NODE_CHAIN_NEW: Message = MessageBuilder("editor.node_tool.chain.new")
            .withDefault("<prefix>Node chain completed.")
            .build()
        val E_NODE_CHAIN_START: Message = MessageBuilder("editor.node_tool.chain.new_start")
            .withDefault("<prefix>Chain started.")
            .build()
        val E_NODE_TOOL_DIR_TOGGLE: Message = MessageBuilder("editor.toolbar.node_tool.directed")
            .withDefault("<prefix>Edges directed: <text_hl>{value:true:false}<text_hl>")
            .withPlaceholders("value")
            .build()
        val E_GROUP_TOOL_N: Message = MessageBuilder("editor.toolbar.group_tool.name")
            .withDefault("<text_l><u>Assign Group</u></text_l>")
            .build()
        val E_GROUP_TOOL_L: Message = MessageBuilder("editor.toolbar.group_tool.lore")
            .withDefault("")
            .build()
        val E_MULTI_GROUP_TOOL_N: Message = MessageBuilder("editor.toolbar.multi_group_tool.name")
            .withDefault("<text_l><u>Mutli Group Tool</u></text_l>")
            .build()
        val E_MULTI_GROUP_TOOL_L: Message = MessageBuilder("editor.toolbar.multi_group_tool.lore")
            .withDefault(
                """
          <text>Assign and remove multiple
          <text>groups at once.
                    
          <text>» <accent>right-click air:</accent> Open GUI</text>
          <text>» <accent>right-click node:</accent> Add groups</text>
          <text>» <accent>left-click node:</accent> Remove groups</text>
          """.trimIndent()
            )
            .build()
        val E_TP_TOOL_N: Message = MessageBuilder("editor.toolbar.teleport_tool.name")
            .withDefault("<text_l><u>Teleport Tool</u></text_l>")
            .build()
        val E_TP_TOOL_L: Message = MessageBuilder("editor.toolbar.teleport_tool.lore")
            .withDefault("<text>Teleports you to the\n<text>nearest node.")
            .build()
        val E_SUB_GROUP_TITLE: Message = MessageBuilder("editor.groups.title")
            .withDefault("Assign Node Groups")
            .build()
        val E_SUB_GROUP_INFO_N: Message = MessageBuilder("editor.groups.info.name")
            .withDefault("<accent>Info</accent>")
            .build()
        val E_SUB_GROUP_INFO_L: Message = MessageBuilder("editor.groups.info.lore")
            .withDefault("<text>Click to toggle groups on or off.</text>\n<text>Create a new nodegroup with\n<text>» <accent>/pf creategroup <arg>key</arg>")
            .build()
        val E_SUB_GROUP_RESET_N: Message = MessageBuilder("editor.groups.reset.name")
            .withDefault("<prefix_negative>Reset Groups</prefix_negative>")
            .build()
        val E_SUB_GROUP_RESET_L: Message = MessageBuilder("editor.groups.reset.lore")
            .withDefault("<text>Reset all groups for the\n<text>selected node.")
            .build()
        val E_SUB_GROUP_ENTRY_N: Message = MessageBuilder("editor.groups.entry.name")
            .withDefault("<primary_l>{group:key}</primary_l>")
            .withPlaceholders("key", "weight", "modifiers")
            .build()
        val E_SUB_GROUP_ENTRY_L: Message = MessageBuilder("editor.groups.entry.lore")
            .withDefault(
                """
          <bg>» </bg><text>Weight: </text><text_hl>{weight:#.##}</text_hl><modifiers:""><newline/><bg>» </bg>{el}</modifiers>
          """.trimIndent()
            )
            .withPlaceholders("key", "weight", "modifiers")
            .build()
        val TARGET_FOUND: Message = MessageBuilder("general.target_reached")
            .withDefault("<prefix>Target reached.")
            .build()

        val EDITM_NG_DELETED: Message = MessageBuilder("editmode.group_deleted")
            .withDefault("<prefix_negative>Your currently edited group was deleted by another user.")
            .build()

        fun applyObjectResolvers(translator: MessageTranslator) {
            translator.add(
                TinyObjectResolver.builder(PathPlayer::class.java)
                    .with("uuid", PathPlayer<*>::uniqueId)
                    .with("name", PathPlayer<*>::name)
                    .with("loc", PathPlayer<*>::location)
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(Keyed::class.java)
                    .with("key", Keyed::getKey)
                    .withFallback(Keyed::getKey)
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(Location::class.java)
                    .with("world", Location::getWorld)
                    .withFallback { GEN_LOCATION.insertObject("loc", it) }
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(Vector::class.java)
                    .with("x", Vector::x)
                    .with("y", Vector::y)
                    .with("z", Vector::z)
                    .withFallback { GEN_LOCATION.insertObject("loc", it) }
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(World::class.java)
                    .with("name", World::getName)
                    .with("uuid", World::getUID)
                    .withFallback { Component.text(it.name) }
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(Node::class.java)
                    .with("id", Node::nodeId)
                    .with("loc", Node::location)
                    .with("edges", Node::edges)
                    .withFallback { GEN_NODE.insertObject("node", it) }
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(Edge::class.java)
                    .with("start", Edge::start)
                    .with("end", Edge::end)
                    .with("weight", Edge::weight)
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(NodeGroup::class.java)
                    .with("weight", NodeGroup::weight)
                    .with("modifiers", NodeGroup::modifiers)
                    .with("content") { listOf(it) }
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(PathVisualizer::class.java)
                    .with("type") {
                        runBlocking { PathFinder.get().storage.loadVisualizerType<PathVisualizer<*, *>>(it.key)?.key }
                    }
                    .with("perm") { it.permission }
                    .build()
            )
            translator.add(
                TinyObjectResolver.builder(NodeSelection::class.java)
                    .with("content") { ArrayList<Node>() }
                    .withFallback { GEN_NODE_SEL.insertObject("nsel", it) }
                    .build()
            )
        }
    }
}
