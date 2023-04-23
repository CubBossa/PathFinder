package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.translations.TranslationHandler;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PathPlayerImpl implements PathPlayer<Player> {

  private final UUID uuid;
  private @Nullable Player player;

  public PathPlayerImpl(UUID playerId) {
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
    PathPlayerImpl that = (PathPlayerImpl) o;
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
  public Location getLocation() {
    return VectorUtils.toInternal(unwrap().getLocation());
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
    TranslationHandler.getInstance().getAudiences().player(unwrap()).sendMessage(message);
    // TODO TranslationHandler.getInstance().sendMessage(message, unwrap());
  }
}
