package de.godcipher.gutil.pagination;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.godcipher.gutil.pagination.ListPaginator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListPaginatorTest {

  private ListPaginator<String> paginator;
  private List<String> items;

  @BeforeEach
  void setUp() {
    items = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
    paginator = new ListPaginator<>(items, 3);
  }

  @Test
  public void testGetPageReturnsImmutableList() {
    List<String> firstPage = paginator.getPage(0);

    assertEquals(Arrays.asList("A", "B", "C"), firstPage);

    try {
      firstPage.add("F");
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Exception is expected, test passes
    }

    try {
      firstPage.remove(0);
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Exception is expected, test passes
    }

    try {
      firstPage.set(0, "Z");
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Exception is expected, test passes
    }
  }

  @Test
  void testGetPage_FirstPage() {
    List<String> page = paginator.getPage(0);
    assertEquals(Arrays.asList("A", "B", "C"), page, "First page should return A, B, C");
  }

  @Test
  void testGetPage_SecondPage() {
    List<String> page = paginator.getPage(1);
    assertEquals(Arrays.asList("D", "E", "F"), page, "Second page should return D, E, F");
  }

  @Test
  void testGetPage_LastPage() {
    List<String> page = paginator.getPage(3); // 4th page
    assertEquals(Arrays.asList("J"), page, "Last page should return J only");
  }

  @Test
  void testGetPage_OutOfRange() {
    List<String> page = paginator.getPage(4);
    assertTrue(page.isEmpty(), "Out of range pages should return an empty list");
  }

  @Test
  void testGetPage_EmptyList() {
    ListPaginator<String> emptyPaginator = new ListPaginator<>(Collections.emptyList(), 3);
    List<String> page = emptyPaginator.getPage(0);
    assertTrue(page.isEmpty(), "Paginator for an empty list should return an empty list");
  }

  @Test
  void testGetTotalPages() {
    int totalPages = paginator.getTotalPages();
    assertEquals(4, totalPages, "Total pages should be 4");
  }

  @Test
  void testGetTotalPages_ExactMultiple() {
    ListPaginator<String> exactPaginator = new ListPaginator<>(items.subList(0, 9), 3);
    int totalPages = exactPaginator.getTotalPages();
    assertEquals(
        3, totalPages, "Total pages should be 3 when items are an exact multiple of page size");
  }

  @Test
  void testInvalidPageSize_ThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new ListPaginator<>(items, 0);
            });
    assertEquals("Page size must be greater than zero.", exception.getMessage());
  }

  @Test
  void testInvalidPageNumber_ThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              paginator.getPage(-1);
            });
    assertEquals("Page number must be non-negative.", exception.getMessage());
  }
}
