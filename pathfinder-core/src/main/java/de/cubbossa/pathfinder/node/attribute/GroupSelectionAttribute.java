package de.cubbossa.pathfinder.node.attribute;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.storage.StorageUtil;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;

public class GroupSelectionAttribute extends AbstractNodeSelectionParser.NodeSelectionArgument<Collection<NodeGroup>> {

  @Getter
  private final String key = "group";

  public GroupSelectionAttribute() {
    super(r -> {
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
    });

    execute(c -> c.getScope().stream()
        .filter(node -> StorageUtil.getGroups(node).containsAll(c.getValue()))
        .collect(Collectors.toList()));

    suggestStrings(c -> PathFinderProvider.get().getStorage().loadAllGroups().join().stream()
        .map(NodeGroup::getKey)
        .map(NamespacedKey::toString)
        .collect(Collectors.toList()));
  }

  @Override
  public SelectionParser.SelectionModification modificationType() {
    return SelectionParser.SelectionModification.FILTER;
  }
}
