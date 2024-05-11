package de.cubbossa.pathfinder.group;

import de.cubbossa.pathfinder.misc.Named;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DiscoverProgressModifier extends Modifier, Named {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:discover-progress");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  NamespacedKey getOwningGroup();

  CompletableFuture<Double> calculateProgress(UUID playerId);
}
