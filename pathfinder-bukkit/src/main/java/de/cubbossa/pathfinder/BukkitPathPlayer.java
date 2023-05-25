package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import de.cubbossa.translations.Message;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class BukkitPathPlayer implements PathPlayer<Player> {

  private final UUID uuid;
  private @Nullable Player player;

  public BukkitPathPlayer(UUID playerId) {
    this.uuid = playerId;
  }

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
    BukkitPathPlayer that = (BukkitPathPlayer) o;
    return Objects.equals(uuid, that.uuid);
  }

  @Override
  public UUID getUniqueId() {
    return uuid;
  }

  @Override
  public Class<Player> getPlayerClass() {
    return Player.class;
  }

  @Override
  public String getName() {
    return unwrap().getName();
  }

  @Override
  public Component getDisplayName() {
    return BukkitPathFinder.getInstance().getAudiences().player(uuid).getOrDefault(Identity.DISPLAY_NAME, Component.text(unwrap().getName()));
  }

  @Override
  public Location getLocation() {
    return BukkitVectorUtils.toInternal(unwrap().getLocation());
  }

  @Override
  public boolean hasPermission(String permission) {
    return unwrap().hasPermission(permission);
  }

  @Override
  public Player unwrap() {
    if (player == null) {
      player = Bukkit.getPlayer(uuid);
    }
    return player;
  }

  @Override
  public void sendMessage(ComponentLike message) {
    Audience audience = PathFinderProvider.get().getAudiences().player(unwrap().getUniqueId());
    ComponentLike resolved = message;
    if (message instanceof Message msg) {
      resolved = msg.asComponent(audience);
    }
    audience.sendMessage(resolved);
  }
}
