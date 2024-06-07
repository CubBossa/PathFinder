package de.cubbossa.pathfinder.node.selection;

import de.cubbossa.pathfinder.antlr.SelectionLanguageBaseVisitor;
import de.cubbossa.pathfinder.antlr.SelectionLanguageParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelectionVisitor extends SelectionLanguageBaseVisitor<List<ParsedSelectionAttribute>> {

  private final Collection<String> identifiers;

  public SelectionVisitor(Collection<String> identifiers) {
    this.identifiers = identifiers;
  }

  @Override
  public List<ParsedSelectionAttribute> visitProgram(SelectionLanguageParser.ProgramContext ctx) {
    return visitExpression(ctx.expression());
  }

  @Override
  public List<ParsedSelectionAttribute> visitExpression(SelectionLanguageParser.ExpressionContext ctx) {
    if (!identifiers.contains(ctx.selector().IDENTIFIER().getSymbol().getText())) {
      throw new IllegalStateException(
          "Invalid identifier: " + ctx.selector().IDENTIFIER().getSymbol().getText());
    }
    return ctx.conditions() == null ? null : visitConditions(ctx.conditions());
  }

  @Override
  public List<ParsedSelectionAttribute> visitConditions(SelectionLanguageParser.ConditionsContext ctx) {
    return ctx.attributelist() == null ? null : visitAttributelist(ctx.attributelist());
  }

  @Override
  public List<ParsedSelectionAttribute> visitAttributelist(
      SelectionLanguageParser.AttributelistContext ctx) {

    List<ParsedSelectionAttribute> list = new ArrayList<>();
    if (ctx.attributelist() != null) {
      list.addAll(visitAttributelist(ctx.attributelist()));
    }
    list.addAll(visitAttribute(ctx.attribute()));
    return list;
  }

  @Override
  public List<ParsedSelectionAttribute> visitAttribute(SelectionLanguageParser.AttributeContext ctx) {

    String identifier = ctx.IDENTIFIER().getText();
    return List.of(new ParsedSelectionAttribute(identifier, ctx.value().getText()));
  }
}
