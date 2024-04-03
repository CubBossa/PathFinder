package de.cubbossa.pathfinder.util;

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
import de.cubbossa.pathfinder.node.selection.SelectSuggestionVisitor;
import de.cubbossa.pathfinder.node.selection.SelectionAttribute;
import de.cubbossa.pathfinder.node.selection.SelectionVisitor;
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

public class SelectionParser<TypeT, ContextT extends SelectionParser.ArgumentContext<?, TypeT>> {

  private final Collection<String> identifiers = new ArrayList<>();
  private final Map<String, Argument<?, TypeT, ContextT, ?>> argumentMap = new HashMap<>();

  public SelectionParser(String identifier, String... alias) {
    identifiers.add(identifier);
    identifiers.addAll(Arrays.stream(alias).toList());
  }

  public void addResolver(Argument<?, ? extends TypeT, ? extends ContextT, ?> argument) {
    argumentMap.put(argument.getKey(), (Argument<?, TypeT, ContextT, ?>) argument);
  }

  public <S> Collection<TypeT> parse(String input, List<TypeT> scope, BiFunction<S, List<TypeT>, ContextT> context)
      throws ParseCancellationException {

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

    List<TypeT> scopeHolder = new ArrayList<>(scope);
    // not the speediest but fine for me for now
    for (SelectionModification modification : SelectionModification.values()) {
      for (SelectionAttribute a : attributes) {
        if (argumentMap.containsKey(a.identifier())) {
          Argument<S, TypeT, ContextT, ?> argument = (Argument<S, TypeT, ContextT, ?>) argumentMap.get(a.identifier());

          if (argument.modificationType() != modification) {
            continue;
          }

          S value = argument.getParse().apply(a.value());
          scopeHolder = argument.getExecute().apply(
              context.apply(value, scopeHolder)
          );
        }
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

  @Getter
  @RequiredArgsConstructor
  public static class ArgumentContext<ValueT, TypeT> {
    private final ValueT value;
    private final List<TypeT> scope;
  }

  @Getter
  @RequiredArgsConstructor
  public static class SuggestionContext {
    private final Player player;
    private final String input;
  }

  @Getter
  public static abstract class Argument<ValueT, TypeT, ContextT extends SelectionParser.ArgumentContext<?, TypeT>, ArgumentT extends Argument<ValueT, TypeT, ContextT, ArgumentT>> {

    private final Function<String, ValueT> parse;
    private Function<ContextT, List<TypeT>> execute;
    private Function<SuggestionContext, List<Suggestion>> suggest;

    public Argument(ArgumentType<ValueT> type) {
      this.parse = s -> {
        try {
          return type.parse(new StringReader(s));
        } catch (CommandSyntaxException e) {
          throw new RuntimeException(e);
        }
      };
    }

    public abstract String getKey();

    public abstract SelectionModification modificationType();

    public ArgumentT execute(Function<ContextT, List<TypeT>> execute) {
      this.execute = execute;
      return (ArgumentT) this;
    }

    public ArgumentT suggest(List<Suggestion> suggest) {
      this.suggest = context -> suggest;
      return (ArgumentT) this;
    }

    public ArgumentT suggestStrings(List<String> suggest) {
      this.suggest = context -> suggest.stream()
          .map(s -> new Suggestion(StringRange.between(0, context.input.length()), s))
          .collect(Collectors.toList());
      return (ArgumentT) this;
    }

    public ArgumentT suggest(Function<SuggestionContext, List<Suggestion>> suggest) {
      this.suggest = suggest;
      return (ArgumentT) this;
    }

    public ArgumentT suggestStrings(Function<SuggestionContext, List<String>> suggest) {
      this.suggest = context -> suggest.apply(context).stream()
          .map(s -> new Suggestion(StringRange.between(0, context.input.length()), s))
          .collect(Collectors.toList());
      return (ArgumentT) this;
    }
  }

  public enum SelectionModification {
    SORT,
    FILTER,
    PEEK
  }

  public static class ErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                            int charPositionInLine, String msg, RecognitionException e) {
      throw new ParseCancellationException(msg, e);
    }
  }
}
