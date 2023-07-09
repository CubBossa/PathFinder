package de.cubbossa.pathfinder.v3;

import de.cubbossa.pathapi.misc.NamespacedKey;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Deprecated(forRemoval = true)
public interface V3Storage {

  void connect();

  void disconnect();

  Collection<V3RoadMap> loadRoadmaps();

  Collection<V3Edge> loadEdges();

  Collection<V3Node> loadNodes();

  Collection<V3GroupNode> loadGroupNodes();

  Collection<V3NodeGroup> loadNodeGroups();

  Collection<V3SearchTerm> loadSearchTerms();

  Collection<V3Discovering> loadDiscoverings();

  Collection<V3Visualizer> loadVisualizers();

  record V3RoadMap(NamespacedKey key, String nameFormat, NamespacedKey vis, double curveLength) {
  }

  record V3Edge(int start, int end, float weight) {
  }

  record V3Node(int id, NamespacedKey type, NamespacedKey roadmap, double x, double y, double z, UUID world,
                Double curveLength) {
  }

  record V3GroupNode(int id, NamespacedKey group) {
  }

  record V3NodeGroup(NamespacedKey key, String nameFormat, @Nullable String perm, boolean navigable,
                     boolean discoverable, Double findDistance) {
  }

  record V3SearchTerm(NamespacedKey group, String key) {
  }

  record V3Discovering(UUID player, NamespacedKey group, LocalDateTime time) {
  }

  record V3Visualizer(NamespacedKey key, NamespacedKey type, @Nullable String nameFormat, @Nullable String permission,
                      int interval, String cfg) {
  }
}
