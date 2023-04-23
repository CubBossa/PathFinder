package de.cubbossa.pathapi.misc;

import java.util.Objects;
import lombok.Getter;

@Getter
public final class NamespacedKey {

  private static final char SEPARATOR = ':';
  private final String namespace;
  private final String key;
  public NamespacedKey(String namespace, String key) {
    this.namespace = namespace;
    this.key = key;
  }

  public static NamespacedKey fromString(String value) {
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
