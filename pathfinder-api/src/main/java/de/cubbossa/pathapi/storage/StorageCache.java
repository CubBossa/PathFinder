package de.cubbossa.pathapi.storage;

public interface StorageCache<E> {

	void write(E e);

	void invalidate(E e);

	void invalidateAll();
}
