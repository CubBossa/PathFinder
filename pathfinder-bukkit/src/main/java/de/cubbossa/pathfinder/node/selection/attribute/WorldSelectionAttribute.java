package de.cubbossa.pathfinder.node.selection.attribute;

import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.pf4j.Extension;

@Getter
@Extension(points = NodeSelectionAttribute.class)
public class WorldSelectionAttribute implements NodeSelectionAttribute<World> {

  private final String key = "world";

  @Override
  public ArgumentType<World> getValueType() {
    return r -> {
      World world = Bukkit.getWorld(r.getRemaining());
      if (world == null) {
        throw new RuntimeException("'" + r.getRemaining() + "' is not a valid world.");
      }
      return world;
    };
  }

  @Override
  public Type getAttributeType() {
    return Type.FILTER;
  }

  @Override
  public List<Node> execute(AbstractNodeSelectionParser.NodeArgumentContext<World> context) {
    return context.getScope().stream()
        .filter(node -> Objects.equals(node.getLocation().getWorld().getUniqueId(), context.getValue().getUID()))
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getStringSuggestions(SelectionParser.SuggestionContext context) {
    return Bukkit.getWorlds().stream()
        .map(World::getName)
        .collect(Collectors.toList());
  }
}
