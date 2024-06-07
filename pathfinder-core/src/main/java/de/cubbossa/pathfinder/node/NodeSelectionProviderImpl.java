package de.cubbossa.pathfinder.node;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute;
import de.cubbossa.pathfinder.util.ExtensionPoint;
import de.cubbossa.pathfinder.util.SelectionParser;
import dev.jorel.commandapi.SuggestionInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class NodeSelectionProviderImpl<SenderT, ContextT extends AbstractNodeSelectionParser.NodeArgumentContext<?>> extends NodeSelectionProvider {

  static final ExtensionPoint<NodeSelectionAttribute> EXTENSION_POINT = new ExtensionPoint<>(NodeSelectionAttribute.class);

  private final AbstractNodeSelectionParser<SenderT, ContextT> parser;

  public NodeSelectionProviderImpl(AbstractNodeSelectionParser<SenderT, ContextT> parser) {
    this.parser = parser;
    EXTENSION_POINT.getExtensions().forEach(this::add);
    NodeSelectionProvider.provider = this;
  }

  private <T> void add(NodeSelectionAttribute<T> i) {
    parser.addResolver(new AbstractNodeSelectionParser.NodeSelectionArgument<>(i.valueType) {
      @Override
      public String getKey() {
        return i.key;
      }

      @Override
      public SelectionParser.SelectionModification modificationType() {
        return SelectionParser.SelectionModification.valueOf(i.attributeType.name());
      }

      @Override
      public Collection<String> executeAfter() {
        return i.executeAfter();
      }

      @Override
      public Function<AbstractNodeSelectionParser.NodeArgumentContext<T>, List<Node>> getExecute() {
        return i::execute;
      }

      @Override
      public Function<SelectionParser.SuggestionContext, List<Suggestion>> getSuggest() {
        return c -> {
          ArrayList<Suggestion> suggestions = new ArrayList<>(i.getSuggestions(c));
          suggestions.addAll(i.getStringSuggestions(c).stream()
              .map(string -> new Suggestion(StringRange.between(0, c.getInput().length()), string))
              .toList());
          return suggestions;
        };
      }
    });
  }

  @Override
  protected NodeSelection of(String selection) {
    List<Node> scope = new ArrayList<>(PathFinder.get().getStorage().loadNodes().join());
    scope = parser.parse(selection, scope, null);
    return new NodeSelectionImpl(scope, selection);
  }

  @Override
  protected NodeSelection of(String selection, Iterable<Node> scope) {
    List<Node> _scope = new ArrayList<>();
    scope.forEach(_scope::add);
    _scope = parser.parse(selection, _scope, null);
    return new NodeSelectionImpl(_scope, selection);
  }

  @Override
  protected NodeSelection of(Iterable<Node> scope) {
    List<Node> _scope = new ArrayList<>();
    scope.forEach(_scope::add);
    return new NodeSelectionImpl(_scope);
  }

  @Override
  protected NodeSelection ofSender(String selection, Object sender) {
    List<Node> scope = new ArrayList<>(PathFinder.get().getStorage().loadNodes().join());
    scope = parser.parse(selection, scope, sender);
    return new NodeSelectionImpl(scope, selection);
  }

  @Override
  protected NodeSelection ofSender(String selection, Iterable<Node> scope, Object sender) {
    List<Node> _scope = new ArrayList<>();
    scope.forEach(_scope::add);
    _scope = parser.parse(selection, _scope, sender);
    return new NodeSelectionImpl(_scope, selection);
  }

  public static CompletableFuture<Suggestions> getNodeSelectionSuggestions(SuggestionInfo suggestionInfo) {
    return ((NodeSelectionProviderImpl) provider).parser.applySuggestions(suggestionInfo.currentArg(), suggestionInfo.currentArg().length() > 0
        ? suggestionInfo.currentArg().substring(1)
        : "");
  }
}
