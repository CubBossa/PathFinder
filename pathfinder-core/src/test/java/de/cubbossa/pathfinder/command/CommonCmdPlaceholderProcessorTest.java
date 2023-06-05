package de.cubbossa.pathfinder.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommonCmdPlaceholderProcessorTest {

  @Test
  void addResolver() {
  }

  @Test
  void process() {

    CommonCmdPlaceholderProcessor processor = new CommonCmdPlaceholderProcessor();
    processor.addResolver(CmdTagResolver.tag("test", u -> "test"));

    Assertions.assertEquals("some test", processor.process("some <test>"));
    Assertions.assertEquals("some test", processor.process("some <test:<test:<test>:test:<test:<test>>>>"));
    Assertions.assertEquals("some test<other>", processor.process("some <test><other>"));
  }
}