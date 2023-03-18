package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class WaypointType extends NodeType<Waypoint> {

  public WaypointType() {
    super(
        new NamespacedKey(PathPlugin.getInstance(), "waypoint"),
        "<color:#ff0000>Waypoint</color>",
        new ItemStack(Material.MAP),
        PathPlugin.getInstance().getDatabase()
    );
  }

  @Override
  public CompletableFuture<Waypoint> createNode(NodeCreationContext context) {
    if (getStorage() != null) {
      return getStorage().createNode(context);
    }
    Waypoint waypoint = new Waypoint(UUID.randomUUID());
    waypoint.setLocation(context.location());
    return CompletableFuture.completedFuture(waypoint);
  }
}
