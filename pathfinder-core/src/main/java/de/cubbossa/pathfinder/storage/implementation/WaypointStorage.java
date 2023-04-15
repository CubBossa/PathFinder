package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathfinder.api.storage.NodeDataStorage;
import de.cubbossa.pathfinder.api.storage.Storage;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.WaypointDataStorage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WaypointStorage implements NodeDataStorage<Waypoint> {

  private final WaypointDataStorage implementation;

  public WaypointStorage(Storage storage) {
    if (storage.getImplementation() instanceof WaypointDataStorage wp) {
      implementation = wp;
    } else {
      throw new IllegalArgumentException("Storage implementation must also include waypoint methods.");
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
  public void deleteNode(Waypoint node) {
    implementation.deleteWaypoints(List.of(node));
  }
}
