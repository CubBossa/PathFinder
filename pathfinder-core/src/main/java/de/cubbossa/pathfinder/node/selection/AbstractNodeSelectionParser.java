package de.cubbossa.pathfinder.node.selection;

import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public abstract class AbstractNodeSelectionParser<SenderT, ContextT extends AbstractNodeSelectionParser.NodeArgumentContext<?, ?>> extends SelectionParser<Node, ContextT> {
  public AbstractNodeSelectionParser(String identifier, String... alias) {
    super(identifier, alias);
  }

  public void addResolver(NodeSelectionArgument<?> argument) {
    super.addResolver((Argument<?, ? extends Node, ? extends ContextT, ?>) argument);
  }

  public abstract Collection<Node> parse(String input, List<Node> scope, SenderT sender);

  @Override
  public <ValueT> Collection<Node> parse(String input, List<Node> scope, BiFunction<ValueT, List<Node>, ContextT> context) throws ParseCancellationException {
    return super.parse(input, scope, context);
  }

  public static abstract class NodeArgumentContext<SenderT, ValueT> extends ArgumentContext<ValueT, Node> {

    public NodeArgumentContext(ValueT value, List<Node> scope) {
      super(value, scope);
    }

    public abstract SenderT getSender();

    public abstract Location getSenderLocation();
  }

  public static abstract class NodeSelectionArgument<TypeT> extends Argument<TypeT, Node, NodeArgumentContext<?, TypeT>, NodeSelectionArgument<TypeT>> {

    public NodeSelectionArgument(ArgumentType<TypeT> type) {
      super(type);
    }
  }
}
