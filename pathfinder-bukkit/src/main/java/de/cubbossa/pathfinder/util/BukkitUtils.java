package de.cubbossa.pathfinder.util;


import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathPlayer;
import de.cubbossa.pathfinder.BukkitPathSender;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.UUID;

public class BukkitUtils {

  private static final UUID console = new UUID(0, 0);

  public static <PlayerT extends CommandSender> PathPlayer<PlayerT> wrap(PlayerT sender) {
    if (sender instanceof Player player) {
      return (PathPlayer<PlayerT>) new BukkitPathPlayer(player.getUniqueId());
    } else if (sender instanceof ConsoleCommandSender) {
      return (PathPlayer<PlayerT>) new BukkitPathSender();
    }
    throw new IllegalArgumentException("No implementation for type " + sender.getClass().getSimpleName());
  }

    public static Location lerp(Location a, Location b, double percent) {
        if (!Objects.equals(a.getWorld(), b.getWorld())) {
            throw new IllegalArgumentException("Both locations must be in the same world to be lerped.");
        }
        return lerp(a.toVector(), b.toVector(), percent).toLocation(a.getWorld());
    }

    public static Vector lerp(Vector a, Vector b, double percent) {
        return a.clone().add(b.clone().subtract(a).multiply(percent));
    }
}
