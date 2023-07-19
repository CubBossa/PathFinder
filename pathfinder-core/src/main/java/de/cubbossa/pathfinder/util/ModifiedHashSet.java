package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.Changes;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

public class ModifiedHashSet<E> extends HashSet<E> {

  @Getter
  private final Changes<E> changes;

  public ModifiedHashSet(Changes<E> changes) {
    this.changes = changes;
  }

  public ModifiedHashSet() {
    this(new Changes<>());
  }

  public ModifiedHashSet(Changes<E> changes, Collection<E> iterable) {
    this(changes);
    addAll(iterable);
  }

  public ModifiedHashSet(Collection<E> iterable) {
    this(new Changes<>(), iterable);
  }

  @Override
  public boolean add(E e) {
    if (super.add(e)) {
      changes.getAddList().add(e);
      return true;
    }
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    changes.getAddList().addAll(c);
    return super.addAll(c);
  }

  @Override
  public boolean remove(Object o) {
    if (super.remove(o)) {
      changes.getRemoveList().add((E) o);
      return true;
    }
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    changes.getRemoveList().addAll((Collection<? extends E>) c);
    return super.removeAll(c);
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    return super.removeIf(e -> {
      if (filter.test(e)) {
        changes.getRemoveList().add(e);
        return true;
      }
      return false;
    });
  }
}
