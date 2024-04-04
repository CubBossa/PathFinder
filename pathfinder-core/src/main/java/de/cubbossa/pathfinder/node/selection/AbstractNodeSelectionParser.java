package de.cubbossa.pathfinder.node.selection;

import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.List;

public abstract class AbstractNodeSelectionParser<SenderT, ContextT extends AbstractNodeSelectionParser.NodeArgumentContext<?>> extends SelectionParser<Node, ContextT> {
  public AbstractNodeSelectionParser(String identifier, String... alias) {
    super(identifier, alias);
  }

  public void addResolver(NodeSelectionArgument<?> argument) {
    super.addResolver((Argument<?, ? extends Node, ? extends ContextT, ?>) argument);
  }

  public static abstract class NodeArgumentContext<ValueT> extends ArgumentContext<ValueT, Node> {

    public NodeArgumentContext(ValueT value, List<Node> scope) {
      super(value, scope);
    }
  }

  public static abstract class NodeSelectionArgument<TypeT> extends Argument<TypeT, Node, NodeArgumentContext<TypeT>, NodeSelectionArgument<TypeT>> {

    public NodeSelectionArgument(ArgumentType<TypeT> type) {
      super(type);
    }
  }
}
