package de.cubbossa.pathfinder.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringCompassTest {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  @Test
  void asComponent() {
    StringCompass compass = new StringCompass("<gray>--------</gray>", 3, () -> 5.);
    compass.addMarker("N", "N", () -> 0.);
    compass.addMarker("E", "E", () -> 90.);
    compass.addMarker("S", "S", () -> 180.);
    compass.addMarker("W", "W", () -> 270.);

    Assertions.assertEquals(
        "<gray>-W-N-E-",
        MINI_MESSAGE.serialize(compass.asComponent().compact()));
  }
}