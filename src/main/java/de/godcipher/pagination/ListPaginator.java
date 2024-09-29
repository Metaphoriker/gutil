package de.godcipher.pagination;

import java.util.Collections;
import java.util.List;

/** An immutable paginator for handling paginated views of a list of items. */
public class ListPaginator<T> {

  /** List of items to paginate through. */
  private final List<T> items;

  /** The maximum number of items per page. */
  private final int pageSize;

  /**
   * Constructs a ListPaginator with a specified page size and an existing list of items.
   *
   * @param items the list of items to paginate
   * @param pageSize the number of items per page; must be greater than zero
   * @throws IllegalArgumentException if pageSize is less than or equal to zero
   */
  public ListPaginator(List<T> items, int pageSize) {
    validateIntGreaterThanZero(pageSize);
    this.items = items;
    this.pageSize = pageSize;
  }

  /**
   * Retrieves a sublist of items for the specified page number.
   *
   * @param pageNumber the page number (zero-based index); must be non-negative
   * @return an unmodifiable list of items for the specified page, or an empty list if the page is
   *     out of range
   * @throws IllegalArgumentException if pageNumber is negative
   */
  public List<T> getPage(int pageNumber) {
    validateIntNonNegative(pageNumber);
    int fromIndex = pageNumber * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, items.size());
    return getItemsInRange(fromIndex, toIndex);
  }

  /**
   * Returns a sublist of items for the specified index range, wrapped in an unmodifiable list.
   *
   * @param fromIndex the starting index (inclusive)
   * @param toIndex the ending index (exclusive)
   * @return an unmodifiable list of items within the range, or an empty list if fromIndex is out of
   *     range
   */
  private List<T> getItemsInRange(int fromIndex, int toIndex) {
    if (fromIndex >= items.size()) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(items.subList(fromIndex, toIndex));
  }

  /**
   * Validates that the provided integer is greater than zero.
   *
   * @param integer the integer value to validate (e.g., page size)
   * @throws IllegalArgumentException if the integer is less than or equal to zero
   */
  private void validateIntGreaterThanZero(int integer) {
    if (integer <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero.");
    }
  }

  /**
   * Validates that the provided integer is non-negative.
   *
   * @param integer the integer value to validate (e.g., page number)
   * @throws IllegalArgumentException if the integer is negative
   */
  private void validateIntNonNegative(int integer) {
    if (integer < 0) {
      throw new IllegalArgumentException("Page number must be non-negative.");
    }
  }

  /**
   * Returns the total number of pages based on the current list size and page size.
   *
   * @return the total number of pages
   */
  public int getTotalPages() {
    return (int) Math.ceil((double) items.size() / pageSize);
  }
}
