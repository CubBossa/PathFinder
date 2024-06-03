package de.cubbossa.pathfinder.node

import de.cubbossa.disposables.Disposable
import de.cubbossa.pathfinder.misc.Keyed
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.storage.NodeStorageImplementation
import org.pf4j.ExtensionPoint
import java.util.*

/**
 * A NodeType is the extension point that allows to register a new type of nodes.
 * The NodeType instance handles the storage individually. Waypoints as default NodeType might
 * suffice for the most cases and nodes are not supposed to hold functionality. Functionality should be provided
 * by applying NodeGroups to the Node.
 * <br></br><br></br>
 * Waypoints represent static nodes in the graph. Players are resembled by PlayerNodes and allow the PathView to update
 * when the player has moved.
 * <br></br>
 * You would want to implement this interface to translate plugin data into nodes (ChestShops, NPCs, Quests) or to
 * introduce a moving waypoint type (for example if a player needs to jump on a moving platform).
 * <br></br>
 * Implement [de.cubbossa.pathfinder.misc.Named] to provide a component display name in some situations.
 *
 * @param <N> The Node type
</N> */
interface NodeType<N : Node> : Keyed, Disposable, NodeStorageImplementation<N>, ExtensionPoint {
    /**
     * If the node can be created in a certain context.
     * Might return false if the player is not allowed to place nodes in certain locations or if the NodeType
     * is readonly and translates another plugins data into nodes. (Citizens NPCs -> Waypoints / ChestShops -> Waypoints)
     * <br></br><br></br>
     * Editors might not provide the possibility to create nodes of this type if this method returns false.
     *
     * @param context The context of creation
     * @return true if the node can be created.
     */
    fun canBeCreated(context: Context): Boolean {
        return true
    }

    /**
     * Creates a new node instance and saves it to storage.
     * @param context The context of node creation including id and location to apply.
     * @return The new node instance.
     */
    fun createAndLoadNode(context: Context): N?

    /**
     * The context for node creation
     * @param id a uuid to use for the new node
     * @param location the location of the new node
     */
    @JvmRecord
    data class Context(@JvmField val id: UUID, @JvmField val location: Location)
}
