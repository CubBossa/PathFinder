package de.cubbossa.pathfinder.core.node;

import de.cubbossa.pathfinder.core.node.implementation.Waypoint;
import de.cubbossa.pathfinder.api.storage.NodeDataStorage;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class WaypointType extends AbstractNodeType<Waypoint> {

  public WaypointType(NodeDataStorage<Waypoint> storage, MiniMessage miniMessage) {
    super(
        NamespacedKey.fromString("pathfinder:waypoint"),
        "<color:#ff0000>Waypoint</color>",
        new ItemStack(Material.MAP),
        miniMessage,
        storage
    );
  }

  @Override
  public Waypoint createAndLoadNode(Context context) {
    return getStorage().createAndLoadNode(context);
  }
}
