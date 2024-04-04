package de.cubbossa.pathfinder.node.selection.attribute;

import com.google.auto.service.AutoService;
import com.mojang.brigadier.arguments.ArgumentType;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Node;
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

@Getter
@AutoService(NodeSelectionAttribute.class)
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
