package de.cubbossa.pathfinder.node.selection;

import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.List;
import lombok.Getter;
import org.bukkit.command.CommandSender;

public class BukkitNodeSelectionParser extends AbstractNodeSelectionParser<CommandSender, BukkitNodeSelectionParser.BukkitNodeArgumentContext<?>> {

  public BukkitNodeSelectionParser(String identifier, String... alias) {
    super(identifier, alias);
  }

  @Override
  public <ValueT> BukkitNodeArgumentContext<?> createContext(ValueT value, List<Node> scope, Object sender) {
    if (!(sender instanceof CommandSender)) {
      throw new IllegalArgumentException("Expecting sender to be instance of bukkit CommandSender.");
    }
    return new BukkitNodeArgumentContext<>(value, scope, (CommandSender) sender);
  }

  public static abstract class BukkitNodeSelectionArgument<T>
      extends SelectionParser.Argument<T, Node, BukkitNodeArgumentContext<T>, BukkitNodeSelectionArgument<T>> {

    public BukkitNodeSelectionArgument(ArgumentType<T> type) {
      super(type);
    }
  }

  @Getter
  public static class BukkitNodeArgumentContext<ValueT> extends NodeArgumentContext<ValueT> {
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
