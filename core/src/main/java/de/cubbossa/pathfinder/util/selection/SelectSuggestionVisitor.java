package de.cubbossa.pathfinder.util.selection;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import de.cubbossa.pathfinder.antlr.SelectionSuggestionLanguageBaseVisitor;
import de.cubbossa.pathfinder.antlr.SelectionSuggestionLanguageParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class SelectSuggestionVisitor
    extends SelectionSuggestionLanguageBaseVisitor<List<Suggestion>> {

  private final Collection<String> identifiers;
  private final Map<String, Function<SelectionParser.SuggestionContext, List<Suggestion>>>
      arguments;
  private final String inputString;
  private final Player player;

  public SelectSuggestionVisitor(
      Collection<String> identifiers,
      Map<String, Function<SelectionParser.SuggestionContext, List<Suggestion>>> arguments,
      String inputString,
      Player player
  ) {
    this.identifiers = identifiers;
    this.arguments = arguments;
    this.inputString = inputString;
    this.player = player;
  }

  @Override
  public List<Suggestion> visitProgram(SelectionSuggestionLanguageParser.ProgramContext ctx) {
    return visitExpression(ctx.expression());
  }

  @Override
  public List<Suggestion> visitExpression(SelectionSuggestionLanguageParser.ExpressionContext ctx) {
    if (ctx.AT() == null) {
      return identifiers.stream()
          .map(i -> new Suggestion(StringRange.at(0), "@" + i))
          .collect(Collectors.toList());
    }
    if (ctx.IDENTIFIER() == null) {
      return identifiers.stream()
          .map(i -> new Suggestion(StringRange.between(0, 1), "@" + i))
          .collect(Collectors.toList());
    }
    if (ctx.conditions() == null) {
      return List.of(new Suggestion(StringRange.at(1 + ctx.IDENTIFIER().getText().length()), "["));
    }
    return visitConditions(ctx.conditions());
  }

  @Override
  public List<Suggestion> visitConditions(SelectionSuggestionLanguageParser.ConditionsContext ctx) {
    if (ctx.COND_CLOSE() != null) {
      return new ArrayList<>();
    }
    if (ctx.attributelist() != null) {
      return visitAttributelist(ctx.attributelist());
    }
    return suggestArguments("", inputString.length(), 0);
  }

  @Override
  public List<Suggestion> visitAttributelist(
      SelectionSuggestionLanguageParser.AttributelistContext ctx) {
    if (ctx.attribute() != null) {
      return visitAttribute(ctx.attribute());
    }
    if (ctx.COND_DELIMIT() != null) {
      return suggestArguments("", inputString.length(), 0);
    }
    return new ArrayList<>();
  }

  @Override
  public List<Suggestion> visitAttribute(SelectionSuggestionLanguageParser.AttributeContext ctx) {
    if (ctx.value() != null) {
      return suggestValues(ctx.IDENTIFIER().getText(), ctx.value().getText(),
          inputString.length() - ctx.value().getText().length());
    }
    if (ctx.COND_EQUALS() != null) {
      return suggestValues(ctx.IDENTIFIER().getText(), "", inputString.length());
    }
    return suggestArguments(ctx.IDENTIFIER().getText(),
        inputString.length() - ctx.IDENTIFIER().getText().length(),
        ctx.IDENTIFIER().getText().length());
  }

  private List<Suggestion> suggestValues(String key, String in, int offset) {
    var argument = arguments.get(key);
    if (argument == null) {
      return new ArrayList<>();
    }
    List<Suggestion> suggestions = argument.apply(new SelectionParser.SuggestionContext(
        player,
        in
    ));
    List<Suggestion> transformed = new ArrayList<>();
    suggestions.forEach(suggestion -> {
      if (in.length() > 0 && !suggestion.getText().toLowerCase().startsWith(in.toLowerCase())) {
        return;
      }
      StringRange range = suggestion.getRange();
      Suggestion s = new Suggestion(StringRange.between(
          range.getStart() + offset, range.getEnd() + offset
      ), suggestion.getText(), suggestion.getTooltip());
      transformed.add(s);
    });
    return transformed;
  }

  private List<Suggestion> suggestArguments(String in, int offset, int length) {
    return arguments.keySet().stream()
        .filter(s -> s.toLowerCase().startsWith(in.toLowerCase()))
        .map(s -> new Suggestion(StringRange.between(offset, offset + length), s))
        .collect(Collectors.toList());
  }
}
