package de.cubbossa.pathfinder.editmode.menu

import com.github.shynixn.mccoroutine.bukkit.launch
import de.cubbossa.menuframework.inventory.Action
import de.cubbossa.menuframework.inventory.Button
import de.cubbossa.menuframework.inventory.MenuPreset
import de.cubbossa.menuframework.inventory.MenuPresets
import de.cubbossa.menuframework.inventory.context.ClickContext
import de.cubbossa.menuframework.inventory.context.ContextConsumer
import de.cubbossa.menuframework.inventory.context.TargetContext
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu
import de.cubbossa.menuframework.inventory.implementations.ListMenu
import de.cubbossa.pathfinder.*
import de.cubbossa.pathfinder.PathFinderConfig.EditModeConfig
import de.cubbossa.pathfinder.editmode.DefaultGraphEditor
import de.cubbossa.pathfinder.editmode.createItemStack
import de.cubbossa.pathfinder.editmode.setGlow
import de.cubbossa.pathfinder.event.NodeDeleteEvent
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.Named
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Edge
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.node.NodeType
import de.cubbossa.pathfinder.storage.StorageAdapter
import de.cubbossa.pathfinder.storage.StorageUtil
import de.cubbossa.pathfinder.util.BukkitUtils
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import de.cubbossa.pathfinder.util.LocalizedItem
import de.cubbossa.pathfinder.util.VectorUtils
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkEffectMeta
import java.lang.Runnable
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

val RIGHT_CLICK_NODE: Action<TargetContext<Node>> = Action()
val LEFT_CLICK_NODE: Action<TargetContext<Node>> = Action()
val RIGHT_CLICK_EDGE: Action<TargetContext<Edge>> = Action()
val LEFT_CLICK_EDGE: Action<TargetContext<Edge>> = Action()

class EditModeMenu(
    private var storage: StorageAdapter,
    private var key: NamespacedKey,
    private var types: Collection<NodeType<*>>,
    config: EditModeConfig
) {

    private val GROUP_ITEM_LIST = arrayOf(
        Material.WHITE_CONCRETE,
        Material.ORANGE_CONCRETE,
        Material.MAGENTA_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE,
        Material.YELLOW_CONCRETE,
        Material.LIME_CONCRETE,
        Material.PINK_CONCRETE,
        Material.GRAY_CONCRETE,
        Material.LIGHT_GRAY_CONCRETE,
        Material.CYAN_CONCRETE,
        Material.PURPLE_CONCRETE,
        Material.BLUE_CONCRETE,
        Material.BROWN_CONCRETE,
        Material.GREEN_CONCRETE,
        Material.RED_CONCRETE,
        Material.BLACK_CONCRETE
    )

    private val multiTool: MutableCollection<NamespacedKey> = HashSet()
    private var undirectedEdgesMode: Boolean
    private var chainEdgeStart: UUID? = null

    private val lock = AtomicBoolean()

    init {
        this.undirectedEdgesMode = !config.isDirectedEdgesByDefault

        PathFinder.get().eventDispatcher.listen(NodeDeleteEvent::class.java) { e: NodeDeleteEvent ->
            if (chainEdgeStart == e.node.nodeId) {
                chainEdgeStart = null
            }
        }
    }

    fun Button.withItemStack(supplier: () -> ItemStack): Button {
        val btn = this
        PathFinderPlugin.getInstance().launch {
            btn.withItemStack(supplier)
        }
        return btn
    }

    fun <T : TargetContext<*>?> Button.withClickHandler(
        action: Action<T>,
        clickHandler: ContextConsumer<T>
    ): Button {
        val btn = this
        PathFinderPlugin.getInstance().launch {
            btn.withClickHandler(action, clickHandler)
        }
        return btn
    }

    fun createHotbarMenu(editor: DefaultGraphEditor, editingPlayer: Player?): BottomInventoryMenu {
        val menu = BottomInventoryMenu(0, 1, 2, 3, 4)

        menu.setDefaultClickHandler(Action.HOTBAR_DROP) { c: ClickContext ->
            Bukkit.getScheduler().runTaskLater(
                PathFinderPlugin.getInstance(),
                Runnable { editor.setEditMode(BukkitUtils.wrap(c.player), false) }, 1L
            )
        }

        menu.setButton(4, Button.builder()
            .withItemStack {
                LocalizedItem.Builder(ItemStack(if (undirectedEdgesMode) Material.RED_DYE else Material.LIGHT_BLUE_DYE))
                    .withName(
                        Messages.E_EDGEDIR_TOOL_N.formatted(
                            Messages.formatter().choice("value", !undirectedEdgesMode)
                        )
                    )
                    .withLore(Messages.E_EDGEDIR_TOOL_L)
                    .createItem(editingPlayer)
            }
            .withClickHandler(
                { c: TargetContext<*> ->
                    undirectedEdgesMode = !undirectedEdgesMode
                    val player = c.player

                    BukkitUtils.wrap(player).sendMessage(
                        Messages.E_NODE_TOOL_DIR_TOGGLE.formatted(
                            Messages.formatter().choice("value", !undirectedEdgesMode)
                        )
                    )
                    c.menu.refresh(c.slot)
                },
                Action.LEFT_CLICK_AIR,
                Action.LEFT_CLICK_BLOCK,
                Action.RIGHT_CLICK_AIR,
                Action.RIGHT_CLICK_BLOCK
            )
        )

        menu.setButton(0, Button.builder()
            .withItemStack {
                val stack = ItemStack(Material.FIREWORK_STAR)
                val meta = stack.itemMeta as FireworkEffectMeta?
                meta!!.addItemFlags(*ItemFlag.entries.toTypedArray())
                meta.effect =
                    FireworkEffect.builder() // green = no current chain, orange = chain started
                        .withColor(Color.fromRGB(if (chainEdgeStart == null) 0x00ff00 else 0xfc8a00))
                        .build()
                stack.setItemMeta(meta)
                LocalizedItem(stack, Messages.E_NODE_TOOL_N, Messages.E_NODE_TOOL_L).createItem(
                    editingPlayer
                )
            }

            .withClickHandler(LEFT_CLICK_NODE) { context: TargetContext<Node> ->
                runBlocking {
                    val p = context.player
                    if (!lock.compareAndSet(false, true)) {
                        BukkitUtils.wrap(p).sendMessage(Messages.GEN_TOO_FAST)
                        return@runBlocking
                    }
                    try {
                        storage.deleteNodes(setOf(context.target.nodeId))
                        p.playSound(p.location, Sound.ENTITY_ARMOR_STAND_BREAK, 1f, 1f)
                    } finally {
                        lock.set(false)
                    }
                }
            }

            .withClickHandler(Action.LEFT_CLICK_AIR) { context: ClickContext ->
                val p = PathPlayer.wrap(context.player)
                // cancel chain
                if (chainEdgeStart == null) {
                    return@withClickHandler
                }
                p.sendMessage(Messages.E_NODE_CHAIN_NEW)
                chainEdgeStart = null
                context.menu.refresh(context.slot)
            }

            .withClickHandler(RIGHT_CLICK_NODE) { context: TargetContext<Node> ->
                PathFinderPlugin.getInstance().launch(Dispatchers.Default) {
                    val p = context.player
                    val pp = PathPlayer.wrap(p)
                    if (chainEdgeStart == null) {
                        chainEdgeStart = context.target.nodeId
                        context.menu.refresh(context.slot)
                        pp.sendMessage(Messages.E_NODE_CHAIN_START)
                        return@launch
                    }
                    if (chainEdgeStart == context.target.nodeId) {
                        p.playSound(p.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                        return@launch
                    }
                    if (!lock.compareAndSet(false, true)) {
                        BukkitUtils.wrap(p).sendMessage(Messages.GEN_TOO_FAST)
                        return@launch
                    }
                    val jobs = HashSet<Deferred<Any>>()
                    jobs.add(async(Dispatchers.IO) {
                        storage.modifyNode(chainEdgeStart!!) { node: Node ->
                            node.connect(context.target.nodeId)
                        }
                    })
                    if (undirectedEdgesMode) {
                        jobs.add(async(Dispatchers.IO) {
                            storage.modifyNode(context.target.nodeId) { node: Node ->
                                node.connect(chainEdgeStart!!)
                            }
                        })
                    }
                    try {
                        jobs.joinAll()
                        chainEdgeStart = null
                        context.menu.refresh(context.slot)
                        PathPlayer.wrap(context.player).sendMessage(Messages.E_NODE_CHAIN_NEW)
                    } finally {
                        lock.set(false)
                    }
                }
            }

            .withClickHandler<TargetContext<Block>>(Action.RIGHT_CLICK_BLOCK) { context: TargetContext<Block> ->
                if (!lock.compareAndSet(false, true)) {
                    BukkitUtils.wrap(context.player).sendMessage(Messages.GEN_TOO_FAST)
                    return@withClickHandler
                }
                val view = context.player.eyeLocation
                val block = context.target.location
                val orientation = BukkitVectorUtils.getIntersection(
                    view.toVector(),
                    view.direction,
                    block.toVector()
                )
                if (orientation == null) {
                    lock.set(false)
                    return@withClickHandler
                }

                val pos = BukkitVectorUtils.toBukkit(
                    VectorUtils.snap(
                        BukkitVectorUtils.toInternal(orientation.location), 2
                    )
                )
                    .toLocation(block.world!!).add(orientation.direction.clone().multiply(.5f))

                val c = NodeType.Context(UUID.randomUUID(), BukkitVectorUtils.toInternal(pos))
                val applicableTypes: Collection<NodeType<*>> = types.stream()
                    .filter { nodeType: NodeType<*> -> nodeType.canBeCreated(c) }
                    .toList()
                if (applicableTypes.size > 1) {
                    openNodeTypeMenu(applicableTypes, context.player, pos)
                    lock.set(false)
                    return@withClickHandler
                }

                val type: NodeType<*>? = applicableTypes.stream().findAny().orElse(null)
                if (type == null) {
                    lock.set(false)
                    throw IllegalStateException("Could not find any node type to generate node.")
                }

                runBlocking {
                    launch {
                        val node =
                            storage.createAndLoadNode(type, BukkitVectorUtils.toInternal(pos))!!

                        try {
                            storage.modifyNode(node.nodeId) { n: Node ->
                                if (chainEdgeStart != null) {
                                    storage.modifyNode(chainEdgeStart) { o: Node ->
                                        o.connect(node)
                                    }
                                    if (undirectedEdgesMode) {
                                        n.connect(chainEdgeStart)
                                    }
                                }
                                chainEdgeStart = n.nodeId
                            }
                            storage.modifyGroup(key) { it.add(node.nodeId) }
                            storage.modifyGroup(AbstractPathFinder.globalGroupKey()) {
                                it.add(node.nodeId)
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        } finally {
                            lock.set(false)
                        }
                    }
                }
            }

            .withClickHandler(LEFT_CLICK_EDGE) { context: TargetContext<Edge> ->
                if (!lock.compareAndSet(false, true)) {
                    BukkitUtils.wrap(context.player).sendMessage(Messages.GEN_TOO_FAST)
                    return@withClickHandler
                }
                launchIO {
                    try {
                        storage.modifyNode(context.target.start) { node: Node ->
                            node.disconnect(context.target.end)
                        }
                    } finally {
                        lock.set(false)
                    }
                }
            }
        )

        menu.setButton(
            3, Button.builder()
                .withItemStack(
                    LocalizedItem(
                        Material.ENDER_PEARL, Messages.E_TP_TOOL_N,
                        Messages.E_TP_TOOL_L
                    ).createItem(editingPlayer)
                )
                .withClickHandler({ context: TargetContext<*> ->
                    launchCalc {
                        if (!lock.compareAndSet(false, true)) {
                            BukkitUtils.wrap(context.player).sendMessage(Messages.GEN_TOO_FAST)
                            return@runBlocking
                        }
                        val group = storage.loadGroup(key)!!
                        val nodes = storage.loadNodes(group)

                        val p = context.player
                        if (nodes.isEmpty()) {
                            // no nodes in the current editing
                            p.playSound(p.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                            return@runBlocking
                        }

                        var dist = -1.0
                        var nearest: Node? = null
                        val pLoc = context.player.location
                        for (node in nodes) {
                            val d = node.location.distance(BukkitVectorUtils.toInternal(pLoc))
                            if (dist == -1.0 || d < dist) {
                                nearest = node
                                dist = d
                            }
                        }

                        val newLoc = BukkitVectorUtils.toBukkit(nearest!!.location)
                            .setDirection(p.location.direction)

                        Bukkit.getScheduler().runTask(PathFinderPlugin.getInstance(), Runnable {
                            p.teleport(newLoc)
                            p.playSound(newLoc, Sound.ENTITY_FOX_TELEPORT, 1f, 1f)
                        })
                        lock.set(false)
                    }
                }, Action.RIGHT_CLICK_ENTITY, Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR)
        )

        menu.setButton(1, Button.builder()
            .withItemStack(
                LocalizedItem(
                    Material.CHEST, Messages.E_GROUP_TOOL_N,
                    Messages.E_GROUP_TOOL_L
                ).createItem(editingPlayer)
            )
            .withClickHandler(RIGHT_CLICK_NODE) { context: TargetContext<Node> ->
                runBlocking {
                    val node = storage.loadNode<Node>(context.target.nodeId)
                    node?.let { openGroupMenu(context.player, it) }
                }
            }
            .withClickHandler(LEFT_CLICK_NODE) { context: TargetContext<Node> ->
                if (!lock.compareAndSet(false, true)) {
                    BukkitUtils.wrap(context.player).sendMessage(Messages.GEN_TOO_FAST)
                    return@withClickHandler
                }
                try {
                    StorageUtil.clearGroups(context.target)
                    context.player.playSound(
                        context.player.location,
                        Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f, 1f
                    )
                } finally {
                    lock.set(false)
                }
            })

        menu.setButton(2, Button.builder()
            .withItemStack(
                LocalizedItem(
                    Material.ENDER_CHEST, Messages.E_MULTI_GROUP_TOOL_N,
                    Messages.E_MULTI_GROUP_TOOL_L
                ).createItem(editingPlayer)
            )
            .withClickHandler(RIGHT_CLICK_NODE) { context: TargetContext<Node> ->
                runBlocking {
                    if (!lock.compareAndSet(false, true)) {
                        BukkitUtils.wrap(context.player).sendMessage(Messages.GEN_TOO_FAST)
                        return@runBlocking
                    }
                    try {
                        val groups = storage.loadGroupsByMod(multiTool)
                        StorageUtil.addGroups(groups, context.target.nodeId)
                        context.player.playSound(
                            context.player.location,
                            Sound.BLOCK_CHEST_CLOSE,
                            1f,
                            1f
                        )
                    } finally {
                        lock.set(false)
                    }
                }
            }
            .withClickHandler(LEFT_CLICK_NODE) {
                runBlocking {
                    val groups = storage.loadGroupsByMod(multiTool)
                    StorageUtil.removeGroups(
                        groups,
                        it.target.nodeId
                    )
                    it.player.playSound(
                        it.player.location,
                        Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f, 1f
                    )
                }
            }
            .withClickHandler(Action.RIGHT_CLICK_AIR)
            { context: ClickContext ->
                openMultiToolMenu(
                    context.player
                )
            }
            .withClickHandler(Action.RIGHT_CLICK_BLOCK)
            { context: TargetContext<Block?> ->
                openMultiToolMenu(
                    context.player
                )
            }
        )
        return menu
    }

    private fun groupItem(group: NodeGroup): LocalizedItem.Builder {
        val mod = group.key.hashCode()

        val resolver = TagResolver.builder()
            .resolver(Messages.formatter().namespacedKey("key", group.key))
            .resolver(Messages.formatter().number("weight", group.weight))
            .resolver(Messages.formatter().modifiers("modifiers", group.modifiers))
            .build()

        return LocalizedItem.Builder(ItemStack(GROUP_ITEM_LIST[Math.floorMod(mod, 16)]))
            .withName(Messages.E_SUB_GROUP_ENTRY_N.formatted(resolver))
            .withLore(Messages.E_SUB_GROUP_ENTRY_L.formatted(resolver))
    }

    private fun openGroupMenu(player: Player, node: Node) {
        storage.loadAllGroups().thenAccept { nodeGroups: Collection<NodeGroup> ->
            val nodeGroupList =
                ArrayList(nodeGroups).sortedWith(Comparator.comparing { g: NodeGroup -> g.key.toString() })

            val menu = ListMenu(
                Messages.E_SUB_GROUP_TITLE.asComponent(
                    BukkitPathFinder.getInstance().audiences.player(player.uniqueId)
                ), 4
            )
            menu.addPreset(
                MenuPresets.fillRow(
                    ItemStack(Material.BLACK_STAINED_GLASS_PANE),
                    3
                )
            ) //TODO extract icon
            menu.addPreset(MenuPresets.paginationRow(3, 0, 1, false, Action.LEFT))
            for (group in nodeGroupList) {
                if (group.key == AbstractPathFinder.globalGroupKey()) {
                    continue
                }

                menu.addListEntry(Button.builder()
                    .withItemStack {
                        var stack = groupItem(group).createItem(player)
                        if (group.contains(node.nodeId)) {
                            stack = setGlow(stack)
                        }
                        stack
                    }
                    .withClickHandler(Action.LEFT, groupEntryClickHandler(menu, group, node))
                    .withClickHandler(Action.RIGHT, groupEntryClickHandler(menu, group, node))
                )
            }
            menu.addPreset { presetApplier: MenuPreset.PresetApplier ->
                presetApplier.addItemOnTop(
                    3 * 9 + 8,
                    LocalizedItem(
                        Material.BARRIER, Messages.E_SUB_GROUP_RESET_N,
                        Messages.E_SUB_GROUP_RESET_L
                    ).createItem(player)
                )
                presetApplier.addClickHandlerOnTop(3 * 9 + 8, Action.LEFT) { c: ClickContext ->
                    if (!lock.compareAndSet(false, true)) {
                        BukkitUtils.wrap(c.player).sendMessage(Messages.GEN_TOO_FAST)
                        return@addClickHandlerOnTop
                    }
                    StorageUtil.clearGroups(node).thenRun {
                        menu.refresh(*menu.listSlots)
                        c.player.playSound(
                            c.player.location,
                            Sound.ENTITY_WANDERING_TRADER_DRINK_MILK,
                            1f,
                            1f
                        )
                    }.whenComplete { unused: Void?, throwable: Throwable? ->
                        throwable?.printStackTrace()
                        lock.set(false)
                    }
                }
                presetApplier.addItemOnTop(
                    3 * 9 + 4,
                    LocalizedItem(
                        Material.PAPER, Messages.E_SUB_GROUP_INFO_N,
                        Messages.E_SUB_GROUP_INFO_L
                    ).createItem(player)
                )
            }
            menu.open(player)
        }
    }

    private fun groupEntryClickHandler(
        menu: ListMenu,
        group: NodeGroup,
        node: Node
    ): ContextConsumer<ClickContext> {
        return ContextConsumer { c: ClickContext ->
            if (!lock.compareAndSet(false, true)) {
                BukkitUtils.wrap(c.player).sendMessage(Messages.GEN_TOO_FAST)
                return@ContextConsumer
            }
            if (group.contains(node.nodeId)) {
                StorageUtil.removeGroups(group, node.nodeId).thenRun {
                    c.player.playSound(c.player.location, Sound.BLOCK_CHEST_CLOSE, 1f, 1f)
                    menu.refresh(*menu.listSlots)
                }.whenComplete { unused: Void?, throwable: Throwable? ->
                    throwable?.printStackTrace()
                    lock.set(false)
                }
            } else {
                StorageUtil.addGroups(group, node.nodeId).thenRun {
                    c.player.playSound(c.player.location, Sound.BLOCK_CHEST_OPEN, 1f, 1f)
                    menu.refresh(*menu.listSlots)
                }.whenComplete { unused: Void?, throwable: Throwable? ->
                    throwable?.printStackTrace()
                    lock.set(false)
                }
            }
        }
    }

    private fun openMultiToolMenu(player: Player) {
        storage.loadAllGroups().thenAccept { nodeGroups: Collection<NodeGroup> ->
            val nodeGroupList =
                ArrayList(nodeGroups).sortedWith(Comparator.comparing { g: NodeGroup -> g.key.toString() })

            val menu = ListMenu(
                Messages.E_SUB_GROUP_TITLE.asComponent(
                    BukkitPathFinder.getInstance().audiences.player(player.uniqueId)
                ), 4
            )
            menu.addPreset(
                MenuPresets.fillRow(
                    ItemStack(Material.BLACK_STAINED_GLASS_PANE),
                    3
                )
            ) //TODO extract icon
            menu.addPreset(MenuPresets.paginationRow(3, 0, 1, false, Action.LEFT))
            for (group in nodeGroupList) {
                if (group.key == AbstractPathFinder.globalGroupKey()) {
                    continue
                }

                menu.addListEntry(Button.builder()
                    .withItemStack {
                        var stack = groupItem(group).createItem(player)
                        if (multiTool.contains(group.key)) {
                            stack = setGlow(stack)
                        }
                        stack
                    }
                    .withClickHandler(Action.LEFT, multiToolEntryClickHandler(menu, group))
                    .withClickHandler(Action.RIGHT, multiToolEntryClickHandler(menu, group))
                )
            }
            menu.addPreset { presetApplier: MenuPreset.PresetApplier ->
                presetApplier.addItemOnTop(
                    3 * 9 + 8,
                    LocalizedItem(
                        Material.BARRIER, Messages.E_SUB_GROUP_RESET_N,
                        Messages.E_SUB_GROUP_RESET_L
                    ).createItem(player)
                )
                presetApplier.addClickHandlerOnTop(3 * 9 + 8, Action.LEFT) { c: ClickContext ->
                    multiTool.clear()
                    menu.refresh(*menu.listSlots)
                    c.player
                        .playSound(
                            c.player.location, Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f,
                            1f
                        )
                }
                presetApplier.addItemOnTop(
                    3 * 9 + 4,
                    LocalizedItem(
                        Material.PAPER, Messages.E_SUB_GROUP_INFO_N,
                        Messages.E_SUB_GROUP_INFO_L
                    ).createItem(player)
                )
            }
            menu.open(player)
        }
    }

    private fun multiToolEntryClickHandler(
        menu: ListMenu,
        group: NodeGroup
    ): ContextConsumer<ClickContext> {
        return ContextConsumer { c: ClickContext ->
            if (multiTool.contains(group.key)) {
                multiTool.remove(group.key)
                c.player.playSound(c.player.location, Sound.BLOCK_CHEST_CLOSE, 1f, 1f)
                menu.refresh(*menu.listSlots)
            } else {
                multiTool.add(group.key)
                c.player.playSound(c.player.location, Sound.BLOCK_CHEST_OPEN, 1f, 1f)
                menu.refresh(*menu.listSlots)
            }
        }
    }

    private fun openNodeTypeMenu(
        types: Collection<NodeType<*>>,
        player: Player,
        location: Location
    ) {
        val menu = ListMenu(Component.text("Choose a NodeType"), 2)
        for (type in types) {
            menu.addListEntry(Button.builder()
                .withItemStack { nodeTypeItem(type) }
                .withClickHandler(Action.RIGHT) {
                    storage.createAndLoadNode(type, BukkitVectorUtils.toInternal(location))
                    menu.close(player)
                })
        }
        menu.open(player)
    }

    private fun nodeTypeItem(type: NodeType<*>): ItemStack {
        val name = if (type is Named) {
            type.displayName
        } else {
            Component.text(type.key.toString())
        }
        return createItemStack(
            GROUP_ITEM_LIST[Math.floorMod(type.key.hashCode(), 16)],
            name
        )
    }
}