package de.cubbossa.pathfinder.citizens;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.visualizer.impl.EdgeBasedVisualizer;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CitizensPathVisualizer extends EdgeBasedVisualizer<CitizensPathVisualizer.View> {

  private NPC copyableNpc;

  public CitizensPathVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public View createView(UpdatingPath nodes, PathPlayer<Player> player) {
    return new View(player, nodes);
  }

  public class View extends EdgeBasedVisualizer<View>.EdgeBasedView {

    private NPC npc;

    public View(PathPlayer<Player> player, UpdatingPath nodes) {
      super(player, nodes);
    }

    @Override
    public void play(Location nearestPoint, Location leadPoint, Edge nearestEdge) {
      if (!npc.isSpawned()) {
        npc.spawn(nearestPoint);
      }
    }

    @Override
    public void dispose() {
      npc.destroy();
    }
  }
}
