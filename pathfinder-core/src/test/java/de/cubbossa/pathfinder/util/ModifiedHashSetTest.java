package de.cubbossa.pathfinder.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModifiedHashSetTest {

  @Test
  void add() {
    ModifiedHashSet<String> set = new ModifiedHashSet<>();
    set.add("a");
    assertEquals(1, set.getChanges().getAddList().size());
    assertEquals(0, set.getChanges().getRemoveList().size());
    assertEquals(Set.of("a"), set.getChanges().getAddList());

    set.getChanges().flush();

    assertEquals(0, set.getChanges().getAddList().size());
    assertEquals(0, set.getChanges().getRemoveList().size());
  }

  @Test
  void addAll() {
    ModifiedHashSet<String> set = new ModifiedHashSet<>();
    set.addAll(List.of("a", "b"));

    assertEquals(2, set.size());
    assertEquals(2, set.getChanges().getAddList().size());
    assertEquals(0, set.getChanges().getRemoveList().size());
    assertEquals(Set.of("a", "b"), set.getChanges().getAddList());

    set.getChanges().flush();

    assertEquals(2, set.size());
    assertEquals(0, set.getChanges().getAddList().size());
    assertEquals(0, set.getChanges().getRemoveList().size());
  }

  @Test
  void remove() {
    ModifiedHashSet<String> set = new ModifiedHashSet<>();
    set.add("a");
    set.getChanges().flush();
    set.remove("a");

    assertEquals(0, set.size());
    assertEquals(0, set.getChanges().getAddList().size());
    assertEquals(1, set.getChanges().getRemoveList().size());
    assertEquals(Set.of("a"), set.getChanges().getRemoveList());
  }

  @Test
  void removeAll() {
    ModifiedHashSet<String> set = new ModifiedHashSet<>();
    set.addAll(Set.of("a", "b"));
    set.getChanges().flush();
    set.removeAll(Set.of("a", "b"));

    assertEquals(0, set.size());
    assertEquals(0, set.getChanges().getAddList().size());
    assertEquals(2, set.getChanges().getRemoveList().size());
    assertEquals(Set.of("a", "b"), set.getChanges().getRemoveList());
  }

  @Test
  void removeIf() {
    ModifiedHashSet<String> set = new ModifiedHashSet<>();
    set.addAll(Set.of("a", "b"));
    set.getChanges().flush();
    set.removeIf(s -> s.equalsIgnoreCase("A"));

    assertEquals(1, set.size());
    assertEquals(0, set.getChanges().getAddList().size());
    assertEquals(1, set.getChanges().getRemoveList().size());
    assertEquals(Set.of("a"), set.getChanges().getRemoveList());
    assertEquals(Set.of("b"), set);
  }
}