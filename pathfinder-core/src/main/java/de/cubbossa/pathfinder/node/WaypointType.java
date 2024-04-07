package de.cubbossa.pathfinder.node;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.NodeStorageImplementation;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.implementation.NodeStorageImplementationWrapper;

@AutoService(NodeType.class)
public class WaypointType extends AbstractNodeType<Waypoint> {

  public WaypointType() {
    super(NamespacedKey.fromString("pathfinder:waypoint"), null);
  }

  @Override
  public Waypoint createNodeInstance(Context context) {
    Waypoint waypoint = new Waypoint(context.id());
    waypoint.setLocation(context.location());
    return waypoint;
  }

  @Override
  public NodeStorageImplementation<Waypoint> getStorage() {
    if (storage == null && PathFinderProvider.get() != null) {
      storage = new NodeStorageImplementationWrapper(PathFinderProvider.get().getStorage());
    }
    return storage;
  }
}
