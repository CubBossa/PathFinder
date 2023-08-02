package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;

public class BukkitParticlePlayer extends AbstractParticlePlayer<org.bukkit.Location> {

  private final Player player;

  public BukkitParticlePlayer(List<Location> locations, Player player) {
    super(locations);
    this.player = player;
  }

  @Override
  Location getView() {
    return BukkitVectorUtils.toInternal(player.getEyeLocation());
  }

  @Override
  void playParticle(org.bukkit.Location location) {
    player.spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0);
  }

  @Override
  org.bukkit.Location convert(Location location) {
    return BukkitVectorUtils.toBukkit(location);
  }
}
