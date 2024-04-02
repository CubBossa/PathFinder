package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.storage.NodeStorageImplementation;
import de.cubbossa.pathapi.storage.StorageAdapter;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.WaypointStorageImplementation;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class WaypointStorageImplementationImpl implements NodeStorageImplementation<Waypoint> {

  private final WaypointStorageImplementation implementation;

  public WaypointStorageImplementationImpl(StorageAdapter storage) {
    if (storage.getImplementation() instanceof WaypointStorageImplementation wp) {
      implementation = wp;
    } else {
      throw new IllegalArgumentException(
          "StorageAdapter implementation must also include waypoint methods.");
    }
  }

  @Override
  public Waypoint createAndLoadNode(Context context) {
    return implementation.createAndLoadWaypoint(context.location());
  }

  @Override
  public Optional<Waypoint> loadNode(UUID uuid) {
    return implementation.loadWaypoint(uuid);
  }

  @Override
  public Collection<Waypoint> loadNodes(Collection<UUID> ids) {
    return implementation.loadWaypoints(ids);
  }

  @Override
  public Collection<Waypoint> loadAllNodes() {
    return implementation.loadAllWaypoints();
  }

  @Override
  public void saveNode(Waypoint node) {
    implementation.saveWaypoint(node);
  }

  @Override
  public void deleteNodes(Collection<Waypoint> nodes) {
    implementation.deleteWaypoints(nodes);
  }
}
