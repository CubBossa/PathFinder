package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import org.bukkit.entity.Player;

public abstract class BukkitVisualizer<ViewT extends BukkitVisualizer<ViewT>.BukkitView>
    extends AbstractVisualizer<ViewT, Player> {

  public BukkitVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public Class<Player> getTargetType() {
    return Player.class;
  }

  public abstract class BukkitView extends AbstractVisualizer<ViewT, Player>.AbstractView {
    public BukkitView(PathPlayer<Player> targetViewer, UpdatingPath path) {
      super(targetViewer, path);
    }
  }
}
