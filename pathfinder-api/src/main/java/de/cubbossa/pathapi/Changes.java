package de.cubbossa.pathapi;

import lombok.Getter;

import java.util.LinkedList;

@Getter
public final class Changes<E> {
  private final LinkedList<E> addList;
  private final LinkedList<E> removeList;

  public Changes() {
    addList = new LinkedList<>();
    removeList = new LinkedList<>();
  }

  public void flush() {
    addList.clear();
    removeList.clear();
  }
}
