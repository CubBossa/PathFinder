package de.cubbossa.pathfinder.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandUtilsTest {

  @Test
  void wrapWithQuotation() {
    Suggestion quotation = new Suggestion(StringRange.at(0), "\"\"");
    Suggestion suggestion = new Suggestion(StringRange.at(0), "a");
    Suggestion suggestionOffset = new Suggestion(StringRange.at(1), "a");
    Suggestions suggestions = Suggestions.create("abc", List.of(suggestion));

    assertEquals(
        Suggestions.create("abc", List.of(suggestionOffset)),
        CommandUtils.wrapWithQuotation("abc", suggestions, false)
    );
    assertEquals(
        Suggestions.create("abc", List.of(suggestionOffset, quotation)),
        CommandUtils.wrapWithQuotation("abc", suggestions, true)
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
    assertEquals(List.of(3), CommandUtils.subList(List.of(1, 2, 3), 2));
    assertEquals(new ArrayList<>(), CommandUtils.subList(List.of(1, 2, 3), 3));
    assertEquals(new ArrayList<>(), CommandUtils.subList(List.of(1, 2, 3), 10));
  }

  @Test
  void testSubList() {
    assertEquals(List.of(3), CommandUtils.subList(List.of(1, 2, 3), 2, 1));
    assertEquals(new ArrayList<>(), CommandUtils.subList(List.of(1, 2, 3), 3, 1));
    assertEquals(new ArrayList<>(), CommandUtils.subList(List.of(1, 2, 3), 10, 1));
    assertEquals(List.of(2), CommandUtils.subList(List.of(1, 2, 3), 1, 1));
    assertEquals(new ArrayList<>(), CommandUtils.subList(List.of(1, 2, 3), 0, 0));
    assertEquals(List.of(1, 2, 3), CommandUtils.subList(List.of(1, 2, 3), 0, 10));
  }

  @Test
  void subListPaginated() {
    assertEquals(List.of(1, 2), CommandUtils.subListPaginated(List.of(1, 2, 3), 0, 2));
    assertEquals(List.of(3), CommandUtils.subListPaginated(List.of(1, 2, 3), 1, 2));
    assertEquals(new ArrayList<>(), CommandUtils.subListPaginated(List.of(1, 2, 3), 2, 2));
  }
}