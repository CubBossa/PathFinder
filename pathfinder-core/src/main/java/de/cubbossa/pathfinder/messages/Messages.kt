package de.cubbossa.pathfinder.messages

import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.command.ModifierCommandExtension
import de.cubbossa.pathfinder.command.ModifierCommandExtension.toComponents
import de.cubbossa.pathfinder.group.Modifier
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.Range
import de.cubbossa.pathfinder.misc.Vector
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.CollectionUtils
import de.cubbossa.translations.Message
import de.cubbossa.translations.MessageBuilder
import lombok.Setter
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.AudienceProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Particle
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

object Messages {
    private val formatter: MessageFormatter = MessageFormatterImpl()

    fun formatter(): MessageFormatter {
        return formatter
    }

    val PREFIX: Message = MessageBuilder("prefix")
        .withDefault("<c-brand>PathFinder</c-brand><bg> › </bg><t>")
        .build()
    val GEN_TOO_FAST: Message = MessageBuilder("general.response_pending")
        .withDefault("<msg:prefix>Better slow down, your database is out of breath.")
        .build()
    val GEN_ERROR: Message = MessageBuilder("general.error")
        .withDefault("<c-negative><cause></c-negative>")
        .withPlaceholders("cause")
        .build()
    val GEN_VECTOR: Message = MessageBuilder("general.vector")
        .withDefault("<t-hl><x:#.##><t>,</t> <y:#.##><t>,</t> <z:#.##></t-hl>")
        .withPlaceholders("x", "y", "z")
        .withComment("The numberformat can be specified as argument for x, y and z. Check out https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html for more information on number formatting.")
        .build()
    val GEN_PARTICLE: Message = MessageBuilder("general.particle")
        .withDefault("<t-hl><particle></t-hl>")
        .withPlaceholders("particle", "meta")
        .build()
    val GEN_PARTICLE_META: Message = MessageBuilder("general.particle")
        .withDefault("<t-hl><particle> <t>(<meta>)</t></t-hl>")
        .withPlaceholders("particle", "meta")
        .build()

    val GEN_NULL: Message = MessageBuilder("general.null")
        .withDefault("<t-hl>null</t-hl>")
        .build()
    val GEN_NODE: Message = MessageBuilder("general.node")
        .withDefault("(<world>; <location>)")
        .withPlaceholders("id", "world", "location")
        .build()
    val GEN_NODE_SEL: Message = MessageBuilder("general.selection.nodes")
        .withDefault("<white><u><amount> Nodes</u></white>")
        .withPlaceholders("amount")
        .build()
    val GEN_GROUP_SEL: Message = MessageBuilder("general.selection.groups")
        .withDefault("<t-warm><u><amount> Groups</u></t-warm>")
        .withPlaceholders("amount")
        .build()
    val RELOAD_ERROR: Message = MessageBuilder("command.reload.error")
        .withDefault("<c-negative>An error occurred while reloading: <error></c-negative>")
        .withPlaceholder("error")
        .build()
    val RELOAD_SUCCESS: Message = MessageBuilder("command.reload.success.general")
        .withDefault("<msg:prefix>Successfully reloaded in <c-offset-light><ms></c-offset-light><c-offset>ms</c-offset>.")
        .withPlaceholders("ms")
        .build()
    val RELOAD_SUCCESS_LANG: Message = MessageBuilder("command.reload.success.language")
        .withDefault("<msg:prefix>Successfully reloaded language in <c-offset-light><ms></c-offset-light><c-offset>ms</c-offset>.")
        .withPlaceholders("ms")
        .build()
    val RELOAD_SUCCESS_FX: Message = MessageBuilder("command.reload.success.effects")
        .withDefault("<msg:prefix>Successfully reloaded effects in <c-offset-light><ms></c-offset-light><c-offset>ms</c-offset>.")
        .withPlaceholders("ms")
        .build()
    val RELOAD_SUCCESS_CFG: Message = MessageBuilder("command.reload.success.config")
        .withDefault("<msg:prefix>Successfully reloaded config files in <c-offset-light><ms></c-offset-light><c-offset>ms</c-offset>.")
        .withPlaceholders("ms")
        .build()

    val HELP: Message = MessageBuilder("general.help")
        .withDefault(
            """
          <gradient:black:dark_gray:black>------------ <c-brand>Pathfinder</c-brand> ------------</gradient>
          <t>Running <c-offset>Pathfinder v<version></c-offset>.
                
          <t>Require help? Checkout the <t-warm><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></t-warm>.
          <t>Use <t-warm>/pf help</t-warm> to view available commands.
          
          """.trimIndent()
        )
        .withPlaceholder("version")
        .build()
    val CMD_DUMP_SUCCESS: Message = MessageBuilder("command.createdump.success")
        .withDefault("<msg:prefix>Dump file successfully created in plugin directory.")
        .build()
    val CMD_DUMP_FAIL: Message = MessageBuilder("command.createdump.failure")
        .withDefault("<negative>Dump file could not be created. Check console for details.</negative>")
        .build()
    val CMD_HELP: Message = MessageBuilder("command.help")
        .withDefault(
            """
          <gradient:black:dark_gray:black>------------ <c-brand>Pathfinder</c-brand> ------------</gradient>
          <t>Require help? Checkout the <t-warm><u><click:open_url:"https://docs.leonardbausenwein.de/getting_started/introduction.html">WIKI</click></u></t-warm>.
                    
          <white>Commands:</white>
          <bg>» </bg><t><t-warm>/pf editmode</t-warm> - Create, edit and delete waypoints via GUI
          <bg>» </bg><t><t-warm>/pf node</t-warm> - Create, edit and delete waypoints via Commands
          <bg>» </bg><t><t-warm>/pf group</t-warm> - Add behaviour to multiple waypoints
          <bg>» </bg><t><t-warm>/pf visualizer</t-warm> - Compass, particles, placeholders and more
          <bg>» </bg><t><t-warm>/find</t-warm> - Find the shortest way to a nodegroup
          
          """.trimIndent()
        )
        .build()

    val CMD_INCOMPLETE: Message = MessageBuilder("command.error.incomplete.title")
        .withDefault("<red>Your command is incomplete. Use one of the syntaxes below:")
        .build()
    val CMD_INCOMPLETE_LINE: Message = MessageBuilder("command.error.incomplete.line")
        .withDefault("<bg>» </bg><cmd>")
        .withPlaceholders("cmd")
        .build()

    val INFO: Message = MessageBuilder("commands.info")
        .withDefault(
            """
          <gradient:black:dark_gray:black>------------ <c-brand>Pathfinder</c-brand> ------------</gradient>
          <bg>» </bg><t>Current version: <t-warm><version></t-warm>
          <bg>» </bg><t>Spigot-Page: <t-warm><u><click:open_url:"https://www.spigotmc.org/resources/gps-pathfinder-minecraft-pathfinding-tool.104961/">https://www.spigotmc.org/...</click></u></t-warm>
          
          """.trimIndent()
        )
        .withPlaceholders("authors", "version", "api-version")
        .build()

    val MODULES: Message = MessageBuilder("commands.modules")
        .withDefault(
            """
          <c-offset>Active Modules:</c-offset>
          <modules:"
          ":"<bg>» </bg>">
          """.trimIndent()
        )
        .withPlaceholder("modules")
        .build()

    val CMD_FORCE_FIND: Message = MessageBuilder("commands.force_find")
        .withDefault("<msg:prefix>Player <name> discovered <t-hl><discovery></t-hl>.")
        .withPlaceholder("name", "discovery")
        .build()
    val CMD_FORCE_FORGET: Message = MessageBuilder("commands.force_forget")
        .withDefault("<msg:prefix>Player <name> forgot about <t-hl><discovery></t-hl>.")
        .withPlaceholders("name", "discovery")
        .build()

    val CMD_N_CREATE: Message = MessageBuilder("commands.node.create")
        .withDefault("<msg:prefix>Successfully created Node #<id>.")
        .withTranslation(Locale.GERMAN, "<msg:prefix>Wegpunkt #<id> erfolgreich erstellt.")
        .withPlaceholders("id")
        .build()
    val CMD_N_DELETE: Message = MessageBuilder("commands.node.delete")
        .withDefault("<msg:prefix>Successfully deleted <selection>.")
        .withPlaceholders("selection")
        .build()
    val CMD_N_UPDATED: Message = MessageBuilder("commands.node.moved")
        .withDefault("<msg:prefix>Updated <selection>.")
        .withPlaceholders("selection", "location")
        .build()
    val CMD_N_INFO: Message = MessageBuilder("commands.node.info")
        .withDefault(
            """
          <c-offset>Node #<id></c-offset>
          <bg>» </bg><t>Position: <t-hl><position></t-hl> (<world>)
          <bg>» </bg><t>Edges: <edges>
          <bg>» </bg><t>Groups: <groups>
          
          """.trimIndent()
        )
        .withPlaceholders("id", "groups", "position", "world", "edges")
        .build()
    val CMD_N_INFO_NO_SEL: Message = MessageBuilder("commands.node.info_no_selection")
        .withDefault("<c-negative>No nodes found to display. Check your selection query.</c-negative>")
        .build()
    val CMD_N_ADD_GROUP: Message = MessageBuilder("commands.node.add_group")
        .withDefault("<msg:prefix>Added <nodes> to group <t-hl><group></t-hl>.")
        .withPlaceholders("nodes", "group")
        .build()
    val CMD_N_REMOVE_GROUP: Message = MessageBuilder("commands.node.remove_groups")
        .withDefault("<msg:prefix>Removed <nodes> from group <t-hl><group></t-hl>.")
        .withPlaceholders("nodes", "group")
        .build()
    val CMD_N_CLEAR_GROUPS: Message = MessageBuilder("commands.node.clear_groups")
        .withDefault("<msg:prefix>Cleared all groups for <nodes>.")
        .withPlaceholders("nodes")
        .build()

    val CMD_N_LIST_HEADER: Message = MessageBuilder("commands.node.list.header")
        .withDefault("<gradient:black:dark_gray:black>------------ <c-brand-light>Waypoints</c-brand-light> ------------</gradient>")
        .withPlaceholders("page", "next-page", "prev-page", "pages")
        .build()
    val CMD_N_LIST_ELEMENT: Message = MessageBuilder("commands.node.list.element")
        .withDefault("<bg>» </bg><hover:show_text:'<t>Groups: <groups><newline><t>Edges to: <edges><newline><t>Click for more information'><click:run_command:/pf nodes \"@n[id=<id>]\" info><t>at <position> (<world>)")
        .withPlaceholders("id", "position", "world", "curve-length", "edges", "groups")
        .build()
    val CMD_N_LIST_FOOTER: Message = MessageBuilder("commands.node.list.footer")
        .withDefault("<gradient:black:dark_gray:black>------------<t> <click:run_command:/pf listnodes \"<selector>\" <prev-page>>←</click> <page>/<pages> <click:run_command:/pf listnodes \"<selector>\" <next-page>>→</click> </t>-------------</gradient>")
        .withPlaceholders("page", "next-page", "prev-page", "pages")
        .build()
    val CMD_N_CONNECT: Message = MessageBuilder("commands.node.connect.success")
        .withDefault("<msg:prefix>Connected <start> to <end>.")
        .withPlaceholders("start", "end")
        .build()
    val CMD_N_DISCONNECT: Message = MessageBuilder("commands.node.disconnect.success")
        .withDefault("<msg:prefix>Disconnected <start> from <end>.")
        .withPlaceholders("start", "end")
        .build()

    val CMD_NG_CREATE_FAIL: Message = MessageBuilder("commands.node_group.create_fail")
        .withDefault("<c-negative>Could not create Nodegroup. Check out console for details.")
        .build()
    val CMD_NG_ALREADY_EXISTS: Message = MessageBuilder("commands.node_group.already_exists")
        .withDefault("<c-negative>A node group <key> already exists.</c-negative>")
        .withPlaceholders("key")
        .build()
    val CMD_NG_CREATE: Message = MessageBuilder("commands.node_group.create")
        .withDefault("<msg:prefix>Node group <t-hl><key></t-hl> created.")
        .withPlaceholders("key")
        .build()
    val CMD_NG_DELETE: Message = MessageBuilder("commands.node_group.delete")
        .withDefault("<msg:prefix>Node group <t-hl><key></t-hl> deleted.")
        .withPlaceholders("key")
        .build()
    val CMD_NG_DELETE_GLOBAL: Message = MessageBuilder("commands_node_group.delete_fail_global")
        .withDefault("<c-negative>You cannot delete the global node group.</c-negative>")
        .withTranslation(Locale.GERMAN, "<c-negative>Du kannst die globale Wegpunktgruppe nicht löschen.</c-negative>")
        .withComment("Indicates, that the global nodegroup cannot be deleted by command.")
        .build()
    val CMD_NG_INFO: Message = MessageBuilder("commands.node_group.info")
        .withDefault(
            """
          <c-brand-light>Group '<key>'</c-brand-light>
          <bg>» </bg><t>Size: <nodes>
          <bg>» </bg><t>Weight: <t-hl><weight></t-hl><modifiers:"":"
          <bg>» </bg><t>"/>
          
          """.trimIndent()
        )
        .withPlaceholders("modifiers", "key", "nodes", "weight")
        .build()
    val CMD_NG_LIST_HEADER: Message = MessageBuilder("commands.node_group.list.header")
        .withDefault("<gradient:black:dark_gray:black>------------ <c-brand-light>Node-Groups</c-brand-light> ------------</gradient>")
        .withPlaceholders("page", "next-page", "prev-page", "pages")
        .build()
    val CMD_NG_LIST_LINE: Message = MessageBuilder("commands.node_group.list.line")
        .withDefault("<dark_gray> » </dark_gray><key> <t>(Weight: <weight>)</t>")
        .withPlaceholders("page", "key", "weight", "modifiers")
        .build()
    val CMD_NG_LIST_FOOTER: Message = MessageBuilder("commands.node_group.list.footer")
        .withDefault("<gradient:black:dark_gray:black>------------<t> <click:run_command:/pf listgroups <prev-page>>←</click> <page>/<pages> <click:run_command:/pf listgroups <next-page>>→</click></t> -------------</gradient>")
        .withPlaceholders("page", "next-page", "prev-page", "pages")
        .build()
    val CMD_NG_MODIFY_SET: Message = MessageBuilder("commands.node_group.modify.set")
        .withDefault("<msg:prefix>Added modifier <t-hl><type></t-hl> to group <t-hl><group></t-hl>.")
        .withPlaceholders("type", "group")
        .build()
    val CMD_NG_MODIFY_REMOVE: Message = MessageBuilder("commands.node_group.modify.remove")
        .withDefault("<msg:prefix>Removed modifier <t-hl><type><t-hl> from group <t-hl><group></t-hl>.")
        .withPlaceholders("type", "group")
        .build()
    val CMD_NG_MOD_CURVELEN: Message = MessageBuilder("commands.node_group.modifier.curvelength")
        .withDefault("Curve length: <length:#.##>")
        .withPlaceholder("length", "Use java number formatting to provide custom formatting.")
        .build()
    val CMD_NG_MOD_DISCOVER: Message = MessageBuilder("commands.node_group.modifier.discoverable")
        .withDefault("Discover as: <name>")
        .withPlaceholder("name", "The name that is being shown when discovering this group.")
        .build()
    val CMD_NG_MOD_DISCOVERIES: Message = MessageBuilder("commands.node_group.modifier.discover-progress")
        .withDefault("Discover progress as: <name>")
        .withPlaceholder("name", "The name that is being shown when running /discoveries.")
        .build()
    val CMD_NG_MOD_FINDDIST: Message = MessageBuilder("commands.node_group.modifier.finddistance")
        .withDefault("Find distance: <distance:#.##>")
        .withPlaceholder("distance", "Use java number formatting to provide custom formatting.")
        .build()
    val CMD_NG_MOD_SEARCH: Message = MessageBuilder("commands.node_group.modifier.navigable")
        .withDefault("Search terms: <terms:\"<t>, </t>\">")
        .withPlaceholder("terms", "A list tag for all search terms, use <terms:between:beforeeach>")
        .build()
    val CMD_NG_MOD_PERM: Message = MessageBuilder("commands.node_group.modifier.permission")
        .withDefault("Permission: <permission>")
        .withPlaceholder("permission")
        .build()
    val CMD_NG_MOD_VIS: Message = MessageBuilder("commands.node_group.modifier.visualizer")
        .withDefault("Visualizer: <visualizer>")
        .withPlaceholder("visualizer")
        .build()

    val CMD_FIND: Message = MessageBuilder("commands.find.success")
        .withDefault("<msg:prefix>Navigation started.  [ <c-accent><click:run_command:/cancelpath>/cancelpath</click></c-accent> ]")
        .build()
    val CMD_DISCOVERIES_ENTRY: Message = MessageBuilder("commands.discoveries.list.entry")
        .withDefault("<bg>» </bg><name>: <percentage:#.##>%")
        .withPlaceholders("name", "percentage", "ratio")
        .build()
    val CMD_DISCOVERIES_HEADER: Message = MessageBuilder("commands.discoveries.list.header")
        .withDefault("<gradient:black:dark_gray:black>------------ <c-brand-light>Discoveries</c-brand-light> ------------</gradient>")
        .withPlaceholders("page", "next-page", "prev-page", "pages")
        .build()
    val CMD_DISCOVERIES_FOOTER: Message = MessageBuilder("commands.discoveries.list.footer")
        .withDefault("<gradient:black:dark_gray:black>-------------<t> <click:run_command:/discoveries <prev-page>>←</click> <page>/<pages> <click:run_command:/discoveries <next-page>>→</click> </t>--------------</gradient>")
        .withPlaceholders("page", "next-page", "prev-page", "pages")
        .build()
    val CMD_FIND_EMPTY: Message = MessageBuilder("commands.find.no_nodes_found")
        .withDefault("<msg:prefix>No matching waypoints could be found.")
        .build()
    val CMD_FIND_TOO_FAR: Message = MessageBuilder("commands.find.too_far_away")
        .withDefault("<msg:prefix>The given location is too far away from any waypoint.")
        .build()
    val CMD_FIND_BLOCKED: Message = MessageBuilder("commands.find.no_path_found")
        .withDefault("<msg:prefix>No possible way could be found to reach that target.")
        .build()
    val CMD_FIND_UNKNOWN: Message = MessageBuilder("commands.find.unknown_error")
        .withDefault("<c-negative>An unknown error occurred.</c-negative>")
        .build()
    val CMD_CANCEL: Message = MessageBuilder("commands.cancel_path")
        .withDefault("<msg:prefix>Navigation cancelled.")
        .build()
    val CMD_FINDP_OFFLINE: Message = MessageBuilder("commands.find_player.target_offline")
        .withDefault("<c-negative>Player not found.</c-negative>")
        .build()
    val CMD_FINDP_NO_SELF: Message = MessageBuilder("commands.find_player.no_requests_to_self")
        .withDefault("<c-negative>You cannot make requests to yourself.</c-negative>")
        .build()
    val CMD_FINDP_NO_REQ: Message = MessageBuilder("commands.find_player.no_requests")
        .withDefault("<c-negative>No requests found.</c-negative>")
        .build()
    val CMD_FINDP_ALREADY_REQ: Message = MessageBuilder("commands.find_player.already_requested")
        .withDefault("<c-negative>Navigation already requested.</c-negative>")
        .build()
    val CMD_FINDP_REQUEST: Message = MessageBuilder("commands.find_player.request")
        .withDefault("<msg:prefix>Made a request to navigate to <t-hl><target></t-hl>.")
        .withPlaceholders("requester", "target")
        .build()
    val CMD_FINDP_REQUESTED: Message = MessageBuilder("commands.find_player.requested")
        .withDefault("<msg:prefix><t-hl><requester></t-hl> asked to navigate to you. [ <green><click:run_command:/fpaccept <requester>>accept</click></green> ]  [ <red><click:run_command:/fpdecline <requester>>decline</click></red> ]")
        .withPlaceholders("requester", "target")
        .build()
    val CMD_FINDP_ACCEPT: Message = MessageBuilder("commands.find_player.accept")
        .withDefault("<msg:prefix>Request accepted.")
        .withPlaceholders("requester", "target")
        .build()
    val CMD_FINDP_ACCEPTED: Message = MessageBuilder("commands.find_player.accepted")
        .withDefault("<msg:prefix><t-hl><target></t-hl> accepted your navigate request.")
        .withPlaceholders("requester", "target")
        .build()
    val CMD_FINDP_DECLINE: Message = MessageBuilder("commands.find_player.decline")
        .withDefault("<msg:prefix>Request declined.")
        .withPlaceholders("requester", "target")
        .build()
    val CMD_FINDP_DECLINED: Message = MessageBuilder("commands.find_player.declined")
        .withDefault("<msg:prefix><t-hl><target></t-hl> declined your navigate request.")
        .withPlaceholders("requester", "target")
        .build()
    val CMD_FINDP_EXPIRED: Message = MessageBuilder("commands.find_player.request_expired")
        .withDefault("<msg:prefix>Your request expired.")
        .build()
    val DISCOVERY_DISCOVER: Message = MessageBuilder("discovery.discover")
        .withDefault("You discovered: <discoverable>")
        .withTranslation(Locale.GERMAN, "Entdeckt: <discoverable>")
        .withPlaceholders("player", "discoverable", "group")
        .build()
    val DISCOVERY_PROG: Message = MessageBuilder("discovery.progress")
        .withDefault("<percentage:#.##>% of <name>")
        .withPlaceholders("player", "discoverable", "group", "name", "percentage", "ratio", "count-found", "count-all")
        .build()
    val DISCOVERY_FORGET: Message = MessageBuilder("discovery.forget")
        .withDefault("<msg:prefix>You forgot all about <discoverable>")
        .withTranslation(Locale.GERMAN, "Du vergisst alles über <discoverable>")
        .withPlaceholders("player", "discoverable", "group")
        .build()


    val CMD_VIS_LIST_HEADER: Message = MessageBuilder("commands.path_visualizer.list.header")
        .withDefault("<gradient:black:dark_gray:black>------------ <c-brand-light>Visualizer</c-brand-light> ------------</gradient>")
        .withPlaceholders("page", "next-page", "prev-page", "pages")
        .build()
    val CMD_VIS_LIST_ENTRY: Message = MessageBuilder("commands.path_visualizer.list.entry")
        .withDefault("<dark_gray> » </dark_gray><key> <t>(<type>)</t>")
        .withPlaceholders(
            "key",
            "name",
            "world",
            "discoverable",
            "find-distance",
            "curve-length",
            "path-visualizer",
            "type"
        )
        .build()
    val CMD_VIS_LIST_FOOTER: Message = MessageBuilder("commands.path_visualizer.list.footer")
        .withDefault("<gradient:black:dark_gray:black>------------<t> <click:run_command:/pf listvisualizers <prev-page>>←</click> <page>/<pages> <click:run_command:/pf listvisualizers <next-page>>→</click> </t>-------------</gradient>")
        .withPlaceholders("page", "next-page", "prev-page", "pages")
        .build()
    val CMD_VIS_NO_TYPE_FOUND: Message = MessageBuilder("commands.path_visualizer.info.no_type")
        .withDefault("<c-negative>Could not show information to visualizer. Type could not be resolved.</c-negative>")
        .withTranslation(
            Locale.GERMAN,
            "<c-negative>Konnte Visualizer nicht anzeigen. Keine Typ-Information gefunden.</c-negative>"
        )
        .build()
    val CMD_VIS_NO_INFO: Message = MessageBuilder("commands.path_visualizer.info.no_info")
        .withDefault("<c-negative>Could not show information to visualizer. No message layout provided.</c-negative>")
        .withTranslation(
            Locale.GERMAN,
            "<c-negative>Konnte Visualizer nicht anzeigen. Kein Nachrichtenformat gefunden.</c-negative>"
        )
        .build()

    val CMD_VIS_CREATE_SUCCESS: Message = MessageBuilder("commands.path_visualizer.create.success")
        .withDefault("<msg:prefix>Successfully created Visualizer <t-hl><key></t-hl> of type <t-hl><type></t-hl>.")
        .withPlaceholders("key", "type")
        .build()
    val CMD_VIS_NAME_EXISTS: Message = MessageBuilder("commands.path_visualizer.create.already_exists")
        .withDefault("<c-negative>Another visualizer with this name already exists.")
        .build()
    val CMD_VIS_DELETE_SUCCESS: Message = MessageBuilder("commands.path_visualizer.delete.success")
        .withDefault("<msg:prefix>Successfully deleted Visualizer <c-offset><key></c-offset>.")
        .withPlaceholders("key")
        .build()
    val CMD_VIS_DELETE_ERROR: Message = MessageBuilder("commands.path_visualizer.delete.error")
        .withDefault("<c-negative>An unknown error occurred while deleting a visualizer. Please check the console for more information.")
        .build()
    val CMD_VIS_SET_PROP: Message = MessageBuilder("commands.path_visualizer.set_property")
        .withDefault("<msg:prefix>Changed <property> for <t-hl><key></t-hl> from <t-hl><old-value></t-hl> to <t-hl><value></t-hl>.")
        .withPlaceholders("key", "type", "property", "value", "old-value")
        .build()
    val CMD_VIS_SET_PROP_ERROR: Message = MessageBuilder("commands.path_visualizer.set_property_error")
        .withDefault("<c-negative>Could not set property <property> for visualizer.")
        .withPlaceholders("key", "property")
        .build()
    val CMD_VIS_IMPORT_EXISTS: Message = MessageBuilder("commands.path_visualizer.import.already_exists")
        .withDefault("<c-negative>Could not import file, another visualizer with this key already exists.</c-negative>")
        .build()
    val CMD_VIS_IMPORT_NOT_EXISTS: Message = MessageBuilder("commands.path_visualizer.import.file_doesnt_exist")
        .withDefault("<c-negative>Could not import file, there is no example file with this name.</c-negative>")
        .build()
    val CMD_VIS_IMPORT_SUCCESS: Message = MessageBuilder("commands.path_visualizer.import.successful")
        .withDefault("<msg:prefix>Successfully imported Visualizer <t-hl><key></t-hl>")
        .withPlaceholders("key")
        .build()
    val CMD_VIS_COMBINED_INFO: Message = MessageBuilder("commands.path_visualizer.type.combined.info")
        .withDefault(
            """
          <c-brand-light>Visualizer: <key></c-brand-light>
          <bg>» </bg><t>Permission: <t-hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle <key> permission"><permission></click></hover></t-hl>
          <bg>» </bg><t>Children:<entries:"":"<br><bg>  » </bg>"/>
          """.trimIndent()
        )
        .withPlaceholder("entries[:separator][:prefix][:suffix]")
        .build()
    val CMD_VIS_COMBINED_ADD: Message = MessageBuilder("commands.path_visualizer.type.combined.add")
        .withDefault("<msg:prefix>Added <child> as child to <visualizer>.")
        .withPlaceholders("child", "visualizer")
        .build()
    val CMD_VIS_COMBINED_REMOVE: Message = MessageBuilder("commands.path_visualizer.type.combined.remove")
        .withDefault("<msg:prefix>Removed <child> from children for <visualizer>.")
        .build()
    val CMD_VIS_COMBINED_CLEAR: Message = MessageBuilder("commands.path_visualizer.type.combined.clear")
        .withDefault("<msg:prefix>Cleared all children for <visualizer>.")
        .build()
    val CMD_VIS_INFO_PARTICLES: Message = MessageBuilder("commands.path_visualizer.type.particle_visualizer.info")
        .withDefault(
            """
          <c-brand-light>Visualizer: <key></c-brand-light>
          <bg>» </bg><t>Permission: <t-hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit particle <key> permission"><permission></click></hover></t-hl>
          <bg>» </bg><t>Interval: <t-hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit particle <key> interval"><interval></click></hover></t-hl>
          <bg>» </bg><t>Point-Distance: <t-hl><hover:show_text:"Click to change point-distance"><click:suggest_command:"/pathvisualizer edit particle <key> point-distance"><point-distance></click></hover></t-hl>
          <bg>» </bg><t>Particle: <t-hl><hover:show_text:"Click to change particle"><click:suggest_command:"/pathvisualizer edit particle <key> particle"><particle></click></hover></t-hl>
          <bg>» </bg><t>Particle-Steps: <t-hl><hover:show_text:"Click to change particle-steps"><click:suggest_command:"/pathvisualizer edit particle-steps <key> particle"><particle-steps></click></hover></t-hl>
          <bg>» </bg><t>Amount: <t-hl><hover:show_text:"Click to change amount"><click:suggest_command:"/pathvisualizer edit particle <key> particle"><amount></click></hover></t-hl>
          <bg>» </bg><t>Speed: <t-hl><hover:show_text:"Click to change speed"><click:suggest_command:"/pathvisualizer edit particle <key> speed"><speed></click></hover></t-hl>
          <bg>» </bg><t>Offset: <t-hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit particle <key> particle"><c-offset></click></hover></t-hl>
          """.trimIndent()
        )
        .withPlaceholders(
            "key", "type", "permission", "interval", "point-distance",
            "particle", "particle-steps", "amount", "speed", "offset"
        )
        .build()


    val CMD_VIS_COMPASS_INFO: Message = MessageBuilder("commands.path_visualizer.type.compass.info")
        .withDefault(
            """
          <c-brand-light>Visualizer: <key></c-brand-light>
          <bg>» </bg><t>Permission: <t-hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission"><permission></click></hover></t-hl>
          <bg>» </bg><t>Interval: <t-hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval"><interval></click></hover></t-hl>
          <bg>» </bg><t>Marker:
              <bg>» </bg><t>Target: <marker-target>
              <bg>» </bg><t>North: <marker-north>
              <bg>» </bg><t>East: <marker-east>
              <bg>» </bg><t>South: <marker-south>
              <bg>» </bg><t>West: <marker-west>
          <bg>» </bg><t>Background: <background>
          <bg>» </bg><t>Color: <color>
          <bg>» </bg><t>Overlay: <overlay>
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
          <c-brand-light>Visualizer: <key></c-brand-light>
          <bg>» </bg><t>Permission: <t-hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission"><permission></click></hover></t-hl>
          <bg>» </bg><t>Interval: <t-hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval"><interval></click></hover></t-hl>
          <bg>» </bg><t>Point-Distance: <t-hl><hover:show_text:"Click to change point-distance"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> point-distance"><point-distance></click></hover></t-hl>
          <bg>» </bg><t>Particle: <t-hl><hover:show_text:"Click to change particle"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle"><particle></click></hover></t-hl>
          <bg>» </bg><t>Particle-Data: <t-hl><hover:show_text:"Click to change particle-Data"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle-data"><particle-data></click></hover></t-hl>
          <bg>» </bg><t>Amount: <t-hl><hover:show_text:"Click to change amount"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle"><amount></click></hover></t-hl>
          <bg>» </bg><t>Speed: <t-hl><hover:show_text:"Click to change speed"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> particle"><speed></click></hover></t-hl>
          <bg>» </bg><t>Offset:
              <bg>» </bg><t>X: <t-hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-x"><c-offset-x></click></hover></t-hl>
              <bg>» </bg><t>Y: <t-hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-y"><c-offset-y></click></hover></t-hl>
              <bg>» </bg><t>Z: <t-hl><hover:show_text:"Click to change offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> offset-z"><c-offset-z></click></hover></t-hl>
          <bg>» </bg><t>Path Offset (e.g. to make Spirals):
              <bg>» </bg><t>X: <t-hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-x"><path-x></click></hover></t-hl>
              <bg>» </bg><t>Y: <t-hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-y"><path-y></click></hover></t-hl>
              <bg>» </bg><t>Z: <t-hl><hover:show_text:"Click to change path offset"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> path-z"><path-z></click></hover></t-hl>
              """.trimIndent()
            )
            .withPlaceholders(
                "key", "type", "permission", "interval", "point-distance",
                "particle", "particle-steps", "amount", "speed", "offset-x", "offset-y", "offset-z",
                "path-x", "path-y", "path-z"
            )
            .build()

    val CMD_VIS_PAPI_INFO: Message = MessageBuilder("commands.path_visualizer.type.placeholder_api.info")
        .withDefault(
            """
          <c-brand-light>Visualizer: <key></c-brand-light>
          <bg>» </bg><t>Permission: <t-hl><hover:show_text:"Click to change permission"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> permission"><permission></click></hover></t-hl>
          <bg>» </bg><t>Interval: <t-hl><hover:show_text:"Click to change interval"><click:suggest_command:"/pathvisualizer edit advanced-particle <key> interval"><interval></click></hover></t-hl>
          <bg>» </bg><t>Placeholder:</t>
              <bg>» </bg><t>North: <t-hl><hover:show_text:"Click to change format-north"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-north"><format-north></click></hover></t-hl>
              <bg>» </bg><t>North-East: <t-hl><hover:show_text:"Click to change format-northeast"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-northeast"><format-northeast></click></hover></t-hl>
              <bg>» </bg><t>East: <t-hl><hover:show_text:"Click to change format-east"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-east"><format-east></click></hover></t-hl>
              <bg>» </bg><t>South-East: <t-hl><hover:show_text:"Click to change format-southeast"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-southeast"><format-southeast></click></hover></t-hl>
              <bg>» </bg><t>South: <t-hl><hover:show_text:"Click to change format-south"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-south"><format-south></click></hover></t-hl>
              <bg>» </bg><t>South-West: <t-hl><hover:show_text:"Click to change format-southwest"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-southwest"><format-southwest></click></hover></t-hl>
              <bg>» </bg><t>West: <t-hl><hover:show_text:"Click to change format-west"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-west"><format-west></click></hover></t-hl>
              <bg>» </bg><t>North-West: <t-hl><hover:show_text:"Click to change format-northwest"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-northwest"><format-northwest></click></hover></t-hl>
              <bg>» </bg><t>Distance: <t-hl><hover:show_text:"Click to change format-distance"><click:suggest_command:"/pathvisualizer edit placeholderapi <key> format-distance"><format-distance></click></hover></t-hl>
              """.trimIndent()
        )
        .withPlaceholders(
            "key", "name", "name-format", "type", "permission", "interval", "format-north", "format-north-east",
            "format-east", "format-south-east", "format-south", "format-south-west", "format-west", "format-north-west",
            "format-distance"
        )
        .build()

    val E_NODE_TOOL_N: Message = MessageBuilder("editor.toolbar.node_tool.name")
        .withDefault("<t-light><u>Node Tool</u></t-light>")
        .build()
    val E_NODE_TOOL_L: Message = MessageBuilder("editor.toolbar.node_tool.lore")
        .withDefault(
            """
          <t>» <c-accent>right-click:</c-accent> Create node</t>
          <t>» <c-accent>left-click:</c-accent> Delete clicked node</t>
          <t>» <c-accent>left-click air:</c-accent> Activate chain mode</t>
          """.trimIndent()
        )
        .build()
    val E_EDGEDIR_TOOL_N: Message = MessageBuilder("editor.toolbar.edge_directed_toggle.name")
        .withDefault("<t-light><u>Edges Directed: <t-hl><value:true:false></t-hl></u></t-light>")
        .withPlaceholder("value", "Choice Placeholder, usage: <value:show-this-if-true:show-this-if-false>")
        .build()
    val E_EDGEDIR_TOOL_L: Message = MessageBuilder("editor.toolbar.edge_directed_toggle.lore")
        .withDefault(
            """
          <gray>An edge is directed if its
          color goes from red to blue.
          Players can cross this section only
          in that direction, like a one way road.
          """.trimIndent()
        )
        .build()
    val E_NODE_CHAIN_NEW: Message = MessageBuilder("editor.node_tool.chain.new")
        .withDefault("<msg:prefix>Node chain completed.")
        .build()
    val E_NODE_CHAIN_START: Message = MessageBuilder("editor.node_tool.chain.new_start")
        .withDefault("<msg:prefix>Chain started.")
        .build()
    val E_NODE_TOOL_DIR_TOGGLE: Message = MessageBuilder("editor.toolbar.node_tool.directed")
        .withDefault("<msg:prefix>Edges directed: <t-hl><value:true:false><t-hl>")
        .withPlaceholders("value")
        .build()
    val E_GROUP_TOOL_N: Message = MessageBuilder("editor.toolbar.group_tool.name")
        .withDefault("<t-light><u>Assign Group</u></t-light>")
        .build()
    val E_GROUP_TOOL_L: Message = MessageBuilder("editor.toolbar.group_tool.lore")
        .withDefault("")
        .build()
    val E_MULTI_GROUP_TOOL_N: Message = MessageBuilder("editor.toolbar.multi_group_tool.name")
        .withDefault("<t-light><u>Mutli Group Tool</u></t-light>")
        .build()
    val E_MULTI_GROUP_TOOL_L: Message = MessageBuilder("editor.toolbar.multi_group_tool.lore")
        .withDefault(
            """
          <t>Assign and remove multiple
          <t>groups at once.
                    
          <t>» <c-accent>right-click air:</c-accent> Open GUI</t>
          <t>» <c-accent>right-click node:</c-accent> Add groups</t>
          <t>» <c-accent>left-click node:</c-accent> Remove groups</t>
          """.trimIndent()
        )
        .build()
    val E_TP_TOOL_N: Message = MessageBuilder("editor.toolbar.teleport_tool.name")
        .withDefault("<t-light><u>Teleport Tool</u></t-light>")
        .build()
    val E_TP_TOOL_L: Message = MessageBuilder("editor.toolbar.teleport_tool.lore")
        .withDefault("<t>Teleports you to the\n<t>nearest node.")
        .build()
    val E_SUB_GROUP_TITLE: Message = MessageBuilder("editor.groups.title")
        .withDefault("Assign Node Groups")
        .build()
    val E_SUB_GROUP_INFO_N: Message = MessageBuilder("editor.groups.info.name")
        .withDefault("<c-accent>Info</c-accent>")
        .build()
    val E_SUB_GROUP_INFO_L: Message = MessageBuilder("editor.groups.info.lore")
        .withDefault("<t>Click to toggle groups on or off.</t>\n<t>Create a new nodegroup with\n<t>» <c-accent>/pf creategroup <key>")
        .build()
    val E_SUB_GROUP_RESET_N: Message = MessageBuilder("editor.groups.reset.name")
        .withDefault("<c-negative>Reset Groups</c-negative>")
        .build()
    val E_SUB_GROUP_RESET_L: Message = MessageBuilder("editor.groups.reset.lore")
        .withDefault("<t>Reset all groups for the\n<t>selected node.")
        .build()
    val E_SUB_GROUP_ENTRY_N: Message = MessageBuilder("editor.groups.entry.name")
        .withDefault("<c-brand-light><key></c-brand-light>")
        .withPlaceholders("key", "weight", "modifiers")
        .build()
    val E_SUB_GROUP_ENTRY_L: Message = MessageBuilder("editor.groups.entry.lore")
        .withDefault(
            """
          <bg>» </bg><t>Weight: </t><t-hl><weight:#.##></t-hl><modifiers:"":"
          <bg>» </bg>"/></t>
          """.trimIndent()
        )
        .withPlaceholders("key", "weight", "modifiers")
        .build()
    val TARGET_FOUND: Message = MessageBuilder("general.target_reached")
        .withDefault("<msg:prefix>Target reached.")
        .build()

    val EDITM_NG_DELETED: Message = MessageBuilder("editmode.group_deleted")
        .withDefault("<c-negative>Your currently edited group was deleted by another user.")
        .build()

    @Setter
    private val audiences: AudienceProvider? = null

    private fun audienceSender(sender: CommandSender): Audience {
        return if (sender is Player
        ) audiences!!.player(sender.uniqueId)
        else audiences!!.console()
    }

    fun throwable(throwable: Throwable?): Message {
        return GEN_ERROR.formatted(formatter().throwable(throwable))
    }

    fun formatNode(node: Node): Message {
        return GEN_NODE.formatted(
            Placeholder.parsed("world", node.location.world.name),
            formatter().vector("location", node.location)
        )
    }

    class MessageFormatterImpl internal constructor() : MessageFormatter {
        private val miniMessage = MiniMessage.miniMessage()
        private val nullStyle = Style.empty()
        private val textStyle = Style.empty()
        private val numberStyle = Style.empty()

        override fun throwable(throwable: Throwable): TagResolver {
            return TagResolver.builder()
                .tag("message", Tag.preProcessParsed(throwable.message!!))
                .tag("cause", Tag.preProcessParsed(throwable.cause!!.message!!))
                .build()
        }

        override fun choice(key: String, value: Boolean): TagResolver {
            return Formatter.booleanChoice(key, value)
        }

        override fun number(key: String, value: Number): TagResolver {
            return Formatter.number(key, value)
        }

        override fun uuid(key: String, value: UUID): TagResolver {
            return TagResolver.resolver(key) { argumentQueue: ArgumentQueue, context: Context? ->
                if (!argumentQueue.hasNext()) {
                    return@resolver Tag.inserting(
                        Component.text(value.toString())
                            .clickEvent(ClickEvent.copyToClipboard(value.toString()))
                    )
                }
                val c = TextColor.fromCSSHexString(argumentQueue.pop().value())
                val upperCase = argumentQueue.hasNext() && argumentQueue.pop().isTrue
                var uuidString = value.toString()
                uuidString =
                    if (upperCase) uuidString.uppercase(Locale.getDefault()) else uuidString.lowercase(Locale.getDefault())
                val segments = Arrays.stream(uuidString.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray())
                    .map { content: String? ->
                        Component.text(
                            content!!
                        )
                    }
                    .map { textComponent: TextComponent -> textComponent.color(c) }
                    .collect(Collectors.toList())
                Tag.inserting(
                    Component.join(JoinConfiguration.separator(Component.text("-")), segments)
                        .clickEvent(ClickEvent.copyToClipboard(uuidString))
                )
            }
        }

        override fun namespacedKey(key: String, namespacedKey: NamespacedKey): TagResolver {
            return TagResolver.resolver(key) { queue: ArgumentQueue, context: Context? ->
                if (namespacedKey == null) {
                    return@resolver Tag.selfClosingInserting(GEN_NULL.clone())
                }
                val namespaceColor: TextColor?
                val keyColor: TextColor?

                var namespaceString: Component = Component.text(namespacedKey.namespace)
                var keyString: Component = Component.text(namespacedKey.key)

                if (queue.hasNext()) {
                    namespaceColor = TextColor.fromCSSHexString(queue.pop().value())
                    if (namespaceColor != null) {
                        namespaceString = namespaceString.color(namespaceColor)
                        keyString = keyString.color(namespaceColor)
                    }
                    if (queue.hasNext()) {
                        keyColor = TextColor.fromCSSHexString(queue.pop().value())
                        if (keyColor != null) {
                            keyString = keyString.color(keyColor)
                        }
                    }
                }
                Tag.selfClosingInserting(
                    Component.empty()
                        .append(namespaceString)
                        .append(Component.text(":"))
                        .append(keyString)
                )
            }
        }

        override fun nodeSelection(key: String, nodesSupplier: Supplier<Collection<Node>>): TagResolver {
            return TagResolver.resolver(key) { argumentQueue: ArgumentQueue?, context: Context? ->
                var nodes = nodesSupplier.get()
                val size = nodes.size
                nodes = CollectionUtils.subList(ArrayList(nodes), Range(0, 30))
                Tag.selfClosingInserting(GEN_NODE_SEL.formatted(
                    Placeholder.unparsed("amount", size.toString() + "")
                ).asComponent().hoverEvent(HoverEvent.showText(Component.join(
                    JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
                    nodes.stream().map { formatNode(it) }
                        .collect(Collectors.toList()))
                )))
            }
        }

        override fun permission(key: String, permission: String?): TagResolver {
            return TagResolver.resolver(key) { argumentQueue: ArgumentQueue, context: Context? ->
                val col = if (argumentQueue.hasNext()) argumentQueue.pop().value() else null
                val nullVal = if (argumentQueue.hasNext()) argumentQueue.pop().value() else "none"
                val permStyle = if (col == null
                ) if (permission == null) nullStyle else textStyle
                else Style.style(TextColor.fromCSSHexString(col))
                Tag.inserting(if (permission == null
                ) miniMessage.deserialize(nullVal)
                else Component.join(
                    JoinConfiguration.separator(Component.text(".")),
                    Arrays.stream(permission.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray())
                        .map { s: String? ->
                            Component.text(
                                s!!, permStyle
                            )
                        }.collect(Collectors.toList())
                )
                )
            }
        }

        override fun vector(key: String, vector: Vector): TagResolver {
            return Placeholder.component(
                key, GEN_VECTOR.formatted(
                    number("x", vector.x),
                    number("y", vector.y),
                    number("z", vector.z)
                )
            )
        }

        override fun particle(key: String, particle: Particle, data: Any): TagResolver {
            return Placeholder.component(
                key, if (data == null
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

        override fun <C : ComponentLike> list(key: String, entries: Collection<C>): TagResolver {
            return TagResolver.resolver(key) { queue: ArgumentQueue, _ ->
                val e: Collection<C> = CollectionUtils.subList(ArrayList(entries), Range(0, 30))
                var separator: ComponentLike = Component.text(", ", NamedTextColor.GRAY)
                var prefix: ComponentLike? = null
                var suffix: ComponentLike? = null

                if (queue.hasNext()) {
                    separator = miniMessage.deserialize(queue.pop().value())
                }
                if (queue.hasNext()) {
                    prefix = miniMessage.deserialize(queue.pop().value())
                }
                if (queue.hasNext()) {
                    suffix = miniMessage.deserialize(queue.pop().value())
                }
                val finalPrefix = prefix
                val finalSuffix = suffix
                Tag.selfClosingInserting(Component.join(JoinConfiguration.builder().separator(separator).build(),
                    e.stream().map { c: C ->
                        if (finalPrefix == null) {
                            return@map c
                        }
                        if (finalSuffix == null) {
                            return@map Component.empty().append(finalPrefix).append(c)
                        }
                        Component.empty().append(finalPrefix).append(c).append(finalSuffix)
                    }.collect(Collectors.toList())
                )
                )
            }
        }

        override fun <C> list(key: String, entries: Collection<C>, renderer: Function<C, ComponentLike>): TagResolver {
            val componentLikes: MutableCollection<ComponentLike> = ArrayList()
            for (entry in entries) {
                componentLikes.add(renderer.apply(entry))
            }
            return list(key, componentLikes)
        }

        override fun modifiers(key: String, modifiers: Collection<Modifier>): TagResolver {
            return list(key, modifiers) {
                val type = PathFinder.get().modifierRegistry.getType<Modifier>(it.key)
                if (type.isPresent && type.get() is ModifierCommandExtension<*>) {
                    (type as ModifierCommandExtension<*>).toComponents(it as Nothing)
                } else {
                    Component.text("Unknown modifier '${it.key}'.")
                }
            }
        }
    }
}
