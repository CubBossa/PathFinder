package de.cubbossa.pathfinder.util;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HashedRegistry<K extends Keyed> extends HashMap<NamespacedKey, K> implements Iterable<K> {

  @Nullable
  public K get(@NotNull NamespacedKey namespacedKey) {
    return super.get(namespacedKey);
  }

  @NotNull
  public Iterator<K> iterator() {
    return super.values().iterator();
  }

  public K put(K value) {
    return super.put(value.getKey(), value);
  }
}
