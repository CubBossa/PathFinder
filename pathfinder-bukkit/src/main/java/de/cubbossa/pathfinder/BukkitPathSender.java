package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Objects;
import java.util.UUID;

public class BukkitPathSender implements PathPlayer<ConsoleCommandSender> {

  @Override
  public int hashCode() {
    return getUniqueId().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BukkitPathSender that = (BukkitPathSender) o;
    return Objects.equals(getUniqueId(), that.getUniqueId());
  }

  @Override
  public UUID getUniqueId() {
    return new UUID(0, 0);
  }

  @Override
  public Class<ConsoleCommandSender> getPlayerClass() {
    return ConsoleCommandSender.class;
  }

  @Override
  public String getName() {
    return unwrap().getName();
  }

  @Override
  public Component getDisplayName() {
    return Component.text(getName());
  }

  @Override
  public Location getLocation() {
    return new Location(0, 0, 0, null);
  }

  @Override
  public boolean hasPermission(String permission) {
    return unwrap().hasPermission(permission);
  }

  @Override
  public ConsoleCommandSender unwrap() {
    return Bukkit.getConsoleSender();
  }

  @Override
  public void sendMessage(ComponentLike message) {
    Audience audience = PathFinderProvider.get().getAudiences().console();
    if (message.asComponent().compact().equals(Component.empty())) {
      return;
    }
    audience.sendMessage(message);
  }
}
