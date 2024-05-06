package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import java.util.Objects;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BukkitParticlePlayer extends AbstractParticlePlayer<org.bukkit.Location> {

  private final Player player;
  private final Particle particle;
  private final Object particleData;
  private final int amount;
  private final Vector offset;
  private final double speed;
  private long lastException = 0;

  public BukkitParticlePlayer(Player player, Particle particle, Object particleData, int amount,
                              Vector offset, double speed) {
    this.player = player;
    this.particle = particle;
    this.particleData = particleData;
    this.amount = amount;
    this.offset = offset;
    this.speed = speed;
  }

  @Override
  Location getView() {
    return BukkitVectorUtils.toInternal(player.getEyeLocation());
  }

  @Override
  void playParticle(org.bukkit.Location location) {
    if (particle.getDataType() != Void.class && !Objects.equals(particleData.getClass(), particle.getDataType())) {
      if (System.currentTimeMillis() - lastException < 3000) {
        return;
      }
      lastException = System.currentTimeMillis();
      throw new IllegalStateException("Particle data is of wrong type - given: "
          + particleData.getClass().getName() + ", required: " + particle.getDataType().getName());
    }
    player.spawnParticle(particle, location, amount, offset.getX(), offset.getY(), offset.getZ(), speed, particleData);
  }

  @Override
  org.bukkit.Location convert(Location location) {
    return BukkitVectorUtils.toBukkit(location);
  }

  @Override
  Location convert(org.bukkit.Location location) {
    return BukkitVectorUtils.toInternal(location);
  }
}
