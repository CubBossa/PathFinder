package de.cubbossa.pathfinder.util;

import lombok.experimental.UtilityClass;

import java.util.List;

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
     * @param list   The list to modify
     * @param offset The amount of elements to drop.
     * @param limit  The limit of elements that the return list can have.
     * @param <T>    The element type of the list class.
     * @return A new list instance that is similar to the old list except for the first n elements
     * that were dropped
     */
    public <T> List<T> subList(List<T> list, int offset, int limit) {
        return list.subList(Integer.min(offset, list.size() == 0 ? 0 : list.size()),
                Integer.min(limit + offset, list.size()));
    }

    /**
     * Paginates a list by cutting it in slices of same size.
     *
     * @param list     The list with elements to paginate.
     * @param page     The page index, starting by 0.
     * @param pageSize The element count that makes up one page.
     * @param <T>      The element type of the list class.
     * @return A new list instance that is similar to the old list but only contains the given page.
     */
    public <T> List<T> subListPaginated(List<T> list, int page, int pageSize) {
        return subList(list, page * pageSize, pageSize);
    }
}
