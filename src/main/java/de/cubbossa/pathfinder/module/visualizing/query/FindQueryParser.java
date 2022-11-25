package de.cubbossa.pathfinder.module.visualizing.query;

import de.cubbossa.pathfinder.antlr.QueryLanguageLexer;
import de.cubbossa.pathfinder.antlr.QueryLanguageParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Collection;
import java.util.List;

public class FindQueryParser {

	public Collection<SearchTermHolder> parse(String input, List<SearchTermHolder> scope) {

		CharStream charStream = CharStreams.fromString(input);
		QueryLanguageLexer lexer = new QueryLanguageLexer(charStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		QueryLanguageParser parser = new QueryLanguageParser(tokens);

		//TODO errorparserlistener

		QueryLanguageParser.ProgramContext tree = parser.program();
		return new QueryLanguageVisitor<>(scope).visit(tree);
	}
}
