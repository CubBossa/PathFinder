package de.cubbossa.pathfinder.util.selection;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathfinder.core.node.Node;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NodeSelectionParser extends SelectionParser<Node, NodeSelectionParser.Context<?>> {

  public static class Argument<T>
      extends SelectionParser.Argument<T, Node, Context<T>, Argument<T>> {

    public Argument(ArgumentType<T> type) {
      super(type);
    }
  }

  public NodeSelectionParser(String identifier, String... alias) {
    super(identifier, alias);
  }

  public static <T> Argument<T> argument(ArgumentType<T> type) {
    return new Argument<>(type);
  }

  public <S> Collection<Node> parse(Player player, String input, List<Node> scope)
      throws ParseCancellationException, CommandSyntaxException {
    return super.parse(input, scope, (o, nodes) -> new Context<>(input, scope, player));
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
