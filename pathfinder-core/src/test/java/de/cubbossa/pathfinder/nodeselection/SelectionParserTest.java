package de.cubbossa.pathfinder.nodeselection;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathfinder.node.selection.NumberRange;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SelectionParserTest {

  public static final TestParser.Argument<UUID> UUID = new TestParser.Argument<>("uuid", r -> {
    UUID uuid = java.util.UUID.fromString(r.getRemaining());
    System.out.println(uuid);
    return uuid;
  })
      .execute(SelectionParser.ArgumentContext::getScope);
  public static final TestParser.Argument<Integer> LETTER_COUNT = new TestParser.Argument<>("lettercount",
      IntegerArgumentType.integer(1, 10)
  ).execute(c -> c.getScope().stream()
      .filter(s -> {
        Matcher matcher = Pattern.compile("[a-zA-Z]").matcher(s);
        int counter = 0;
        while (matcher.find()) {
          counter++;
        }
        return counter == c.getValue();
      })
      .collect(Collectors.toList()));
  public static final TestParser.Argument<NumberRange> LENGTH = new TestParser.Argument<>("length",
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
  public static final TestParser.Argument<CharacterType> TYPE =
      new TestParser.Argument<>("type", reader -> switch (reader.getRemaining()) {
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
          .suggestStrings(List.of("letter", "number"));
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
    parser.addResolver(UUID);
    parser.addResolver(LENGTH);
    parser.addResolver(TYPE);
    parser.addResolver(LETTER_COUNT);
  }

  @Test
  @SneakyThrows
  public void testParseSelection1a() {

    Assertions.assertEquals(
        Lists.newArrayList("A", "B", "C", "D", "E", " ", ""),
        parser.parse("@s[length=..1]", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testParseSelection1b() {

    Assertions.assertEquals(
        Lists.newArrayList("A", "B", "C", "D", "E", " "),
        parser.parse("@s[length=1]", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testParseSelection2() {

    Assertions.assertEquals(
        Lists.newArrayList("OtherWord", "00000000", "            ", "More words than one",
            "Another sentence"),
        parser.parse("@s[length=5..]", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testParseSelection3() {

    Assertions.assertEquals(
        Lists.newArrayList("OtherWord"),
        parser.parse("@s[length=5..,type=letter]", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testParseSelection4() {

    Assertions.assertEquals(
        Lists.newArrayList("123"),
        parser.parse("@s[length=..5,type=number]", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testParseSelection5() {

    Assertions.assertEquals(
        Lists.newArrayList("A", "B", "C", "D", "E"),
        parser.parse("@s[lettercount=1]", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testParseSelection6a() {

    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=abcdefg]", SCOPE));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=0123456]", SCOPE));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=a-a]", SCOPE));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=a1]", SCOPE));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=1a]", SCOPE));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=68cf83b1]", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testParseSelection6b() {

    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=68cf83b1-1f01-4c56-88bb-c2bec7105714]", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testParseSelection7() {
    TestParser testParser = new TestParser("s");
    testParser.addResolver(new TestParser.Argument<>("a", StringArgumentType.string()).execute(c -> {
      c.getScope().add("A");
      return c.getScope();
    }));
    testParser.addResolver(new TestParser.Argument<>("b", StringArgumentType.string()) {
      @Override
      public Collection<String> executeAfter() {
        return List.of("a");
      }
    }.execute(c -> {
      c.getScope().add("B");
      return c.getScope();
    }));

    Assertions.assertEquals(
        testParser.parse("@s[a=A,b=B]", new ArrayList<>()),
        testParser.parse("@s[b=B,a=A]", new ArrayList<>())
    );

  }

  @Test
  public void testInvalidClassifier() {
    Assertions.assertThrows(IllegalStateException.class,
        () -> parser.parse("@invalid", SCOPE));
  }

  @Test
  @SneakyThrows
  public void testOnlyClassifier() {
    Assertions.assertEquals(SCOPE, parser.parse("@s", SCOPE));
  }

  @Test
  void applySuggestions() throws ExecutionException, InterruptedException {

    System.out.println(parser.applySuggestions("123", "").get());
    System.out.println(parser.applySuggestions("", "@").get());
    System.out.println(parser.applySuggestions("", "@abc[").get());
    System.out.println(parser.applySuggestions("", "@n[le").get());
    System.out.println(parser.applySuggestions("", "@n[type=").get());
    System.out.println(parser.applySuggestions("", "@n[type=le").get());
    System.out.println(parser.applySuggestions("", "@n[type=le,").get());
    System.out.println(parser.applySuggestions("", "@s[]").get());
    System.out.println(parser.applySuggestions("", "@n").get());
  }

  private enum CharacterType {
    LETTER, NUMBER, MISC
  }

  public static class TestParser
      extends SelectionParser<String, SelectionParser.ArgumentContext<?, String>> {

    public TestParser(String identifier, String... alias) {
      super(identifier, alias);
    }

    @Override
    public <ValueT> ArgumentContext<?, String> createContext(ValueT value, List<String> scope, Object sender) {
      return new ArgumentContext<Object, String>(value, scope) {
        @Override
        public Object getSender() {
          return sender;
        }

        @Override
        public Location getSenderLocation() {
          return null;
        }
      };
    }

    public static class Argument<V>
        extends SelectionParser.Argument<V, String, ArgumentContext<V, String>, Argument<V>> {

      @Getter
      private final String key;

      public Argument(String key, ArgumentType<V> type) {
        super(type);
        this.key = key;
      }

      @Override
      public SelectionModification modificationType() {
        return SelectionModification.FILTER;
      }
    }
  }
}