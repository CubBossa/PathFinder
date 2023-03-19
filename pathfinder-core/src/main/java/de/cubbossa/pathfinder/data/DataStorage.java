package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;

public interface DataStorage extends ApplicationLayer, NodeDataStorage<Waypoint> {

  default void connect() throws IOException {
    connect(() -> {
    });
  }

  /**
   * Sets up the database files or does nothing if the database is already setup.
   * If the database hasn't yet existed, the initial callback will be executed.
   *
   * @param initial A callback to be executed if the database was initially created
   */
  void connect(Runnable initial) throws IOException;

  void disconnect();

  @Override
  default CompletableFuture<Void> teleportNode(UUID nodeId, Location location) {
    return updateNode(nodeId, node -> node.setLocation(location));
  }
}
