package de.cubbossa.pathfinder.api.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Pagination {

  private final int offset;
  private final int limit;

  public int getStart() {
    return offset;
  }

  public int getEndExclusive() {
    return offset + limit;
  }

  @Override
  public String toString() {
    return "Pagination{" +
        "offset=" + offset +
        ", limit=" + limit +
        '}';
  }

  public static Pagination pagination(int offset, int limit) {
    return new Pagination(offset, limit);
  }

  public static Pagination page(int page, int size) {
    return new Pagination(page * size, size);
  }
}
