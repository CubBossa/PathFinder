package de.cubbossa.pathfinder.node;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.Keyed;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.storage.NodeStorageImplementation;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.pf4j.ExtensionPoint;

/**
 * A NodeType is the extension point that allows to register a new type of nodes.
 * The NodeType instance handles the storage individually. Waypoints as default NodeType might
 * suffice for the most cases and nodes are not supposed to hold functionality. Functionality should be provided
 * by applying NodeGroups to the Node.
 * <br/><br/>
 * Waypoints represent static nodes in the graph. Players are resembled by PlayerNodes and allow the PathView to update
 * when the player has moved.
 * <br/>
 * You would want to implement this interface to translate plugin data into nodes (ChestShops, NPCs, Quests) or to
 * introduce a moving waypoint type (for example if a player needs to jump on a moving platform).
 * <br/>
 * Implement {@link de.cubbossa.pathfinder.misc.Named} to provide a component display name in some situations.
 *
 * @param <N> The Node type
 */
public interface NodeType<N extends Node> extends Keyed, Disposable, NodeStorageImplementation<N>, ExtensionPoint {

  /**
   * If the node can be created in a certain context.
   * Might return false if the player is not allowed to place nodes in certain locations or if the NodeType
   * is readonly and translates another plugins data into nodes. (Citizens NPCs -> Waypoints / ChestShops -> Waypoints)
   * <br/><br/>
   * Editors might not provide the possibility to create nodes of this type if this method returns false.
   *
   * @param context The context of creation
   * @return true if the node can be created.
   */
  default boolean canBeCreated(Context context) {
    return true;
  }

  /**
   * Creates a new node instance and saves it to storage.
   * @param context The context of node creation including id and location to apply.
   * @return The new node instance.
   */
  @Nullable N createAndLoadNode(Context context);

  /**
   * The context for node creation
   * @param id a uuid to use for the new node
   * @param location the location of the new node
   */
  record Context(UUID id, Location location) {
  }
}
