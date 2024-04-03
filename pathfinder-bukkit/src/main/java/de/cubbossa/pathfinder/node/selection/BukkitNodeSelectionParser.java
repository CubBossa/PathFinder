package de.cubbossa.pathfinder.node.selection;

import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.command.CommandSender;

public class BukkitNodeSelectionParser extends AbstractNodeSelectionParser<CommandSender, BukkitNodeSelectionParser.BukkitNodeArgumentContext<?>> {

  public BukkitNodeSelectionParser(String identifier, String... alias) {
    super(identifier, alias);
  }

  public Collection<Node> parse(String input, List<Node> scope, CommandSender sender)
      throws ParseCancellationException {
    return super.parse(input, scope, (o, nodes) -> new BukkitNodeArgumentContext<>(o, nodes, sender));
  }

  public static abstract class BukkitNodeSelectionArgument<T>
      extends SelectionParser.Argument<T, Node, BukkitNodeArgumentContext<T>, BukkitNodeSelectionArgument<T>> {

    public BukkitNodeSelectionArgument(ArgumentType<T> type) {
      super(type);
    }
  }

  @Getter
  public static class BukkitNodeArgumentContext<ValueT> extends NodeArgumentContext<CommandSender, ValueT> {
    private final CommandSender sender;

    public BukkitNodeArgumentContext(ValueT value, List<Node> scope, CommandSender sender) {
      super(value, scope);
      this.sender = sender;
    }

    @Override
    public Location getSenderLocation() {
      return BukkitPathFinder.getInstance().wrap(sender).getLocation();
    }
  }
}
