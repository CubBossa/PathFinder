package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DiscoverProgressModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:discover-progress");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  String getCommandKey();

  String getNameFormat();

  CompletableFuture<Double> calculateProgress(UUID playerId);
}
