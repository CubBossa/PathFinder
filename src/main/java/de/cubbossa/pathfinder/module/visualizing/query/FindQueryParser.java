package de.cubbossa.pathfinder.module.visualizing.query;

import de.cubbossa.pathfinder.antlr.QueryLanguageLexer;
import de.cubbossa.pathfinder.antlr.QueryLanguageParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.Collection;
import java.util.List;

public class FindQueryParser {

	public <T extends SearchTermHolder> Collection<T> parse(String input, List<T> scope) throws ParseCancellationException {

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
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
			throw new ParseCancellationException(msg, e);
		}
	}
}
