package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.NodeStorageImplementation;
import de.cubbossa.pathfinder.storage.StorageAdapter;
import de.cubbossa.pathfinder.storage.WaypointStorageImplementation;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class NodeStorageImplementationWrapper implements NodeStorageImplementation<Waypoint> {

  private final WaypointStorageImplementation implementation;

  public NodeStorageImplementationWrapper(StorageAdapter storage) {
    if (storage.getImplementation() instanceof WaypointStorageImplementation wp) {
      implementation = wp;
    } else {
      throw new IllegalArgumentException(
          "StorageAdapter implementation must also include waypoint methods.");
    }
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
