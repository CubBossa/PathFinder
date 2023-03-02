package de.cubbossa.pathfinder.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Triple<A, B, C> {

  private A left;
  private B middle;
  private C right;

  public static <A, B, C> Triple<A, B, C> of(A left, B middle, C right) {
    return new Triple<>(left, middle, right);
  }
}
