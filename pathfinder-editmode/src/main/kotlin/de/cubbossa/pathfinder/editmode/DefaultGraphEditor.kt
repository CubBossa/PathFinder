package de.cubbossa.pathfinder.editmode

import de.cubbossa.disposables.Disposable
import de.cubbossa.menuframework.inventory.Action
import de.cubbossa.menuframework.inventory.context.TargetContext
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu
import de.cubbossa.pathfinder.PathFinder
import de.cubbossa.pathfinder.PathFinderPlugin
import de.cubbossa.pathfinder.editmode.menu.EditModeMenu
import de.cubbossa.pathfinder.editor.GraphEditor
import de.cubbossa.pathfinder.editor.GraphRenderer
import de.cubbossa.pathfinder.event.NodeCreateEvent
import de.cubbossa.pathfinder.event.NodeDeleteEvent
import de.cubbossa.pathfinder.event.NodeGroupDeleteEvent
import de.cubbossa.pathfinder.event.NodeSaveEvent
import de.cubbossa.pathfinder.group.NodeGroup
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.util.BukkitUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lombok.SneakyThrows
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

class DefaultGraphEditor(group: NodeGroup) : GraphEditor<Player>, GraphRenderer<Player>, Listener {

    private val pathFinder: PathFinder = PathFinder.get()
    override val groupKey: NamespacedKey = group.key
    override val isEdited: Boolean
        get() = editingPlayers.isNotEmpty()

    private val editingPlayers: MutableMap<PathPlayer<Player>, BottomInventoryMenu> = HashMap()
    private val preservedGameModes: MutableMap<PathPlayer<Player>, GameMode> = HashMap()

    internal val renderers: MutableCollection<GraphRenderer<Player>> = ArrayList()
    private val listeners: MutableCollection<de.cubbossa.pathfinder.event.Listener<*>>
    private val entityInteractListener: EntityInteractListener

    private val renderExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val eventDispatcher = PathFinder.get().eventDispatcher
        listeners = HashSet()

        entityInteractListener = EntityInteractListener()
        PathFinder.get().disposer.register(this, entityInteractListener)

        listeners.add(eventDispatcher.listen(NodeCreateEvent::class.java) { e: NodeCreateEvent ->
            runBlocking {
                launch {
                    renderAll(
                        e.node
                    )
                }
            }
        })
        listeners.add(eventDispatcher.listen(NodeSaveEvent::class.java) { e: NodeSaveEvent ->
            runBlocking {
                launch {
                    renderAll(
                        e.node
                    )
                }
            }
        })
        listeners.add(eventDispatcher.listen(NodeDeleteEvent::class.java) { e: NodeDeleteEvent ->
            runBlocking {
                launch {
                    eraseAll(
                        e.node
                    )
                }
            }
        })
        Bukkit.getPluginManager()
            .registerEvents(entityInteractListener, PathFinderPlugin.getInstance())

        eventDispatcher.listen(NodeGroupDeleteEvent::class.java) { event: NodeGroupDeleteEvent ->
            if (event.group.key != groupKey) {
                return@listen
            }
            for (player in editingPlayers.keys) {
                setEditMode(player, false)
                player.sendMessage(Messages.EDITM_NG_DELETED)
            }
            PathFinder.get().disposer.dispose(this)
        }

        Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance())
    }

    fun addRenderer(renderer: GraphRenderer<Player>) {
        renderers.add(renderer)
        PathFinder.get().disposer.register(this, renderer)
    }

    private suspend fun renderAll(node: Node) {
        renderAll(setOf(node))
    }

    private suspend fun renderAll(nodes: Collection<Node>) {
        for (player in editingPlayers.keys) {
            val sorted: List<Node> =
                ArrayList(nodes).sortedWith(Comparator.comparing { node: Node ->
                    node.location.distanceSquared(player.location)
                })
            renderNodes(player, sorted)
        }
    }

    private suspend fun eraseAll(node: Node) {
        eraseAll(setOf(node))
    }

    private suspend fun eraseAll(nodes: Collection<Node>) {
        for (player in editingPlayers.keys) {
            eraseNodes(player, nodes)
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val p = BukkitUtils.wrap(event.player)
        val menu = editingPlayers[p] ?: return
        event.isCancelled = menu.handleInteract(
            INTERACT, TargetContext(
                event.player, menu, event.player.inventory.heldItemSlot,
                INTERACT, true, event
            )
        )
    }

    @SneakyThrows
    override fun dispose() {
        cancelEditModes()

        PlayerInteractEvent.getHandlerList().unregister(this)
        listeners.forEach(Consumer { listener: de.cubbossa.pathfinder.event.Listener<*>? ->
            pathFinder.eventDispatcher.drop(
                listener
            )
        })
        renderExecutor.shutdown()
    }

    override fun toggleEditMode(player: PathPlayer<Player>): Boolean {
        val isEditing = isEditing(player)
        setEditMode(player, !isEditing)
        return !isEditing
    }

    override fun cancelEditModes() {
        for (player in editingPlayers.keys) {
            setEditMode(player, false)
        }
    }

    /**
     * Sets a player into edit mode for this roadmap.
     *
     * @param player   the player to set the edit mode for
     * @param activate activate or deactivate edit mode
     */
    override fun setEditMode(player: PathPlayer<Player>, activate: Boolean) {
        val bukkitPlayer = player.unwrap()

        if (activate) {
            if (bukkitPlayer == null || !bukkitPlayer.isOnline) {
                return
            }

            val menu = EditModeMenu(
                pathFinder.storage, groupKey,
                pathFinder.nodeTypeRegistry.types,
                pathFinder.configuration.editMode
            ).createHotbarMenu(this, bukkitPlayer)
            editingPlayers[player] = menu
            menu.openSync(bukkitPlayer)

            preservedGameModes[player] = bukkitPlayer.gameMode
            bukkitPlayer.gameMode = GameMode.CREATIVE

            pathFinder.storage.loadGroup(groupKey).thenCompose { group: Optional<NodeGroup?> ->
                pathFinder.storage.loadNodes(group.orElseThrow())
                    .thenAccept { n: Collection<Node> ->
                        runBlocking { launch { renderNodes(player, n) } }
                    }
            }
        } else {
            if (bukkitPlayer != null && bukkitPlayer.isOnline) {
                val menu = editingPlayers[player]
                menu?.close(bukkitPlayer)
                bukkitPlayer.gameMode = preservedGameModes.getOrDefault(player, GameMode.SURVIVAL)
            }
            runBlocking { launch { clear(player) } }
            editingPlayers.remove(player)
        }
    }

    override fun isEditing(player: PathPlayer<Player>): Boolean {
        return editingPlayers.containsKey(player)
    }

    fun isEditing(player: Player): Boolean {
        return isEditing(PathPlayer.wrap(player))
    }

    override suspend fun clear(player: PathPlayer<Player>) {
        for (renderer in renderers) {
            renderer.clear(player)
        }
    }

    override suspend fun renderNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        for (renderer in renderers) {
            renderer.renderNodes(player, nodes)
        }
    }

    override suspend fun eraseNodes(player: PathPlayer<Player>, nodes: Collection<Node>) {
        for (renderer in renderers) {
            renderer.eraseNodes(player, nodes)
        }
    }

    private inner class EntityInteractListener : Listener, Disposable {
        @EventHandler
        fun onInteract(e: PlayerInteractEntityEvent) {
            val player = BukkitUtils.wrap(e.player)
            if (!editingPlayers.containsKey(player)) {
                return
            }
            val slot = e.player.inventory.heldItemSlot
            // slots 0-5 are part of the editmode -> cancel interaction
            // it does not affect interacting with nodes or edges, those are handled beforehand
            // and are no valid entities, hence they don't trigger actual Events
            if (slot < 5) {
                e.isCancelled = true
            }
        }

        override fun dispose() {
            PlayerInteractAtEntityEvent.getHandlerList().unregister(this)
        }
    }

    companion object {
        val INTERACT: Action<TargetContext<PlayerInteractEvent>> = Action()
    }
}
