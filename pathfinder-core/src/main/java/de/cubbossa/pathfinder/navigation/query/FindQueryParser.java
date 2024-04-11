package de.cubbossa.pathfinder.navigation.query;

import de.cubbossa.pathfinder.antlr.QueryLanguageLexer;
import de.cubbossa.pathfinder.antlr.QueryLanguageParser;
import de.cubbossa.pathfinder.visualizer.query.SearchQueryAttribute;
import de.cubbossa.pathfinder.visualizer.query.SearchTerm;
import de.cubbossa.pathfinder.visualizer.query.SearchTermHolder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class FindQueryParser {

  public <T> Collection<T> parse(String input, List<T> scope, Function<T, Collection<SearchTerm>> seachtermSupplier)
      throws ParseCancellationException {
    return parse(input, scope.stream().map(t -> new Wrapper<>(t, seachtermSupplier)).toList())
        .stream()
        .map(w -> w.element)
        .collect(Collectors.toList());
  }

  public <T extends SearchTermHolder> Collection<T> parse(String input, List<T> scope) {

    CharStream charStream = CharStreams.fromString(input);
    QueryLanguageLexer lexer = new QueryLanguageLexer(charStream);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    QueryLanguageParser parser = new QueryLanguageParser(tokens);

    ErrorListener listener = new ErrorListener();

    lexer.removeErrorListeners();
    lexer.addErrorListener(listener);
    parser.removeErrorListeners();
    parser.addErrorListener(listener);

    QueryLanguageParser.ProgramContext tree = parser.program();
    return new QueryLanguageVisitor<>(scope).visit(tree);
  }

  public static class ErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                            int charPositionInLine, String msg, RecognitionException e) {
      throw new ParseCancellationException(msg, e);
    }
  }

  private record Wrapper<T>(T element, Function<T, Collection<SearchTerm>> fun)
      implements SearchTermHolder {

    @Override
    public Collection<SearchTerm> getSearchTerms() {
      return fun.apply(element);
    }

    @Override
    public void addSearchTerms(Collection<SearchTerm> searchTerms) {

    }

    @Override
    public void removeSearchTerms(Collection<SearchTerm> searchTerms) {

    }

    @Override
    public void clearSearchTerms() {

    }

    @Override
    public boolean matches(SearchTerm searchTerm) {
      return matches(searchTerm, Collections.emptySet());
    }

    @Override
    public boolean matches(SearchTerm searchTerm, Collection<SearchQueryAttribute> attributes) {
      return getSearchTerms().stream()
          .filter(searchTerm::equals)
          .anyMatch(t -> t.matches(attributes));
    }

    @Override
    public boolean matches(String term) {
      return matches(term, Collections.emptySet());
    }

    @Override
    public boolean matches(String term, Collection<SearchQueryAttribute> attributes) {
      return getSearchTerms().stream()
          .filter(searchTerm -> searchTerm instanceof SearchTermImpl s && s.getIdentifier().equalsIgnoreCase(term))
          .anyMatch(searchTerm -> searchTerm.matches(attributes));
    }
  }
}
