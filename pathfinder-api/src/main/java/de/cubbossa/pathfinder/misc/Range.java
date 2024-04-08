package de.cubbossa.pathfinder.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Range {

  private final int offset;
  private final int limit;

  public static Range range(int offset, int limit) {
    return new Range(offset, limit);
  }

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
}
