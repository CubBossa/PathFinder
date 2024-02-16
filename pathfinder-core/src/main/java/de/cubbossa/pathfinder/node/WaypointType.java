package de.cubbossa.pathfinder.node;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.NodeDataStorage;
import de.cubbossa.pathfinder.node.implementation.Waypoint;

public class WaypointType extends AbstractNodeType<Waypoint> {

  public WaypointType(NodeDataStorage<Waypoint> storage) {
    super(
        NamespacedKey.fromString("pathfinder:waypoint"),
        "<color:#ff0000>Waypoint</color>",
        storage
    );
  }

  @Override
  public Waypoint createAndLoadNode(Context context) {
    return getStorage().createAndLoadNode(context);
  }
}
