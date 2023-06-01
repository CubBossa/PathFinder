package de.cubbossa.pathfinder.nodeselection;

import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathapi.node.Node;
import lombok.Getter;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class NodeSelectionParser extends SelectionParser<Node, NodeSelectionParser.Context<?>> {

  public NodeSelectionParser(String identifier, String... alias) {
    super(identifier, alias);
  }

  public static <T> Argument<T> argument(ArgumentType<T> type) {
    return new Argument<>(type);
  }

  public <S> Collection<Node> parse(Player player, String input, List<Node> scope)
      throws ParseCancellationException {
    return super.<S>parse(input, scope, (o, nodes) -> new Context<>(o, scope, player));
  }

  public static class Argument<T>
      extends SelectionParser.Argument<T, Node, Context<T>, Argument<T>> {

    public Argument(ArgumentType<T> type) {
      super(type);
    }
  }

  @Getter
  public class Context<T> extends ArgumentContext<T, Node> {
    private final CommandSender sender;

    public Context(T value, List<Node> scope, CommandSender sender) {
      super(value, scope);
      this.sender = sender;
    }
  }
}
