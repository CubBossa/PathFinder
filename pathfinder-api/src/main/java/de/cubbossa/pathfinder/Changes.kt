package de.cubbossa.pathfinder;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

@Getter
public final class Changes<E> {
  private final Collection<E> addList;
  private final Collection<E> removeList;

  public Changes() {
    addList = ConcurrentHashMap.newKeySet(16);
    removeList = ConcurrentHashMap.newKeySet(16);
  }

  public void flush() {
    addList.clear();
    removeList.clear();
  }
}
