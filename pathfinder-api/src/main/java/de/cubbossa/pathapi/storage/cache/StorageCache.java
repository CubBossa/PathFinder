package de.cubbossa.pathapi.storage.cache;

public interface StorageCache<E> {

  void write(E e);

  void invalidate(E e);

  void invalidateAll();
}
