package de.cubbossa.pathfinder.node.selection

import de.cubbossa.pathfinder.antlr.SelectionLanguageBaseVisitor
import de.cubbossa.pathfinder.antlr.SelectionLanguageParser

class SelectionVisitor(private val identifiers: Collection<String>) :
    SelectionLanguageBaseVisitor<List<ParsedSelectionAttribute>>() {
    override fun visitProgram(ctx: SelectionLanguageParser.ProgramContext): List<ParsedSelectionAttribute>? {
        return visitExpression(ctx.expression())
    }

    override fun visitExpression(ctx: SelectionLanguageParser.ExpressionContext): List<ParsedSelectionAttribute>? {
        check(identifiers.contains(ctx.selector().IDENTIFIER().symbol.text)) {
            "Invalid identifier: " + ctx.selector().IDENTIFIER().symbol.text
        }
        return if (ctx.conditions() == null) null else visitConditions(ctx.conditions())
    }

    override fun visitConditions(ctx: SelectionLanguageParser.ConditionsContext): List<ParsedSelectionAttribute>? {
        return if (ctx.attributelist() == null) null else visitAttributelist(ctx.attributelist())
    }

    override fun visitAttributelist(
        ctx: SelectionLanguageParser.AttributelistContext
    ): List<ParsedSelectionAttribute> {
        val list: MutableList<ParsedSelectionAttribute> = ArrayList()
        if (ctx.attributelist() != null) {
            list.addAll(visitAttributelist(ctx.attributelist()))
        }
        list.addAll(visitAttribute(ctx.attribute()))
        return list
    }

    override fun visitAttribute(ctx: SelectionLanguageParser.AttributeContext): List<ParsedSelectionAttribute> {
        val identifier = ctx.IDENTIFIER().text
        return listOf(ParsedSelectionAttribute(identifier, ctx.value().text))
    }
}
