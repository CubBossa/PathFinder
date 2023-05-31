package de.cubbossa.pathapi.misc;

import lombok.Getter;

@Getter
public final class Pagination extends Range {

  private final int page;
  private final int size;

  public static Pagination page(int page, int size) {
    return new Pagination(page, size);
  }

  Pagination(int page, int size) {
    super(page * size, size);
    this.page = page;
    this.size = size;
  }

  public int getPageCount(int elements) {
    return (int) Math.ceil(elements / (float) size);
  }
}
