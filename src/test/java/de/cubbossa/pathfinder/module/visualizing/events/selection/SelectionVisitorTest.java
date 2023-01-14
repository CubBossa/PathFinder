package de.cubbossa.pathfinder.module.visualizing.events.selection;

import de.cubbossa.pathfinder.antlr.SelectionLanguageLexer;
import de.cubbossa.pathfinder.antlr.SelectionLanguageParser;
import de.cubbossa.pathfinder.util.selection.SelectionVisitor;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

class SelectionVisitorTest {

  @Test
  public void test() {
    CharStream charStream = CharStreams.fromString("@e[some=1,condition=2,in=3,order=4]");
    SelectionLanguageLexer lexer = new SelectionLanguageLexer(charStream);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    SelectionLanguageParser parser = new SelectionLanguageParser(tokens);
    SelectionVisitor visitor = new SelectionVisitor(List.of("e"));

    SelectionLanguageParser.ProgramContext tree = parser.program();
    System.out.println(visitor.visit(tree));
  }

}