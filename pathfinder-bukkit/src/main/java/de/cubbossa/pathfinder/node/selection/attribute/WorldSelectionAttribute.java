package de.cubbossa.pathfinder.node.selection.attribute;

import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldSelectionAttribute extends AbstractNodeSelectionParser.NodeSelectionArgument<World> {

  @Getter
  private final String key = "world";

  public WorldSelectionAttribute() {
    super(r -> {
      World world = Bukkit.getWorld(r.getRemaining());
      if (world == null) {
        throw new RuntimeException("'" + r.getRemaining() + "' is not a valid world.");
      }
      return world;
    });

    execute(c -> c.getScope().stream()
        .filter(node -> Objects.equals(node.getLocation().getWorld().getUniqueId(), c.getValue().getUID()))
        .collect(Collectors.toList()));

    suggestStrings(c -> Bukkit.getWorlds().stream()
        .map(World::getName)
        .collect(Collectors.toList()));
  }

  @Override
  public SelectionParser.SelectionModification modificationType() {
    return SelectionParser.SelectionModification.FILTER;
  }
}
