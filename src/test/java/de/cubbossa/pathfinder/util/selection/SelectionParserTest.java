package de.cubbossa.pathfinder.util.selection;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SelectionParserTest {

  public static class TestParser
      extends SelectionParser<String, SelectionParser.ArgumentContext<?, String>> {

    public static class Argument<V>
        extends SelectionParser.Argument<V, String, ArgumentContext<V, String>, Argument<V>> {

      public Argument(ArgumentType<V> type) {
        super(type);
      }
    }

    public TestParser(String identifier, String... alias) {
      super(identifier, alias);
    }
  }

  public static final TestParser.Argument<NumberRange> LENGTH = new TestParser.Argument<>(
      reader -> {
        try {
          return NumberRange.parse(reader.getRemaining() == null
              ? ""
              : reader.getRemaining());
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }
      })
      .execute(context -> context.getScope().stream()
          .filter(s -> (s.length() >= context.getValue().getStart().doubleValue())
              && (s.length() <= context.getValue().getEnd().doubleValue()))
          .collect(Collectors.toList()));

  private enum CharacterType {
    LETTER, NUMBER, MISC
  }

  public static final TestParser.Argument<CharacterType> TYPE =
      new TestParser.Argument<>(reader -> switch (reader.getRemaining()) {
        case "letter" -> CharacterType.LETTER;
        case "number" -> CharacterType.NUMBER;
        default -> CharacterType.MISC;
      })
          .execute(context -> switch (context.getValue()) {
            case LETTER -> context.getScope().stream()
                .filter(o -> o.matches("[a-zA-Z]+"))
                .collect(Collectors.toList());
            case NUMBER -> context.getScope().stream()
                .filter(s -> s.matches("[0-9]+"))
                .collect(Collectors.toList());
            case MISC -> context.getScope();
          })
          .suggest(List.of("letter", "number"));

  private static final List<String> SCOPE = Lists.newArrayList(
      "A", "B", "C", "D", "E",
      "Word", "OtherWord", "XYZ",
      "123", "00000000",
      "            ", " ", "",
      "More words than one", "Another sentence"
  );

  private static TestParser parser;

  @BeforeAll
  static void setup() {
    parser = new TestParser("s");
    parser.addResolver("length", LENGTH);
    parser.addResolver("type", TYPE);
  }

  @Test
  @SneakyThrows
  public void testParseSelection1() {

    Assertions.assertEquals(
        Lists.newArrayList("A", "B", "C", "D", "E", " ", ""),
        parser.parse("@s[length=..1]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection2() {

    Assertions.assertEquals(
        Lists.newArrayList("OtherWord", "00000000", "            ", "More words than one",
            "Another sentence"),
        parser.parse("@s[length=5..]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection3() {

    Assertions.assertEquals(
        Lists.newArrayList("OtherWord"),
        parser.parse("@s[length=5..,type=letter]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection4() {

    Assertions.assertEquals(
        Lists.newArrayList("123"),
        parser.parse("@s[length=..5,type=number]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  public void testInvalidClassifier() {
    Assertions.assertThrows(IllegalStateException.class,
        () -> parser.parse("@invalid", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testOnlyClassifier() {
    Assertions.assertEquals(SCOPE, parser.parse("@s", SCOPE, SelectionParser.ArgumentContext::new));
  }
}