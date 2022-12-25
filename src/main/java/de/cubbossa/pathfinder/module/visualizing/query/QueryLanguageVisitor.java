package de.cubbossa.pathfinder.module.visualizing.query;

import de.cubbossa.pathfinder.antlr.QueryLanguageBaseVisitor;
import de.cubbossa.pathfinder.antlr.QueryLanguageParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryLanguageVisitor<T extends SearchTermHolder>
    extends QueryLanguageBaseVisitor<Collection<T>> {

  private final Collection<T> scope;

  @Override
  public Collection<T> visitProgram(QueryLanguageParser.ProgramContext ctx) {
    return visitExpression(ctx.expression());
  }

  @Override
  public Collection<T> visitExpression(QueryLanguageParser.ExpressionContext context) {
    if (context.lhs == null) {
      return visit(context.term());
    }
    if (context.rhs == null || context.op == null) {
      if (context.NOT() != null) {
        Collection<T> childrenResult = visit(context.lhs);
        return scope.stream().filter(t -> !childrenResult.contains(t)).collect(Collectors.toList());
      } else {
        return visit(context.lhs);
      }
    }

    if (context.AND() != null) {
      Collection<T> result = new ArrayList<>(visit(context.lhs));
      return visit(context.rhs).stream().filter(result::contains).collect(Collectors.toList());
    } else {
      Collection<T> result = new ArrayList<>(visit(context.lhs));
      result.addAll(visit(context.rhs));
      return result;
    }
  }

  @Override
  public Collection<T> visitTerm(QueryLanguageParser.TermContext context) {
    String searchTerm = context.IDENTIFIER().getText();
    Collection<SearchQueryAttribute> attributes = context.attributeblock() == null ?
        new ArrayList<>() : visitAnAttributeBlock(context.attributeblock());

    return scope.stream().filter(t -> t.matches(searchTerm, attributes))
        .collect(Collectors.toList());
  }

  public Collection<SearchQueryAttribute> visitAnAttributeBlock(
      QueryLanguageParser.AttributeblockContext context) {
    return context.attributelist() == null ? new ArrayList<>()
        : visitAnAttributeList(context.attributelist());
  }

  public Collection<SearchQueryAttribute> visitAnAttributeList(
      QueryLanguageParser.AttributelistContext context) {
    Collection<SearchQueryAttribute> result = new ArrayList<>();
    if (context.attributelist() != null) {
      result.addAll(visitAnAttributeList(context.attributelist()));
    }
    result.add(visitAnAttribute(context.attribute()));
    return result;
  }

  public SearchQueryAttribute visitAnAttribute(QueryLanguageParser.AttributeContext context) {
    return new SearchQueryAttribute(
        context.IDENTIFIER().getText(),
        SearchQueryAttribute.Comparator.fromString(context.comparator().getText()),
        context.value().getText()
    );
  }
}
