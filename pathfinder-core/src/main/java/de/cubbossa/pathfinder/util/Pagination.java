package de.cubbossa.pathfinder.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Pagination {

  private final int offset;
  private final int limit;

  public int getStart() {
    return offset;
  }

  public int getEndExclusive() {
    return offset + limit;
  }

  public static Pagination pagination(int offset, int limit) {
    return new Pagination(offset, limit);
  }

  public static Pagination page(int page, int size) {
    return new Pagination(page * size, size);
  }
}
