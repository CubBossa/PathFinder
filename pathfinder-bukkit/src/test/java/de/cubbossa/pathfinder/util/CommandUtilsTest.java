package de.cubbossa.pathfinder.util;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.misc.Range;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandUtilsTest {

  @Test
  void wrapWithQuotation() {
    Suggestion suggestion = new Suggestion(StringRange.at(0), "a");
    Suggestions suggestions = Suggestions.create("abc", List.of(suggestion));

    Suggestion quotation = new Suggestion(StringRange.at(0), "\"\"");
    assertEquals(
        Suggestions.create("abc", List.of(quotation)),
        CommandUtils.wrapWithQuotation("abc", suggestions, "", 0)
    );
    Suggestion quotationStart = new Suggestion(StringRange.between(0, 1), "\"");
    assertEquals(
        Suggestions.create("abc", List.of(quotationStart)),
        CommandUtils.wrapWithQuotation("abc", suggestions, "a", 0)
    );
    Suggestion suggestionOffset = new Suggestion(StringRange.at(1), "a");
    assertEquals(
        Suggestions.create("abc", List.of(suggestionOffset)),
        CommandUtils.wrapWithQuotation("abc", suggestions, "\"", 0)
    );
  }

  @Test
  void offsetSuggestions() {
    Suggestion suggestion = new Suggestion(StringRange.at(0), "a");
    Suggestion suggestionOffset = new Suggestion(StringRange.at(1), "a");
    Suggestions suggestions = Suggestions.create("abc", List.of(suggestion));

    assertEquals(
        Suggestions.create("abc", List.of(suggestionOffset)),
        CommandUtils.offsetSuggestions("abc", suggestions, 1)
    );
  }

  @Test
  void printList() {
  }

  @Test
  void subList() {
    assertEquals(List.of(3), CollectionUtils.subList(List.of(1, 2, 3), 2));
    assertEquals(new ArrayList<>(), CollectionUtils.subList(List.of(1, 2, 3), 3));
    assertEquals(new ArrayList<>(), CollectionUtils.subList(List.of(1, 2, 3), 10));
  }

  @Test
  void testSubList() {
    assertEquals(List.of(3), CollectionUtils.subList(List.of(1, 2, 3), Range.range(2, 1)));
    assertEquals(new ArrayList<>(), CollectionUtils.subList(List.of(1, 2, 3), Range.range(3, 1)));
    assertEquals(new ArrayList<>(), CollectionUtils.subList(List.of(1, 2, 3), Range.range(10, 1)));
    assertEquals(List.of(2), CollectionUtils.subList(List.of(1, 2, 3), Range.range(1, 1)));
    assertEquals(new ArrayList<>(), CollectionUtils.subList(List.of(1, 2, 3), Range.range(0, 0)));
    assertEquals(List.of(1, 2, 3), CollectionUtils.subList(List.of(1, 2, 3), Range.range(0, 10)));
  }

  @Test
  void subListPaginated() {
    assertEquals(List.of(1, 2), CollectionUtils.subList(List.of(1, 2, 3), Pagination.page(0, 2)));
    assertEquals(List.of(3), CollectionUtils.subList(List.of(1, 2, 3), Pagination.page(1, 2)));
    assertEquals(new ArrayList<>(), CollectionUtils.subList(List.of(1, 2, 3), Pagination.page(2, 2)));
  }
}