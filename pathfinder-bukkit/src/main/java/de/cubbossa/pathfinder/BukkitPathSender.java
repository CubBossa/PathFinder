package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.tinytranslations.Message;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

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
    Audience audience = PathFinder.get().getAudiences().console();
    ComponentLike resolved = message;
    if (message instanceof Message msg) {
      resolved = msg.asComponent(audience);
    }
    audience.sendMessage(resolved);
  }
}
