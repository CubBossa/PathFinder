package de.cubbossa.pathfinder.node;

import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathapi.storage.NodeDataStorage;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import de.cubbossa.pathapi.misc.NamespacedKey;
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
