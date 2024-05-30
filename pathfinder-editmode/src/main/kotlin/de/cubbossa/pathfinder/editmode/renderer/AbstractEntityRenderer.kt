package de.cubbossa.pathfinder.editmode.renderer

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.Maps
import de.cubbossa.cliententities.PlayerSpace
import de.cubbossa.cliententities.entity.ClientEntity
import de.cubbossa.menuframework.inventory.Action
import de.cubbossa.menuframework.inventory.InvMenuHandler
import de.cubbossa.menuframework.inventory.context.TargetContext
import de.cubbossa.pathfinder.editor.GraphRenderer
import de.cubbossa.pathfinder.misc.PathPlayer
import lombok.Getter
import lombok.Setter
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.util.function.Consumer
import kotlin.math.pow

@Getter
@Setter
abstract class AbstractEntityRenderer<ElementT, DisplayT : Display>(
    plugin: JavaPlugin,
    private val entityClass: Class<DisplayT>
) : GraphRenderer<Player> {

    private val playerSpace: PlayerSpace = PlayerSpace.create().withEventSupport().build()

    @JvmField
    protected val players: MutableCollection<PathPlayer<Player>> = HashSet()

    @JvmField
    val entityNodeMap: BiMap<DisplayT?, ElementT> = Maps.synchronizedBiMap(HashBiMap.create())
    private val interactionNodeMap: BiMap<Interaction, ElementT> =
        Maps.synchronizedBiMap(HashBiMap.create())
    private var renderDistance: Double = 0.0
    private var renderDistanceSquared: Double = 0.0

    private val listeners: MutableCollection<PlayerSpace.Listener<*>> = HashSet()

    init {
        listeners.add(playerSpace.registerListener(PlayerInteractAtEntityEvent::class.java) { e: PlayerInteractAtEntityEvent ->
            this.onClick(
                e
            )
        })
        listeners.add(playerSpace.registerListener(EntityDamageByEntityEvent::class.java) { e: EntityDamageByEntityEvent ->
            this.onHit(
                e
            )
        })
    }

    override fun dispose() {
        listeners.forEach(Consumer { listener: PlayerSpace.Listener<*>? ->
            playerSpace.unregisterListener(
                listener
            )
        })
        try {
            playerSpace.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun setRenderDistance(renderDistance: Double) {
        this.renderDistance = renderDistance
        this.renderDistanceSquared = renderDistance.pow(2.0)
    }

    abstract fun equals(a: ElementT, b: ElementT): Boolean

    abstract suspend fun location(element: ElementT): Location

    abstract fun render(element: ElementT, entity: DisplayT)

    abstract fun hitbox(element: ElementT, entity: Interaction)

    abstract fun handleInteract(
        player: Player,
        slot: Int,
        left: Boolean
    ): Action<TargetContext<ElementT>>

    suspend fun showElements(elements: Collection<ElementT>, player: Player) {
        for (element in elements) {
            showElement(element, player)
        }
        playerSpace.announce()
    }

    suspend fun showElement(element: ElementT, player: Player) {
        playerSpace.addPlayerIfAbsent(player)
        if (entityNodeMap.inverse().containsKey(element)) {
            return updateElement(element, player)
        }

        val location: Location = location(element)
        try {
            val entity =
                playerSpace.spawn<DisplayT, ClientEntity>(location, entityClass) as DisplayT
            entity.viewRange = (renderDistance / 64.0).toFloat()

            entityNodeMap.inverse()[element] = entity
            entityNodeMap[entity] = element

            render(element, entity)

            val interaction: Interaction = playerSpace.spawn(location, Interaction::class.java)
            interactionNodeMap[interaction] = element
            hitbox(element, interaction)
        } catch (t: Throwable) {
            hideElements(setOf(element), player)
            throw t
        }
    }

    suspend fun updateElement(element: ElementT, player: Player) {
        val display = entityNodeMap.inverse()[element]
        if (display!!.isDead) {
            entityNodeMap.remove(display)
            return showElement(element, player)
        }
        val prev = display.location
        val loc: Location = location(element)
        // update position if position changed
        if (prev != loc) {
            display.teleport(loc)

            val interaction = interactionNodeMap.inverse()[element]
            interaction!!.teleport(loc)
            hitbox(element, interaction)
        }
    }

    fun hideElements(elements: Collection<ElementT>, player: Player) {
        val map: MutableMap<ElementT, DisplayT?> = entityNodeMap.inverse()
        val interact: MutableMap<ElementT, Interaction> = interactionNodeMap.inverse()

        // make hashset to make sure that elements was not the keyset of either map
        for (e in HashSet<ElementT>(elements)) {
            val e1: Entity? = map.remove(e)
            val e2: Entity? = interact.remove(e)
            if (e1 == null && e2 == null) {
                continue
            }
            e1?.remove()
            e2?.remove()
        }
        playerSpace.announce()
    }

    fun onClick(e: PlayerInteractEntityEvent) {
        if (e.rightClicked is Interaction) {
            handleInteract(e.rightClicked as Interaction, e.player, false)
        }
    }

    fun onHit(e: EntityDamageByEntityEvent) {
        if (e.entity is Interaction && e.damager is Player) {
            handleInteract(e.entity as Interaction, e.damager as Player, true)
        }
    }

    private fun handleInteract(e: Interaction, player: Player, left: Boolean) {
        val element = interactionNodeMap[e] ?: return
        val slot = player.inventory.heldItemSlot
        val menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot)
            ?: return
        val action = handleInteract(player, slot, left)
        menu.handleInteract(action, TargetContext(player, menu, slot, action, true, element))
    }
}