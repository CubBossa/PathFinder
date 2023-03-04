package de.cubbossa.pathfinder.data;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jooq.Converter;

public class NamespacedKeyConverter implements Converter<String, NamespacedKey> {
  @Override
  public NamespacedKey from(String databaseObject) {
    return NamespacedKey.fromString(databaseObject);
  }

  @Override
  public String to(NamespacedKey userObject) {
    return userObject.toString();
  }

  @Override
  public @NotNull Class<String> fromType() {
    return String.class;
  }

  @Override
  public @NotNull Class<NamespacedKey> toType() {
    return NamespacedKey.class;
  }
}
