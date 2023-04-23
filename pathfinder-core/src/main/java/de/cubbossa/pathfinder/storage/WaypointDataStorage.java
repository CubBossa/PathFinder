package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface WaypointDataStorage {

  Waypoint createAndLoadWaypoint(Location location);

  Optional<Waypoint> loadWaypoint(UUID uuid);

  Collection<Waypoint> loadWaypoints(Collection<UUID> ids);

  Collection<Waypoint> loadAllWaypoints();

  void saveWaypoint(Waypoint node);

  void deleteWaypoints(Collection<Waypoint> waypoints);

}
