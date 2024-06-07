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
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.selection.ParsedSelectionAttribute;
import de.cubbossa.pathfinder.node.selection.SelectSuggestionVisitor;
import de.cubbossa.pathfinder.node.selection.SelectionVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
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
import org.jetbrains.annotations.NotNull;

public abstract class SelectionParser<TypeT, ContextT extends SelectionParser.ArgumentContext<?, TypeT>> {

  private final Collection<String> identifiers = new ArrayList<>();
  private final Collection<Argument<?, TypeT, ContextT, ?>> arguments = new TreeSet<>();

  public SelectionParser(String identifier, String... alias) {
    identifiers.add(identifier);
    identifiers.addAll(Arrays.stream(alias).toList());
  }

  public void addResolver(Argument<?, ? extends TypeT, ? extends ContextT, ?> argument) {
    arguments.add((Argument<?, TypeT, ContextT, ?>) argument);
  }

  public abstract <ValueT> ContextT createContext(ValueT value, List<TypeT> scope, Object sender);

  public List<TypeT> parse(String input, List<TypeT> scope) {
    return this.parse(input, scope, null);
  }

  public <ValueT> List<TypeT> parse(String input, List<TypeT> scope, Object sender)
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
    List<ParsedSelectionAttribute> attributes = new SelectionVisitor(identifiers).visit(tree);
    if (attributes == null) {
      attributes = new ArrayList<>();
    }

    List<TypeT> scopeHolder = new ArrayList<>(scope);
    for (Argument<?, TypeT, ContextT, ?> argument : arguments) {
      for (ParsedSelectionAttribute a : attributes) {
        if (!a.identifier.equals(argument.getKey())) {
          continue;
        }
        ValueT value = (ValueT) argument.getParse().apply(a.value);
        scopeHolder = argument.getExecute().apply(
            createContext(value, scopeHolder, sender)
        );
      }
    }
    return scopeHolder;
  }

  public CompletableFuture<Suggestions> applySuggestions(
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
    arguments.forEach((a) -> map.put(a.getKey(), a.getSuggest()));

    List<Suggestion> suggestions =
        new SelectSuggestionVisitor(identifiers, map, input, null).visit(tree);

    return CompletableFuture.completedFuture(Suggestions.create(command, suggestions));
  }

  @Getter
  @RequiredArgsConstructor
  public static abstract class ArgumentContext<ValueT, TypeT> {
    private final ValueT value;
    private final List<TypeT> scope;

    public abstract Object getSender();

    public abstract Location getSenderLocation();
  }

  @Getter
  @RequiredArgsConstructor
  public static class SuggestionContext {
    private final Object sender;
    private final String input;
  }

  @Getter
  public static abstract class Argument<ValueT, TypeT, ContextT extends SelectionParser.ArgumentContext<?, TypeT>, ArgumentT extends Argument<ValueT, TypeT, ContextT, ArgumentT>>
      implements Comparable<ArgumentT> {

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

    public Collection<String> executeAfter() {
      return Collections.emptyList();
    }

    @Override
    public int compareTo(@NotNull ArgumentT o) {
      int compare = Integer.compare(modificationType().ordinal(), o.modificationType().ordinal());
      if (compare != 0) {
        return compare;
      }
      if (o.executeAfter().contains(getKey())) {
        return 1;
      }
      if (executeAfter().contains(o.getKey())) {
        return -1;
      }
      return String.CASE_INSENSITIVE_ORDER.compare(getKey(), o.getKey());
    }

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
