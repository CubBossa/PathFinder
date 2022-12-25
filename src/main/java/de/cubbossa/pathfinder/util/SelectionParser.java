package de.cubbossa.pathfinder.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.CustomArgument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class SelectionParser<T, C extends SelectionParser.Context> {

  private static final Pattern SELECT_PATTERN =
      Pattern.compile("@[a-zA-Z0-9_]+(\\[((.+=.+,)*(.+=.+))?])?");
  private final List<String> classifiers;
  private final Collection<Filter<T, C>> filters;
  @Getter
  @Setter
  private Function<String, C> contextSupplier;

  public SelectionParser(Function<String, C> contextSupplier, String... classifier) {
    this.classifiers = Lists.newArrayList(classifier);
    this.contextSupplier = contextSupplier;
    this.filters = new ArrayList<>();
  }

  public SelectionParser(Collection<Filter<T, C>> filters, Function<String, C> contextSupplier,
                         String... classifier) {
    this.classifiers = Lists.newArrayList(classifier);
    this.contextSupplier = contextSupplier;
    this.filters = new ArrayList<>(filters);
  }

  public void addSelector(Filter<T, C> filter) {
    filters.add(filter);
  }

  public <S extends Collection<T>> S parseSelection(Collection<T> scope, String input,
                                                    Supplier<S> resultFactory)
      throws CustomArgument.CustomArgumentException {
    Matcher matcher = SELECT_PATTERN.matcher(input);
    if (!matcher.matches()) {
      throw new CustomArgument.CustomArgumentException(
          "Select String must be of format @<classifier>[<key>=<value>,...]");
    }
    String classifier = input.substring(1).split("\\[")[0];
    if (!classifiers.contains(classifier.toLowerCase())) {
      throw new CustomArgument.CustomArgumentException("Invalid classifier: " + classifier);
    }
    if (input.indexOf('[') == -1) {
      S res = resultFactory.get();
      res.addAll(scope);
      return res;
    }
    String argumentString = matcher.group(2);

    Map<Filter<T, C>, String> arguments = new HashMap<>();
    while (argumentString.length() > 0) {
      int len = argumentString.length();
      for (Filter<T, C> filter : filters) {
        if (!argumentString.startsWith(filter.key())) {
          continue;
        }
        argumentString = argumentString.substring(filter.key().length() + 1);
        Matcher m = filter.value().matcher(argumentString);
        if (!m.find() || m.start() != 0) {
          throw new CustomArgument.CustomArgumentException(
              "Illegal value for key '" + filter.key() + "': " + argumentString);
        }
        arguments.put(filter, argumentString.substring(0, m.end()));
        argumentString = argumentString.substring(m.end());
        if (argumentString.startsWith(",")) {
          argumentString = argumentString.substring(1);
        }
      }
      if (len <= argumentString.length()) {
        throw new CustomArgument.CustomArgumentException(
            "Illegal selection argument: " + argumentString);
      }
    }

    S result = resultFactory.get();

    if (result instanceof NodeSelection sel) {
      Map<String, String> argumentsStrings = new HashMap<>();
      arguments.forEach((tcFilter, s) -> argumentsStrings.put(tcFilter.key, s));
      sel.setMeta(new NodeSelection.Meta(input, argumentsStrings));
    }

    result.addAll(scope);
    for (Map.Entry<Filter<T, C>, String> entry : arguments.entrySet()) {
      Collection<T> x;
      try {
        x = entry.getKey().filter().apply(result, contextSupplier.apply(entry.getValue()));
      } catch (FilterException e) {
        throw new CustomArgument.CustomArgumentException(e.getMessage());
      }
      if (x.getClass().equals(result.getClass())) {
        result = (S) x;
      } else {
        result.clear();
        result.addAll(x);
      }
    }
    return result;
  }

  public CompletableFuture<Suggestions> applySuggestions(SuggestionInfo suggestionInfo,
                                                         SuggestionsBuilder suggestionsBuilder)
      throws CommandSyntaxException {
    if (suggestionInfo.currentInput().contains("]")) {
      return suggestionsBuilder.buildFuture();
    }
    int lastSeparator = Integer.max(
        suggestionInfo.currentInput().lastIndexOf(','),
        suggestionInfo.currentInput().lastIndexOf('['));
    int lastEquals = suggestionInfo.currentInput().lastIndexOf('=');

    if (lastSeparator == lastEquals) {
      return suggestionsBuilder
          .suggest("\"@n\"")
          .suggest("\"@n[]\"")
          .buildFuture();
    }

    if (lastSeparator > lastEquals) {
      String key = suggestionInfo.currentInput().substring(lastSeparator + 1).toLowerCase();
      SuggestionsBuilder b = suggestionsBuilder
          .createOffset(lastSeparator + 1);
      filters.stream()
          .filter(tcFilter -> tcFilter.key().toLowerCase().contains(key))
          .forEach(tcFilter -> b.suggest(tcFilter.key()));
      return b.buildFuture();
    } else {
      String key = suggestionInfo.currentInput().substring(lastSeparator + 1, lastEquals);
      String val = suggestionInfo.currentInput().substring(lastEquals + 1).toLowerCase();
      Filter<T, C> filter =
          filters.stream().filter(tcFilter -> tcFilter.key().equalsIgnoreCase(key)).findFirst()
              .orElse(null);
      if (filter == null) {
        return suggestionsBuilder.buildFuture();
      }
      SuggestionsBuilder b = suggestionsBuilder
          .createOffset(lastEquals + 1);
      try {
        filter.completions.apply(contextSupplier.apply(val)).stream()
            .distinct()
            .filter(s -> s.toLowerCase().contains(val))
            .forEach(b::suggest);
        return b.buildFuture();

      } catch (SuggestionException e) {
        throw new CommandSyntaxException(
            CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), e::getMessage, val,
            suggestionInfo.currentInput().length());
      }
    }
  }
  public interface FilterApplier<N, C> {
    Collection<N> apply(Collection<N> elements, C context) throws FilterException;
  }

  public interface SuggestionsSupplier<N, C> {
    Collection<String> apply(C context) throws SuggestionException;
  }

  @RequiredArgsConstructor
  public static class Context {
    private final String value;

    public String value() {
      return value;
    }
  }

  @RequiredArgsConstructor
  @Getter
  public static class FilterException extends Exception {
    private final String message;
  }

  @RequiredArgsConstructor
  @Getter
  public static class SuggestionException extends Exception {
    private final String message;
  }

  public record Filter<N, C extends SelectionParser.Context>(String key, Pattern value,
                                                             FilterApplier<N, C> filter,
                                                             SuggestionsSupplier<N, C> completions) {
  }
}
