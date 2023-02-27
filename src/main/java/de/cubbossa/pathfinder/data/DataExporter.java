package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeGroup;
import de.cubbossa.pathfinder.core.node.NodeGroupHandler;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.visualizer.CombinedVisualizer;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.NodeSelection;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public interface DataExporter {

  static DataExporter all() {
    return storage -> {
      visualizers(VisualizerHandler.getInstance().getPathVisualizerMap().values()).save(storage);
      groupSet(NodeGroupHandler.getInstance().getNodeGroups()).save(storage);
      roadMaps(RoadMapHandler.getInstance().getRoadMaps().values()).save(storage);
    };
  }

  static DataExporter roadMaps(Collection<RoadMap> roadMaps) {
    return storage -> {
      // visualizers before everything else, because they are independent
      visualizers(roadMaps.stream().map(RoadMap::getVisualizer).collect(Collectors.toSet())).save(
          storage);
      // save every roadmap with all its data
      for (RoadMap roadMap : roadMaps) {
        roadMap(roadMap, new NodeSelection(roadMap.getNodes()), true).save(storage);
      }
    };
  }

  static DataExporter roadMap(RoadMap roadMap, NodeSelection nodes) {
    return roadMap(roadMap, nodes, false);
  }

  static DataExporter roadMap(RoadMap roadMap, NodeSelection nodes, boolean ignoreVisualizer) {
    return storage -> {
      if (!ignoreVisualizer) {
        visualizers(Set.of(roadMap.getVisualizer())).save(storage);
      }
      storage.updateRoadMap(roadMap);
      Map<NodeGroup, NodeSelection> nodeGroups = new HashMap<>();
      for (Node<?> node : nodes) {
        if (node instanceof Groupable<?> groupable) {
          for (NodeGroup group : groupable.getGroups()) {
            nodeGroups.computeIfAbsent(group, g -> new NodeSelection()).add(node);
          }
        }
      }
      nodeGroups.keySet().forEach(storage::updateNodeGroup);
      nodes.forEach(DataExporter::updateNode);
      nodeGroups.forEach(storage::assignNodesToGroup);
      nodes.forEach(node -> storage.saveEdges(node.getEdges()));
    };
  }

  private static <N extends Node<N>> void updateNode(Node<N> node) {
    node.getType().updateNode((N) node);
  }

  static DataExporter visualizers(Collection<PathVisualizer<?, ?>> visualizers) {
    return storage -> {
      Collection<PathVisualizer<?, ?>> distinct = new HashSet<>();
      Queue<PathVisualizer<?, ?>> toHandle = new LinkedList<>(visualizers);
      while (!toHandle.isEmpty()) {
        PathVisualizer<?, ?> current = toHandle.poll();
        if (current instanceof CombinedVisualizer combined) {
          toHandle.addAll(combined.getVisualizers());
        }
        distinct.add(current);
      }
      distinct.forEach(DataExporter::updateVisualizer);
    };
  }

  static <T extends PathVisualizer<T, ?>> void updateVisualizer(PathVisualizer<T, ?> visualizer) {
    VisualizerDataStorage<T> storage = visualizer.getType().getStorage();
    if (storage != null) {
      storage.updatePathVisualizer((T) visualizer);
    }
  }

  static DataExporter groupSet(Iterable<NodeGroup> groups) {
    return storage -> {
      groups.forEach(storage::updateNodeGroup);
      groups.forEach(g -> storage.addSearchTerms(g, g.getSearchTermStrings()));
    };
  }

  static DataExporter foundData(Player player) {
    return storage -> {
    };
  }

  void save(DataStorage storage) throws IOException;
}
