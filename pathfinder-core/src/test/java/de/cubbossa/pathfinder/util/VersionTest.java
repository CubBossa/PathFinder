package de.cubbossa.pathfinder.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VersionTest {

  @Test
  public void construct1() {
    Assertions.assertDoesNotThrow(() -> new Version("5.0.0-SNAPSHOT-636"));
    Assertions.assertDoesNotThrow(() -> new Version("v5.0.0-SNAPSHOT-b636"));
    Assertions.assertDoesNotThrow(() -> new Version("1.2.3-123"));
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Version("a"));
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Version("1a"));
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Version("a1.2.3"));
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Version("1.2.3-b123a"));
  }

  @Test
  public void compare1() {
    Version a = new Version("1.2");
    Version b = new Version("1");

    Assertions.assertEquals(1, a.compareTo(b));
  }

  @Test
  public void compare2() {
    Version a = new Version("1.2.3-b123");
    Version b = new Version("1.2.3-b123");

    Assertions.assertEquals(0, a.compareTo(b));
  }

  @Test
  public void compare2a() {
    Version a = new Version("5.0.0-SNAPSHOT-636");
    Version b = new Version("v5.0.0-SNAPSHOT-b636");

    Assertions.assertEquals(0, a.compareTo(b));
  }

  @Test
  public void compare3() {
    Version a = new Version("123-b123");
    Version b = new Version("1.2.3-b123");

    Assertions.assertEquals(1, a.compareTo(b));
  }

  @Test
  public void compare4() {
    Version a = new Version("1-b1");
    Version b = new Version("1-b2");

    Assertions.assertEquals(-1, a.compareTo(b));
  }

  @Test
  public void compare5() {
    Version a = new Version("1");
    Version b = new Version("1-b2");

    Assertions.assertEquals(-1, a.compareTo(b));
  }

}