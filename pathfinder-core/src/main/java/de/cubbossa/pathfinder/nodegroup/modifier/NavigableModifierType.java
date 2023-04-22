package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.visualizer.query.SearchTerm;
import de.cubbossa.pathfinder.commands.ModifierCommandExtension;
import de.cubbossa.pathfinder.navigationquery.SimpleSearchTerm;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.io.IOException;
import java.util.Arrays;
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
    return new LinkedHashMapBuilder<String, Object>()
        .put("search-terms", modifier.getSearchTerms().stream().map(SearchTerm::getIdentifier).collect(Collectors.joining(",")))
        .build();
  }

  @Override
  public NavigableModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("search-terms") && values.get("search-terms") instanceof String str) {
      return new NavigableModifier(Arrays.stream(str.split(","))
          .map(SimpleSearchTerm::new).collect(Collectors.toSet()));
    }
    throw new IOException("Could not deserialize NavigableModifier, missing 'search-terms' attribute.");
  }

  @Override
  public ArgumentTree registerAddCommand(ArgumentTree tree, Function<NavigableModifier, CommandExecutor> consumer) {
    return tree.then(new GreedyStringArgument("search-terms").executes((commandSender, objects) -> {
      consumer.apply(new NavigableModifier(((String) objects[1]).split(","))).run(commandSender, objects);
    }));
  }
}
