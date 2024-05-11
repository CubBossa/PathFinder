package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.misc.Range;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CollectionUtils {

  /**
   * Offsets a list by a certain count of elements by dropping the first elements of the list.
   *
   * @param list   The list to modify
   * @param offset The amount of elements to drop.
   * @param <T>    The element type of the list class.
   * @return A new list instance that is similar to the old list except for the first n elements
   * that were dropped
   */
  public <T> List<T> subList(List<T> list, int offset) {
    return list.subList(Integer.min(offset, list.size()), list.size());
  }

  /**
   * Offsets a list by a certain count of elements by dropping the first elements of the list
   * and truncating the result to the given limit.
   *
   * @param list  The list to modify
   * @param range The amount of elements to drop and the limit of elements to return.
   * @param <T>   The element type of the list class.
   * @return A new list instance that is similar to the old list except for the first n elements
   * that were dropped
   */
  public <T> List<T> subList(List<T> list, Range range) {
    return list.subList(
        Integer.min(range.getOffset(), list.size() == 0 ? 0 : list.size()),
        Integer.min(range.getEndExclusive(), list.size())
    );
  }

  public static <E> List<E> everyNth(List<E> in, int n, int offset) {
    List<E> result = new ArrayList<>();
    for (int i = offset % n; i < in.size(); i += n) {
      result.add(in.get(i));
    }
    return result;
  }

  public static <K, V> Map<K, V> sort(Map<K, V> unsorted, Collection<K> sorting) {
    Map<K, V> sorted = new LinkedHashMap<>();
    for (K k : sorting) {
      sorted.put(k, unsorted.get(k));
    }
    return sorted;
  }
}
