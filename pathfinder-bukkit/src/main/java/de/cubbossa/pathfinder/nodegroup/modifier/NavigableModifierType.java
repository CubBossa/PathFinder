package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.group.NavigableModifier;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.query.SearchTerm;
import de.cubbossa.pathfinder.command.ModifierCommandExtension;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.navigationquery.SearchTermImpl;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.pf4j.Extension;

@Extension(points = ModifierType.class)
public class NavigableModifierType implements ModifierType<NavigableModifier>,
    ModifierCommandExtension<NavigableModifier> {

  @Getter
  private final NamespacedKey key = NamespacedKey.fromString("pathfinder:navigable");

  @Override
  public String getSubCommandLiteral() {
    return "searchterms";
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
      return new NavigableModifierImpl(Arrays.stream(str.split(","))
          .map(SearchTermImpl::new).collect(Collectors.toSet()));
    }
    throw new IOException(
        "Could not deserialize NavigableModifier, missing 'search-terms' attribute.");
  }

  @Override
  public ComponentLike toComponents(NavigableModifier modifier) {
    return Messages.CMD_NG_MOD_SEARCH.formatted(
        Messages.formatter().list("terms", modifier.getSearchTermStrings(), Component::text)
    );
  }

  @Override
  public Argument<?> registerAddCommand(Argument<?> tree, Function<NavigableModifier, CommandExecutor> consumer) {
    return tree.then(new GreedyStringArgument("search-terms").executes((commandSender, args) -> {
      consumer.apply(new NavigableModifierImpl(Arrays.stream(args.<String>getUnchecked(1).split(","))
              .map(String::trim)
              .map(s -> s.replace(' ', '_'))
              .toArray(String[]::new)))
          .run(commandSender, args);
    }));
  }
}
