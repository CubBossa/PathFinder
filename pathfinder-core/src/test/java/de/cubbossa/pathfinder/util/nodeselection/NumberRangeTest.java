package de.cubbossa.pathfinder.util.nodeselection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.cubbossa.pathfinder.nodeselection.NumberRange;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class NumberRangeTest {

  @Test
  void from() {
    assertEquals(new NumberRange(1, Double.MAX_VALUE), NumberRange.from(1));
  }

  @Test
  void to() {
    assertEquals(new NumberRange(-Double.MAX_VALUE, 1), NumberRange.to(1));
  }

  @Test
  void range() {
    assertEquals(new NumberRange(1, 3), NumberRange.range(1, 3));
  }

  @Test
  void throwOrder() {
    assertThrows(IllegalArgumentException.class, () -> NumberRange.range(3, 1));
  }

  @Test
  void contains() {
    assertTrue(NumberRange.range(1, 3).contains(2));
  }

  @Test
  @SneakyThrows
  void fromString() {
    String test = "5..10";
    NumberRange parsed = NumberRange.parse(test);

    assertEquals(new NumberRange(5, 10), parsed);
  }
}