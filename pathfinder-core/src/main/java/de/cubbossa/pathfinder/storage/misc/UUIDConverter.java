package de.cubbossa.pathfinder.storage.misc;

import org.jooq.Converter;

import java.util.UUID;

public class UUIDConverter implements Converter<String, UUID> {

  @Override
  public final UUID from(String t) {
    return UUID.fromString(t);
  }

  @Override
  public final String to(UUID u) {
    return u.toString();
  }

  @Override
  public Class<String> fromType() {
    return String.class;
  }

  @Override
  public Class<UUID> toType() {
    return UUID.class;
  }
}
