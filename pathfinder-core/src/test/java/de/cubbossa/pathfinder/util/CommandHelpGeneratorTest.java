package de.cubbossa.pathfinder.util;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.junit.jupiter.api.Test;

import java.util.List;

class CommandHelpGeneratorTest {

  @Test
  void format() {

    CommandAPICommand cmd = new CommandAPICommand("test")
        .withFullDescription("bla1")
        .withSubcommand(new CommandAPICommand("sub1")
            .withFullDescription("bla2")
            .withArguments(new IntegerArgument("int"), new StringArgument("name"))
            .executes((commandSender, objects) -> {
            })
        )
        .withSubcommand(new CommandAPICommand("gamemode")
            .withFullDescription("bla3")
            .withArguments(new MultiLiteralArgument("creative", List.of("survival", "adventure")))
            .executes((commandSender, objects) -> {
            })
        )
        .withSubcommand(new CommandAPICommand("gamemode")
            .withFullDescription("bla4")
            .withArguments(new IntegerArgument("test"))
            .executes((commandSender, objects) -> {
            }))
        .executes((commandSender, objects) -> {
        });
  }
}