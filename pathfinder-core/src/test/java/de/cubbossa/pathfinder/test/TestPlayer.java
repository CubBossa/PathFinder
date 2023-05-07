package de.cubbossa.pathfinder.test;

import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathfinder.util.WorldImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.UUID;

public class TestPlayer implements PathPlayer<Object> {

  private final UUID uuid;

  public TestPlayer() {
    this(UUID.randomUUID());
  }

  public TestPlayer(UUID uuid) {
    this.uuid = uuid;
  }

  @Override
  public UUID getUniqueId() {
    return uuid;
  }

  @Override
  public Class<Object> getPlayerClass() {
    return Object.class;
  }

  @Override
  public String getName() {
    return "testname";
  }

  @Override
  public Component getDisplayName() {
    return Component.text(getName());
  }

  @Override
  public Location getLocation() {
    return new Location(0, 0, 0, new WorldImpl(UUID.randomUUID()));
  }

  @Override
  public boolean hasPermission(String permission) {
    return true;
  }

  @Override
  public Object unwrap() {
    return uuid;
  }

  @Override
  public void sendMessage(ComponentLike message) {

  }
}
