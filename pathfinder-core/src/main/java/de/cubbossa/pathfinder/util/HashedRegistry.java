package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HashedRegistry<K extends Keyed> extends HashMap<NamespacedKey, K>
    implements KeyedRegistry<K> {

  public HashedRegistry() {
    super();
  }

  public HashedRegistry(Map<? extends NamespacedKey, ? extends K> map) {
    super();
    this.putAll(map);
  }

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
