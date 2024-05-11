package de.cubbossa.pathfinder.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModifiedHashMapTest {

  @Test
  void put() {
    ModifiedHashMap<Integer, String> map = new ModifiedHashMap<>();
    map.put(1, "a");
    assertEquals(1, map.getChanges().getAddList().size());
    assertEquals(0, map.getChanges().getRemoveList().size());
    assertEquals(Set.of("a"), map.getChanges().getAddList());

    map.getChanges().flush();

    assertEquals(0, map.getChanges().getAddList().size());
    assertEquals(0, map.getChanges().getRemoveList().size());
  }

  @Test
  void addCopy() {
    Object a = new Object();
    Object b = new Object();

    ModifiedHashMap<Integer, Object> map = new ModifiedHashMap<>();
    map.put(1, a);
    map.getChanges().flush();
    map.put(2, b);
    Set<Object> other = new HashSet<>();
    other.addAll(new HashSet<>(map.values()));
    other.addAll(map.getChanges().getAddList());
    other.add("x");

    assertEquals(1, map.getChanges().getAddList().size());
  }

  @Test
  void putAll() {
    ModifiedHashMap<Integer, String> map = new ModifiedHashMap<>();
    map.putAll(Map.of(1, "a", 2, "b"));

    assertEquals(2, map.size());
    assertEquals(2, map.getChanges().getAddList().size());
    assertEquals(0, map.getChanges().getRemoveList().size());
    assertEquals(Set.of("a", "b"), map.getChanges().getAddList());

    map.getChanges().flush();

    assertEquals(2, map.size());
    assertEquals(0, map.getChanges().getAddList().size());
    assertEquals(0, map.getChanges().getRemoveList().size());
  }

  @Test
  void remove() {
    ModifiedHashMap<Integer, String> map = new ModifiedHashMap<>();
    map.put(1, "a");
    map.getChanges().flush();
    map.remove(1);

    assertEquals(0, map.size());
    assertEquals(0, map.getChanges().getAddList().size());
    assertEquals(1, map.getChanges().getRemoveList().size());
    assertEquals(Set.of("a"), map.getChanges().getRemoveList());
  }

  @Test
  void testRemove() {
    ModifiedHashMap<Integer, String> map = new ModifiedHashMap<>();
    map.put(1, "a");
    map.getChanges().flush();
    map.remove(1, "a");

    assertEquals(0, map.size());
    assertEquals(0, map.getChanges().getAddList().size());
    assertEquals(1, map.getChanges().getRemoveList().size());
    assertEquals(Set.of("a"), map.getChanges().getRemoveList());
  }

  @Test
  void putIfAbsent() {
    ModifiedHashMap<Integer, String> map = new ModifiedHashMap<>();
    map.put(1, "a");
    assertEquals(1, map.getChanges().getAddList().size());
    assertEquals(0, map.getChanges().getRemoveList().size());
    assertEquals(Set.of("a"), map.getChanges().getAddList());

    map.getChanges().flush();

    assertEquals(0, map.getChanges().getAddList().size());
    assertEquals(0, map.getChanges().getRemoveList().size());

    map.putIfAbsent(1, "a");

    assertEquals(0, map.getChanges().getAddList().size());
    assertEquals(0, map.getChanges().getRemoveList().size());
  }
}