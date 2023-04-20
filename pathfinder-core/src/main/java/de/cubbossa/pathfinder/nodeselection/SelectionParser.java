package de.cubbossa.pathfinder.nodeselection;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathfinder.antlr.SelectionLanguageLexer;
import de.cubbossa.pathfinder.antlr.SelectionLanguageParser;
import de.cubbossa.pathfinder.antlr.SelectionSuggestionLanguageLexer;
import de.cubbossa.pathfinder.antlr.SelectionSuggestionLanguageParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.entity.Player;

public class SelectionParser<T, C extends SelectionParser.ArgumentContext<?, T>> {

  @Getter
  @RequiredArgsConstructor
  public static class ArgumentContext<S, T> {
    private final S value;
    private final List<T> scope;
  }

  @Getter
  @RequiredArgsConstructor
  public static class SuggestionContext {
    private final Player player;
    private final String input;
  }

  @Getter
  public static class Argument<S, T, C extends SelectionParser.ArgumentContext<?, T>, A extends Argument<S, T, C, A>> {

    private final Function<String, S> parse;
    private Function<C, List<T>> execute;
    private Function<SuggestionContext, List<Suggestion>> suggest;

    public Argument(ArgumentType<S> type) {
      this.parse = s -> {
        try {
          return type.parse(new StringReader(s));
        } catch (CommandSyntaxException e) {
          throw new RuntimeException(e);
        }
      };
    }

    public A execute(Function<C, List<T>> execute) {
      this.execute = execute;
      return (A) this;
    }

    public A suggest(List<Suggestion> suggest) {
      this.suggest = context -> suggest;
      return (A) this;
    }

    public A suggestStrings(List<String> suggest) {
      this.suggest = context -> suggest.stream()
          .map(s -> new Suggestion(StringRange.between(0, context.input.length()), s))
          .collect(Collectors.toList());
      return (A) this;
    }

    public A suggest(Function<SuggestionContext, List<Suggestion>> suggest) {
      this.suggest = suggest;
      return (A) this;
    }

    public A suggestStrings(Function<SuggestionContext, List<String>> suggest) {
      this.suggest = context -> suggest.apply(context).stream()
          .map(s -> new Suggestion(StringRange.between(0, context.input.length()), s))
          .collect(Collectors.toList());
      return (A) this;
    }
  }

  private final Collection<String> identifiers = new ArrayList<>();
  private final Map<String, Argument<?, T, C, ?>> argumentMap = new HashMap<>();

  public SelectionParser(String identifier, String... alias) {
    identifiers.add(identifier);
    identifiers.addAll(Arrays.stream(alias).toList());
  }

  public void addResolver(
      String identifier,
      Argument<?, ? extends T, ? extends C, ?> argument
  ) {
    argumentMap.put(identifier, (Argument<?, T, C, ?>) argument);
  }

  public <S> Collection<T> parse(String input, List<T> scope, BiFunction<S, List<T>, C> context)
      throws ParseCancellationException, CommandSyntaxException {

    CharStream charStream = CharStreams.fromString(input);
    SelectionLanguageLexer lexer = new SelectionLanguageLexer(charStream);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    SelectionLanguageParser parser = new SelectionLanguageParser(tokens);

    ErrorListener listener = new ErrorListener();

    lexer.removeErrorListeners();
    lexer.addErrorListener(listener);
    parser.removeErrorListeners();
    parser.addErrorListener(listener);

    SelectionLanguageParser.ProgramContext tree = parser.program();
    List<SelectionAttribute> attributes = new SelectionVisitor(identifiers).visit(tree);
    if (attributes == null) {
      attributes = new ArrayList<>();
    }

    List<T> scopeHolder = new ArrayList<>(scope);
    for (SelectionAttribute a : attributes) {
      if (argumentMap.containsKey(a.identifier())) {
        Argument<S, T, C, ?> argument = (Argument<S, T, C, ?>) argumentMap.get(a.identifier());
        S value = argument.getParse().apply(a.value());
        scopeHolder = argument.getExecute().apply(
            context.apply(value, scopeHolder)
        );
      }
    }
    return scopeHolder;
  }

  public CompletableFuture<Suggestions> applySuggestions(
      Player player,
      String command,
      String input
  ) {

    CharStream charStream = CharStreams.fromString(input);
    SelectionSuggestionLanguageLexer lexer = new SelectionSuggestionLanguageLexer(charStream);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    SelectionSuggestionLanguageParser parser = new SelectionSuggestionLanguageParser(tokens);

    parser.removeErrorListeners();

    SelectionSuggestionLanguageParser.ProgramContext tree = parser.program();
    Map<String, Function<SuggestionContext, List<Suggestion>>> map = new HashMap<>();
    argumentMap.forEach((s, tcArgument) -> map.put(s, tcArgument.suggest));

    List<Suggestion> suggestions =
        new SelectSuggestionVisitor(identifiers, map, input, null).visit(tree);

    return CompletableFuture.completedFuture(Suggestions.create(command, suggestions));
  }

  public static class ErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                            int charPositionInLine, String msg, RecognitionException e) {
      throw new ParseCancellationException(msg, e);
    }
  }
}
