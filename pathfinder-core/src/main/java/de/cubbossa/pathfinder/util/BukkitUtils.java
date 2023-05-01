package de.cubbossa.pathfinder.util;


import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.BukkitPathPlayer;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

public class BukkitUtils {

    private static final UUID console = new UUID(0, 0);

    public static <PlayerT extends CommandSender> PathPlayer<PlayerT> wrap(PlayerT sender) {
        return new BukkitPathPlayer(sender); // TODO
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
