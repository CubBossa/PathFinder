package de.cubbossa.pathfinder.node.selection;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.Suggestion;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.pf4j.ExtensionPoint;

public interface NodeSelectionAttribute<ValueT> extends ExtensionPoint {

  String getKey();

  ArgumentType<ValueT> getValueType();

  Type getAttributeType();

  default Collection<String> executeAfter() {
    return Collections.emptyList();
  }

  List<Node> execute(AbstractNodeSelectionParser.NodeArgumentContext<ValueT> context);

  default List<Suggestion> getSuggestions(SelectionParser.SuggestionContext context) {
    return Collections.emptyList();
  }

  default List<String> getStringSuggestions(SelectionParser.SuggestionContext context) {
    return Collections.emptyList();
  }

  enum Type {
    SORT,
    FILTER,
    PEEK
  }
}
