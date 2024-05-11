package de.cubbossa.pathfinder.misc;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public final class NamespacedKey {

  private static final Pattern PATTERN = Pattern.compile("[a-z0-9_-]+:[a-z0-9_-]+");
  private static final Predicate<String> PATTERN_TEST = PATTERN.asMatchPredicate();
  private static final char SEPARATOR = ':';
  private final String namespace;
  private final String key;

  public NamespacedKey(String namespace, String key) {
    this.namespace = namespace;
    this.key = key;
  }

  public static NamespacedKey fromString(String value) {
    if (!PATTERN_TEST.test(value)) {
      throw new IllegalArgumentException("NamespacedKey must match pattern '" + PATTERN.pattern() + "'. Input: '" + value + "'.");
    }
    String[] splits = value.split(":");
    return new NamespacedKey(splits[0], splits[1]);
  }

  @Override
  public String toString() {
    return namespace + SEPARATOR + key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NamespacedKey that = (NamespacedKey) o;
    return Objects.equals(namespace, that.namespace) && Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
