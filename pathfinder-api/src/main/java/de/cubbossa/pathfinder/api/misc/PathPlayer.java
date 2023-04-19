package de.cubbossa.pathfinder.api.misc;

import java.util.UUID;

public interface PathPlayer<P> {

  UUID getUniqueId();
  Class<P> getPlayerClass();
  String getName();
  Location getLocation();
  boolean hasPermission(String permission);
  P unwrap();
}
