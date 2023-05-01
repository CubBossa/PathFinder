package de.cubbossa.pathfinder.modifier;

import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.visualizer.query.SearchTerm;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.navigationquery.SimpleSearchTerm;
import de.cubbossa.pathfinder.nodegroup.modifier.NavigableModifier;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NavigableModifierType implements ModifierType<NavigableModifier>,
    ModifierCommandExtension<NavigableModifier> {

  @Override
  public Class<NavigableModifier> getModifierClass() {
    return NavigableModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "searchable";
  }

  @Override
  public Map<String, Object> serialize(NavigableModifier modifier) {
      LinkedHashMap<String, Object> map = new LinkedHashMap<>();
      map.put("search-terms", modifier.getSearchTerms().stream().map(SearchTerm::getIdentifier)
              .collect(Collectors.joining(",")));
      return map;
  }

  @Override
  public NavigableModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("search-terms") && values.get("search-terms") instanceof String str) {
      return new NavigableModifier(Arrays.stream(str.split(","))
          .map(SimpleSearchTerm::new).collect(Collectors.toSet()));
    }
    throw new IOException(
        "Could not deserialize NavigableModifier, missing 'search-terms' attribute.");
  }

    @Override
    public Argument<?> registerAddCommand(Argument<?> tree, Function<NavigableModifier, CommandExecutor> consumer) {
        return tree.then(new GreedyStringArgument("search-terms").executes((commandSender, args) -> {
            consumer.apply(new NavigableModifier(args.<String>getUnchecked(1).split(",")))
                    .run(commandSender, args);
        }));
    }
}
