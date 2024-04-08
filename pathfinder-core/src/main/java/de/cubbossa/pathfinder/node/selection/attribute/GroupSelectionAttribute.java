package de.cubbossa.pathfinder.node.selection.attribute;

import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.NodeSelectionAttribute;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.pf4j.Extension;

@Getter
@Extension(points = NodeSelectionAttribute.class)
public class GroupSelectionAttribute implements NodeSelectionAttribute<Collection<NodeGroup>> {

  private final String key = "group";

  @Override
  public ArgumentType<Collection<NodeGroup>> getValueType() {
    return r -> {
      String in = r.getRemaining();
      Collection<NodeGroup> groups = new HashSet<>();
      NamespacedKey key;
      try {
        key = NamespacedKey.fromString(in);
      } catch (Throwable t) {
        throw new IllegalArgumentException("Invalid namespaced key: '" + in + "'.");
      }
      Optional<NodeGroup> group = PathFinderProvider.get().getStorage().loadGroup(key).join();
      groups.add(group.orElseThrow(
          () -> new IllegalArgumentException("There is no group with the key '" + key + "'")));
      return groups;
    };
  }

  @Override
  public Type getAttributeType() {
    return Type.FILTER;
  }

  @Override
  public List<Node> execute(AbstractNodeSelectionParser.NodeArgumentContext<Collection<NodeGroup>> context) {
    return context.getScope().stream()
        .filter(node -> StorageUtil.getGroups(node).containsAll(context.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getStringSuggestions(SelectionParser.SuggestionContext context) {
    return PathFinderProvider.get().getStorage().loadAllGroups().join().stream()
        .map(NodeGroup::getKey)
        .map(NamespacedKey::toString)
        .collect(Collectors.toList());
  }
}
