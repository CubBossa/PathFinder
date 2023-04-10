package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.storage.NodeDataStorage;
import de.cubbossa.pathfinder.storage.Storage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WaypointStorage implements NodeDataStorage<Waypoint> {

  private final Storage storage;


  @Override
  public Waypoint createAndLoadNode(Context context) {
    return storage.getImplementation().createAndLoadWaypoint(context.location());
  }

  @Override
  public Optional<Waypoint> loadNode(UUID uuid) {
    return storage.getImplementation().loadWaypoint(uuid);
  }

  @Override
  public Collection<Waypoint> loadNodes(Collection<UUID> ids) {
    return storage.getImplementation().loadWaypoints(ids);
  }

  @Override
  public Collection<Waypoint> loadAllNodes() {
    return storage.getImplementation().loadAllWaypoints();
  }

  @Override
  public void saveNode(Waypoint node) {
    storage.getImplementation().saveWaypoint(node);
  }

  @Override
  public void deleteNode(Waypoint node) {
    storage.getImplementation().deleteWaypoints(List.of(node));
  }
}
