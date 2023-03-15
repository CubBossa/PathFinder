package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

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
  public Waypoint createNode(NodeCreationContext context) {
    Waypoint waypoint = new Waypoint(context.id(), context.persistent());
    waypoint.setLocation(context.location());
    return waypoint;
  }
}
