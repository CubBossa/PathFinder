package de.cubbossa.pathfinder.util.selection;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathfinder.antlr.SelectionLanguageLexer;
import de.cubbossa.pathfinder.antlr.SelectionLanguageParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class SelectionParser<T, C extends SelectionParser.ArgumentContext<?, T>> {

  @Getter
  @RequiredArgsConstructor
  public static class ArgumentContext<S, T> {
    private final S value;
    private final List<T> scope;
  }

  @Getter
  public static class Argument<S, T, C extends SelectionParser.ArgumentContext<?, T>, A extends Argument<S, T, C, A>> {

    private final Function<String, S> parse;
    private Function<C, List<T>> execute;
    private Function<List<T>, List<String>> suggest;

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

    public A suggest(List<String> suggest) {
      this.suggest = ts -> suggest;
      return (A) this;
    }

    public A suggest(Function<List<T>, List<String>> suggest) {
      this.suggest = suggest;
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
        scopeHolder = argument.getExecute().apply(
            context.apply(
                argument.getParse().apply(a.value()),
                scope
            )
        );
      }
    }
    return scopeHolder;
  }

  public static class ErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                            int charPositionInLine, String msg, RecognitionException e) {
      throw new ParseCancellationException(msg, e);
    }
  }
}
