package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.group.NavigableModifier;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.query.SearchTerm;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.navigationquery.SimpleSearchTerm;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NavigableModifierType implements ModifierType<NavigableModifier>,
    ModifierCommandExtension<NavigableModifier> {

  @Getter
  private final NamespacedKey key = NamespacedKey.fromString("pathfinder:navigable");

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
      return new CommonNavigableModifier(Arrays.stream(str.split(","))
          .map(SimpleSearchTerm::new).collect(Collectors.toSet()));
    }
    throw new IOException(
        "Could not deserialize NavigableModifier, missing 'search-terms' attribute.");
  }

  @Override
  public ComponentLike toComponents(NavigableModifier modifier) {
    return Messages.CMD_NG_MOD_SEARCH.formatted(TagResolver.resolver("terms", Messages.formatList(
        modifier.getSearchTermStrings(), Component::text
    )));
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<NavigableModifier, CommandExecutor> consumer) {
    return tree.then(new GreedyStringArgument("search-terms").executes((commandSender, args) -> {
      consumer.apply(new CommonNavigableModifier(args.<String>getUnchecked(1).split(",")))
          .run(commandSender, args);
    }));
  }
}
