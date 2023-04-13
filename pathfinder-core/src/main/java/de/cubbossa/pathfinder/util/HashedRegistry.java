package de.cubbossa.pathfinder.util;

import java.util.HashMap;
import java.util.Iterator;

import de.cubbossa.pathfinder.api.misc.Keyed;
import de.cubbossa.pathfinder.api.misc.KeyedRegistry;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HashedRegistry<K extends Keyed>  extends HashMap<NamespacedKey, K> implements KeyedRegistry<K> {

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
