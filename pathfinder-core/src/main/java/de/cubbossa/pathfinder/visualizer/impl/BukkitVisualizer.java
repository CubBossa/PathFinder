package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import org.bukkit.entity.Player;

public abstract class BukkitVisualizer<T extends PathVisualizer<T, D, Player>, D>
    extends AbstractVisualizer<T, D, Player> {

  public BukkitVisualizer(NamespacedKey key, String nameFormat) {
    super(key, nameFormat);
  }

  @Override
  public Class<Player> getTargetType() {
    return Player.class;
  }

  @Override
  public void destruct(PathPlayer<Player> player, D data) {
  }
}
