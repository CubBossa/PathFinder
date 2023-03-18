package de.cubbossa.pathfinder.data;

import java.nio.ByteBuffer;
import java.util.UUID;
import org.jooq.Converter;

public class UUIDConverter implements Converter<byte[], UUID> {

  @Override
  public final UUID from(byte[] t) {
    if (t == null) {
      return null;
    }

    ByteBuffer bb = ByteBuffer.wrap(t);
    return new UUID(bb.getLong(), bb.getLong());
  }

  @Override
  public final byte[] to(UUID u) {
    if (u == null) {
      return null;
    }

    return ByteBuffer.wrap(new byte[16])
        .putLong(u.getMostSignificantBits())
        .putLong(u.getLeastSignificantBits())
        .array();
  }

  @Override
  public Class<byte[]> fromType() {
    return byte[].class;
  }

  @Override
  public Class<UUID> toType() {
    return UUID.class;
  }
}
