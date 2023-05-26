package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import org.bukkit.entity.Player;

public abstract class BukkitVisualizer<DataT>
    extends AbstractVisualizer<DataT, Player> {

  public BukkitVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public Class<Player> getTargetType() {
    return Player.class;
  }

  @Override
  public void destruct(PathPlayer<Player> player, DataT data) {
  }
}
