package de.cubbossa.pathfinder.editmode.renderer

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import de.cubbossa.cliententities.PlayerSpace
import de.cubbossa.cliententities.entity.ClientArmorStand
import de.cubbossa.cliententities.entity.ClientEntity
import de.cubbossa.menuframework.inventory.Action
import de.cubbossa.menuframework.inventory.InvMenuHandler
import de.cubbossa.menuframework.inventory.context.TargetContext
import de.cubbossa.pathfinder.editor.GraphRenderer
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.util.BukkitVectorUtils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import lombok.Getter
import lombok.Setter
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import kotlin.math.pow

@Getter
@Setter
abstract class AbstractArmorstandRenderer<T>(plugin: JavaPlugin) : GraphRenderer<Player> {
    @JvmField
    protected val players: MutableCollection<PathPlayer<Player>> = HashSet()

    private val playerSpaces: MutableMap<UUID, PlayerSpace> = HashMap()

    @JvmField
    val entityNodeMap: BiMap<ClientArmorStand, T> = HashBiMap.create()
    private val hiddenNodes: MutableMap<Player, MutableSet<T>> = ConcurrentHashMap()
    private var renderDistance: Double = 0.0
    private var renderDistanceSquared: Double = 0.0

    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            runBlocking {
                for (player in players) {
                    val jobs = HashSet<Deferred<Any>>()
                    val show = HashSet<T>()
                    val hide = HashSet<T>()
                    for (node in entityNodeMap.values) {
                        jobs.add(async {
                            val location: Location = retrieveFrom(node)
                            if (BukkitVectorUtils.toInternal(location)
                                    .distanceSquared(player.location) >= renderDistanceSquared
                            ) {
                                hide.add(node)
                            }
                        })
                    }
                    for (node in hiddenNodes.getOrDefault(player.unwrap(), HashSet<T>())) {
                        jobs.add(async {
                            val location: Location = retrieveFrom(node)
                            if (BukkitVectorUtils.toInternal(location)
                                    .distanceSquared(player.location) < renderDistanceSquared
                            ) {
                                show.add(node)
                            }
                        })
                    }
                    for (job in jobs) {
                        job.join()
                    }
                    val col: MutableCollection<T> =
                        hiddenNodes.computeIfAbsent(player.unwrap()) { u: Player? -> HashSet() }
                    col.addAll(hide)
                    col.removeAll(show)
                    showElements(show, player.unwrap())
                    hideElements(hide, player.unwrap())
                }
            }
        }, 1, 5)
    }

    fun setRenderDistance(renderDistance: Double) {
        this.renderDistance = renderDistance
        this.renderDistanceSquared = renderDistance.pow(2.0)
    }

    abstract fun equals(a: T, b: T): Boolean

    abstract fun head(element: T): ItemStack?

    abstract suspend fun retrieveFrom(element: T): Location

    abstract fun handleInteract(player: Player?, slot: Int, left: Boolean): Action<TargetContext<T>>

    abstract fun isSmall(element: T): Boolean

    abstract fun getName(element: T): Component?

    suspend fun showElements(elements: Collection<T>, player: Player) {
        for (element in elements) {
            showElement(element, player)
        }
    }

    open suspend fun showElement(element: T, player: Player) {
        if (entityNodeMap.values.stream().anyMatch { e: T -> equals(element, e) }) {
            updateElement(element, player)
            return
        }
        val location: Location = retrieveFrom(element)
        if (location.distanceSquared(player.location) > renderDistanceSquared) {
            hiddenNodes.computeIfAbsent(player) { HashSet() }.add(element)
            return
        }
        val armorStand: ArmorStand = ps(player).spawn(location, ArmorStand::class.java)
        armorStand.isSmall = isSmall(element)
        (armorStand as ClientEntity).setCustomName(getName(element))
        armorStand.setBasePlate(false)
        armorStand.isVisible = false
        armorStand.equipment!!.helmet = head(element)
        ps(player).announce()
        entityNodeMap[armorStand as ClientArmorStand] = element
    }

    protected fun ps(player: Player): PlayerSpace {
        return playerSpaces.computeIfAbsent(player.uniqueId) { uuid: UUID? ->
            val playerSpace: PlayerSpace = PlayerSpace.create().withPlayer(uuid).build()
            playerSpace.registerListener(PlayerInteractEntityEvent::class.java) { e: PlayerInteractEntityEvent ->
                this.onClick(
                    e
                )
            }
            playerSpace.registerListener(EntityDamageByEntityEvent::class.java) { e: EntityDamageByEntityEvent ->
                this.onHit(
                    e
                )
            }
            playerSpace
        }
    }

    private suspend fun updateElement(element: T, player: Player) {
        val prev = entityNodeMap.values.stream().filter { e: T -> equals(element, e) }.findAny()
            .orElseThrow()
        val prevLoc = retrieveFrom(prev)
        val loc = retrieveFrom(element)
        // update position if position changed
        if (prevLoc != loc) {
            val entity: Entity? = entityNodeMap.inverse()[element]
            entity!!.teleport(loc)
            ps(player).announce()
        }
    }

    fun hideElements(elements: Collection<T>, player: Player) {
        val nodeEntityMap: MutableMap<T, ClientArmorStand> = entityNodeMap.inverse()
        HashSet(elements).forEach(Consumer { e: T ->
            val present: Entity? = nodeEntityMap.remove(e)
            present?.remove()
        })
        ps(player).announce()
    }

    fun onClick(e: PlayerInteractEntityEvent) {
        handleInteract(e.rightClicked, e.player, false)
    }

    fun onHit(e: EntityDamageByEntityEvent) {
        if (e.damager is Player) {
            handleInteract(e.entity, e.damager as Player, true)
        }
    }

    private fun handleInteract(e: Entity, player: Player, left: Boolean) {
        if (e !is ClientArmorStand) {
            return
        }
        val element = entityNodeMap[e] ?: return
        val slot = player.inventory.heldItemSlot
        val menu = InvMenuHandler.getInstance().getMenuAtSlot(player, slot)
            ?: return
        val action = handleInteract(player, slot, left)
        menu.handleInteract(action, TargetContext(player, menu, slot, action, true, element))
    }
}