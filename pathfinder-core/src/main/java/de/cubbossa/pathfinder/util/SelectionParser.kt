package de.cubbossa.pathfinder.util

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import de.cubbossa.pathfinder.antlr.SelectionLanguageLexer
import de.cubbossa.pathfinder.antlr.SelectionLanguageParser
import de.cubbossa.pathfinder.antlr.SelectionSuggestionLanguageLexer
import de.cubbossa.pathfinder.antlr.SelectionSuggestionLanguageParser
import de.cubbossa.pathfinder.misc.Location
import de.cubbossa.pathfinder.node.selection.SelectSuggestionVisitor
import de.cubbossa.pathfinder.node.selection.SelectionVisitor
import de.cubbossa.pathfinder.util.SelectionParser.ArgumentContext
import lombok.Getter
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

abstract class SelectionParser<TypeT, ContextT : ArgumentContext<*, TypeT>>(identifier: String, vararg alias: String) {

    private val identifiers: MutableCollection<String> = ArrayList()
    private val arguments: MutableCollection<Argument<*, TypeT, ContextT, *>> = TreeSet()

    init {
        identifiers.add(identifier)
        identifiers.addAll(Arrays.stream(alias).toList())
    }

    fun addResolver(argument: Argument<*, out TypeT, out ContextT, *>) {
        arguments.add(argument as Argument<*, TypeT, ContextT, *>)
    }

    abstract fun <ValueT> createContext(value: ValueT, scope: List<TypeT>, sender: Any?): ContextT

    @Throws(ParseCancellationException::class)
    fun <ValueT> parse(input: String, scope: MutableList<TypeT>, sender: Any? = null): MutableList<TypeT> {

        val charStream: CharStream = CharStreams.fromString(input)
        val lexer = SelectionLanguageLexer(charStream)
        val tokens = CommonTokenStream(lexer)
        val parser = SelectionLanguageParser(tokens)

        val listener = ErrorListener()

        lexer.removeErrorListeners()
        lexer.addErrorListener(listener)
        parser.removeErrorListeners()
        parser.addErrorListener(listener)

        val tree = parser.program()
        var attributes = SelectionVisitor(identifiers).visit(tree)
        if (attributes == null) {
            attributes = ArrayList()
        }

        var scopeHolder: MutableList<TypeT> = ArrayList(scope)
        for (argument in arguments) {
            if (argument.execute == null) {
                continue
            }
            for ((identifier, value1) in attributes) {
                if (identifier != argument.key) {
                    continue
                }
                val value = argument.parse(value1) as ValueT
                scopeHolder = argument.execute!!(
                    createContext(value, scopeHolder, sender)
                )
            }
        }
        return scopeHolder
    }

    fun applySuggestions(
        command: String,
        input: String
    ): CompletableFuture<Suggestions> {
        val charStream: CharStream = CharStreams.fromString(input)
        val lexer = SelectionSuggestionLanguageLexer(charStream)
        val tokens = CommonTokenStream(lexer)
        val parser = SelectionSuggestionLanguageParser(tokens)

        parser.removeErrorListeners()

        val tree = parser.program()
        val map: MutableMap<String, (SuggestionContext) -> List<Suggestion>> = HashMap()
        arguments.forEach { a: Argument<*, TypeT, ContextT, *> ->
            if (a.suggest == null) {
                map.remove(a.key)
            } else {
                map[a.key] = a.suggest!!
            }
        }

        val suggestions: List<Suggestion> =
            SelectSuggestionVisitor(identifiers, map, input, null).visit(tree)

        return CompletableFuture.completedFuture(Suggestions.create(command, suggestions))
    }

    abstract class ArgumentContext<ValueT, TypeT>(
        val value: ValueT,
        val scope: List<TypeT>
    ) {
        abstract val sender: Any
        abstract val senderLocation: Location
    }

    data class SuggestionContext(
        val sender: Any?,
        val input: String
    )

    @Getter
    abstract class Argument<ValueT, TypeT, ContextT : ArgumentContext<*, TypeT>, ArgumentT : Argument<ValueT, TypeT, ContextT, ArgumentT>>(
        val type: ArgumentType<ValueT>
    ) : Comparable<ArgumentT> {

        var execute: ((ContextT) -> MutableList<TypeT>)? = null
        var suggest: ((SuggestionContext) -> List<Suggestion>)? = null

        abstract val key: String

        abstract fun modificationType(): SelectionModification

        fun parse(s: String): ValueT {
            try {
                return type.parse(StringReader(s))
            } catch (e: CommandSyntaxException) {
                throw RuntimeException(e)
            }
        }

        open fun executeAfter(): Collection<String> {
            return emptyList()
        }

        override fun compareTo(other: ArgumentT): Int {
            val compare = modificationType().ordinal.compareTo(other.modificationType().ordinal)
            if (compare != 0) {
                return compare
            }
            if (other.executeAfter().contains(key)) {
                return 1
            }
            if (executeAfter().contains(other.key)) {
                return -1
            }
            return java.lang.String.CASE_INSENSITIVE_ORDER.compare(key, other.key)
        }

        fun execute(execute: (ContextT) -> MutableList<TypeT>): ArgumentT {
            this.execute = execute
            return this as ArgumentT
        }

        fun suggest(suggest: List<Suggestion>): ArgumentT {
            this.suggest = { context: SuggestionContext -> suggest }
            return this as ArgumentT
        }

        fun suggestStrings(suggest: List<String>): ArgumentT {
            this.suggest = { context: SuggestionContext ->
                suggest.stream()
                    .map { s: String -> Suggestion(StringRange.between(0, context.input!!.length), s) }
                    .collect(Collectors.toList())
            }
            return this as ArgumentT
        }

        fun suggest(suggest: (SuggestionContext) -> List<Suggestion>): ArgumentT {
            this.suggest = suggest
            return this as ArgumentT
        }

        fun suggestStrings(suggest: (SuggestionContext) -> List<String>): ArgumentT {
            this.suggest = { context: SuggestionContext ->
                suggest(context).stream()
                    .map { s: String -> Suggestion(StringRange.between(0, context.input!!.length), s) }
                    .collect(Collectors.toList())
            }
            return this as ArgumentT
        }
    }

    enum class SelectionModification {
        SORT,
        FILTER,
        PEEK
    }

    class ErrorListener : BaseErrorListener() {
        override fun syntaxError(
            recognizer: Recognizer<*, *>, offendingSymbol: Any, line: Int,
            charPositionInLine: Int, msg: String, e: RecognitionException
        ) {
            throw ParseCancellationException(msg, e)
        }
    }
}
