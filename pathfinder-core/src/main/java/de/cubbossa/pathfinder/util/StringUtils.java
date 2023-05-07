package de.cubbossa.pathfinder.util;

import de.cubbossa.pathapi.misc.NamespacedKey;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.awt.*;
import java.util.Random;

public class StringUtils {

  @Setter
  private static MiniMessage miniMessage = MiniMessage.miniMessage();

  public static String toDisplayNameFormat(NamespacedKey key) {
    return insertInRandomHexString(capizalize(key.getKey().toLowerCase()));
  }

  public static String insertInRandomHexString(String inner) {
    String hex = Integer.toHexString(
            Color.getHSBColor(new Random().nextInt(360) / 360.f, 73 / 100.f, 96 / 100.f).getRGB())
        .substring(2);
    return "<#" + hex + ">" + inner + "</#" + hex + ">";
  }

  public static String capizalize(String in) {
    if (in.length() < 1) {
      throw new IllegalArgumentException("String must not be empty");
    }
    return in.substring(0, 1).toUpperCase() + in.substring(1);
  }

  public static Component deserialize(String str) {
    return miniMessage.deserialize(str);
  }

  public static Component deserialize(String str, TagResolver resolver) {
    return miniMessage.deserialize(str, resolver);
  }
}
