package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.NamespacedKey;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DiscoverProgressModifier extends Modifier {

  NamespacedKey KEY = NamespacedKey.fromString("pathfinder:discover-progress");

  @Override
  default NamespacedKey getKey() {
    return KEY;
  }

  NamespacedKey getOwningGroup();

  Component getDisplayName();

  String getNameFormat();

  CompletableFuture<Double> calculateProgress(UUID playerId);
}
