package de.cubbossa.pathfinder.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommandPlaceholderProcessorImplTest {

  @Test
  void addResolver() {
  }

  @Test
  void process() {

    CommandPlaceholderProcessorImpl processor = new CommandPlaceholderProcessorImpl();
    processor.addResolver(CmdTagResolver.tag("test", u -> "test"));

    Assertions.assertEquals("some test x", processor.process("some ${test} x"));
    Assertions.assertEquals("some test x", processor.process("some ${ test} x"));
    Assertions.assertEquals("some test x", processor.process("some ${test } x"));
    Assertions.assertEquals("some test x", processor.process("some ${ test } x"));
    Assertions.assertEquals("some test", processor.process("some ${test.${test.${test}.test.${test.${test}}}}"));
    Assertions.assertEquals("some test", processor.process("some ${test}${other}"));
  }
}