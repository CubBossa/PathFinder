package de.cubbossa.pathfinder.node.selection

import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import de.cubbossa.pathfinder.antlr.SelectionSuggestionLanguageBaseVisitor
import de.cubbossa.pathfinder.antlr.SelectionSuggestionLanguageParser
import de.cubbossa.pathfinder.util.SelectionParser
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

class SelectSuggestionVisitor(
    private val identifiers: Collection<String>,
    private val arguments: Map<String, (SelectionParser.SuggestionContext) -> List<Suggestion>>,
    private val inputString: String,
    private val player: Player?
) : SelectionSuggestionLanguageBaseVisitor<List<Suggestion>>() {

    override fun visitProgram(ctx: SelectionSuggestionLanguageParser.ProgramContext): List<Suggestion> {
        return visitExpression(ctx.expression())
    }

    override fun visitExpression(ctx: SelectionSuggestionLanguageParser.ExpressionContext): List<Suggestion> {
        if (ctx.AT() == null) {
            return identifiers.stream()
                .map { i: String -> Suggestion(StringRange.at(0), "@$i") }
                .collect(Collectors.toList())
        }
        if (ctx.IDENTIFIER() == null) {
            return identifiers.stream()
                .map { i: String -> Suggestion(StringRange.between(0, 1), "@$i") }
                .collect(Collectors.toList())
        }
        if (ctx.conditions() == null) {
            return java.util.List.of(
                Suggestion(
                    StringRange.at(1 + ctx.IDENTIFIER().text.length),
                    "["
                )
            )
        }
        return visitConditions(ctx.conditions())
    }

    override fun visitConditions(ctx: SelectionSuggestionLanguageParser.ConditionsContext): List<Suggestion> {
        if (ctx.COND_CLOSE() != null) {
            return ArrayList()
        }
        if (ctx.attributelist() != null) {
            return visitAttributelist(ctx.attributelist())
        }
        return suggestArguments("", inputString.length, 0)
    }

    override fun visitAttributelist(
        ctx: SelectionSuggestionLanguageParser.AttributelistContext
    ): List<Suggestion> {
        if (ctx.attribute() != null) {
            return visitAttribute(ctx.attribute())
        }
        if (ctx.COND_DELIMIT() != null) {
            return suggestArguments("", inputString.length, 0)
        }
        return ArrayList()
    }

    override fun visitAttribute(ctx: SelectionSuggestionLanguageParser.AttributeContext): List<Suggestion> {
        if (ctx.value() != null) {
            return suggestValues(
                ctx.IDENTIFIER().text, ctx.value().text,
                inputString.length - ctx.value().text.length
            )
        }
        if (ctx.COND_EQUALS() != null) {
            return suggestValues(ctx.IDENTIFIER().text, "", inputString.length)
        }
        return suggestArguments(
            ctx.IDENTIFIER().text,
            inputString.length - ctx.IDENTIFIER().text.length,
            ctx.IDENTIFIER().text.length
        )
    }

    private fun suggestValues(key: String, input: String, offset: Int): List<Suggestion> {
        val argument = arguments[key]
            ?: return ArrayList()
        val suggestions = argument(
            SelectionParser.SuggestionContext(
                player,
                input
            )
        )
        val transformed: MutableList<Suggestion> = ArrayList()
        suggestions.forEach { suggestion: Suggestion ->
            if (input.isNotEmpty() && !suggestion.text.lowercase(Locale.getDefault()).startsWith(
                    input.lowercase(
                        Locale.getDefault()
                    )
                )
            ) {
                return@forEach
            }
            val range = suggestion.range
            val s = Suggestion(
                StringRange.between(
                    range.start + offset, range.end + offset
                ), suggestion.text, suggestion.tooltip
            )
            transformed.add(s)
        }
        return transformed
    }

    private fun suggestArguments(`in`: String, offset: Int, length: Int): List<Suggestion> {
        return arguments.keys.stream()
            .filter { s: String ->
                s.lowercase(Locale.getDefault()).startsWith(`in`.lowercase(Locale.getDefault()))
            }
            .map { s: String? -> Suggestion(StringRange.between(offset, offset + length), s) }
            .collect(Collectors.toList())
    }
}
