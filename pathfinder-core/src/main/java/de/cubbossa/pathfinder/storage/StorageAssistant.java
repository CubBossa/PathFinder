package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.node.Groupable;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.SimpleNodeGroup;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StorageAssistant {

  public <M extends Modifier> CompletableFuture<Map<Node<?>, M>> loadNodes(Class<M> modifier) {
    return PathPlugin.getInstance().getStorage().loadNodes().thenApply(nodes -> {
      Map<Node<?>, TreeMap<Float, M>> results = new HashMap<>();
      nodes.stream()
          .filter(node -> node instanceof Groupable<?>)
          .map(node -> (Groupable<?>) node)
          .forEach(groupable -> {
            for (NodeGroup group : groupable.getGroups()) {
              if (group.hasModifier(modifier)) {
                results.computeIfAbsent(groupable, g -> new TreeMap<>()).put(group.getWeight(), group.getModifier(modifier));
              }
            }
          });
      Map<Node<?>, M> result = new HashMap<>();
      for (Map.Entry<Node<?>, TreeMap<Float, M>> e : results.entrySet()) {
        result.put(e.getKey(), e.getValue().lastEntry().getValue());
      }
      return result;
    });
  }

}
