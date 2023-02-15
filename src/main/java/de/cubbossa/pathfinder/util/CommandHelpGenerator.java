package de.cubbossa.pathfinder.util;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;

public class CommandHelpGenerator {

  private record Entry(String cmd, String insert, String desc) {
  }


  public CommandHelpGenerator() {

  }

  public List<Component> format(CommandAPICommand tree) {
    entries("/", tree).forEach(entry -> System.out.println(entry.cmd + " - " + entry.desc));
    return List.of(Component.empty());
  }

  private List<Entry> entries(String cmd, CommandAPICommand tree) {
    List<Entry> result = new ArrayList<>();
    if (tree.getExecutor().hasAnyExecutors()) {
      String argString = tree.getArguments().stream()
          .map(this::argumentRepresentation)
          .collect(Collectors.joining(" "));
      if (argString.length() > 0) {
        argString = " " + argString;
      }

      result.add(
          new Entry(cmd + " " + tree.getName() + argString, "", tree.getFullDescription()));
    }
    if (tree.getSubcommands().size() > 0) {
      result.addAll(tree.getSubcommands().stream()
          .map(sc -> entries(cmd + " " + tree.getName(), sc))
          .flatMap(Collection::stream).toList());
    }
    return result;
  }

  private String argumentRepresentation(Argument<?> argument) {
    return switch (argument.getArgumentType()) {
      case LITERAL -> ((LiteralArgument) argument).getLiteral();
      case MULTI_LITERAL -> String.join("|", ((MultiLiteralArgument) argument).getLiterals());
      default -> "<" + argument.getNodeName() + ">";
    };
  }

}
