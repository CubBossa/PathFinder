package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.nodegroup.modifier.*;
import de.cubbossa.pathfinder.storage.v3.V3Storage;
import de.cubbossa.pathfinder.util.WorldImpl;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class V3Converter implements Runnable {

  private V3Storage oldStorage;
  private Storage storage;
  private NodeTypeRegistry nodeTypes;
  private VisualizerTypeRegistry visualizerTypes;

  public volatile int progress = 0;
  public volatile int estimate = Integer.MAX_VALUE;

  public V3Converter(V3Storage oldStorage, Storage storage, NodeTypeRegistry nodeTypes, VisualizerTypeRegistry visualizerTypes) {
    this.oldStorage = oldStorage;
    this.storage = storage;
    this.nodeTypes = nodeTypes;
    this.visualizerTypes = visualizerTypes;
  }

  @Override
  public void run() {
    oldStorage.connect();

    Collection<V3Storage.V3RoadMap> v3RoadMaps = oldStorage.loadRoadmaps();
    Collection<V3Storage.V3Node> v3Nodes = oldStorage.loadNodes();
    Collection<V3Storage.V3NodeGroup> v3NodeGroups = oldStorage.loadNodeGroups();
    Collection<V3Storage.V3Discovering> v3Discoverings = oldStorage.loadDiscoverings();
    Collection<V3Storage.V3SearchTerm> v3SearchTerms = oldStorage.loadSearchTerms();
    Collection<V3Storage.V3Edge> v3Edges = oldStorage.loadEdges();
    Collection<V3Storage.V3Visualizer> v3Visualizers = oldStorage.loadVisualizers();
    Collection<V3Storage.V3GroupNode> v3GroupNodes = oldStorage.loadGroupNodes();

    estimate = v3RoadMaps.size() +
        v3Nodes.size() +
        v3NodeGroups.size() +
        v3Discoverings.size() +
        v3SearchTerms.size() +
        v3Edges.size() +
        v3Visualizers.size() +
        v3GroupNodes.size() + 20;
    progress = 10;

    Map<NamespacedKey, NodeGroup> roadMapReplacements = new HashMap<>();

    for (V3Storage.V3RoadMap rm : v3RoadMaps) {
      NodeGroup group = storage.createAndLoadGroup(rm.key()).join();
      group.addModifier(new CommonCurveLengthModifier(rm.curveLength()));
      group.addModifier(new CommonVisualizerModifier(rm.vis()));
      group.setWeight(0);
      storage.saveGroup(group).join();
      roadMapReplacements.put(rm.key(), group);
      progress++;
    }

    Map<Integer, Node> nodesByOldId = new HashMap<>();

    Map<Double, NodeGroup> curveLengthGroups = new HashMap<>();

    Collection<NodeGroup> roadMapsToSave = new HashSet<>();
    for (V3Storage.V3Node node : v3Nodes) {
      Node waypoint = storage.createAndLoadNode(
          nodeTypes.getType(node.type()),
          new Location(node.x(), node.y(), node.z(), new WorldImpl(node.world()))
      ).join();
      nodesByOldId.put(node.id(), waypoint);

      NamespacedKey inGroup = node.roadmap();
      NodeGroup rm = roadMapReplacements.get(inGroup);
      roadMapsToSave.add(rm);
      rm.add(waypoint.getNodeId());

      Double curveLength = node.curveLength();
      if (curveLength != null) {
        NodeGroup curveLenGroup = curveLengthGroups.computeIfAbsent(curveLength, len -> {
          NodeGroup g = storage.createAndLoadGroup(CommonPathFinder.pathfinder("curve_len_" + len.toString()
              .replace(",", "_").replace(".", "_"))).join();

          g.addModifier(new CommonCurveLengthModifier(len));
          return g;
        });
        curveLenGroup.add(waypoint.getNodeId());
        storage.saveGroup(curveLenGroup).join();
      }
      progress++;
    }
    for (NodeGroup group : roadMapsToSave) {
      storage.saveGroup(group).join();
    }

    Collection<Node> nodesToSave = new HashSet<>();
    for (V3Storage.V3Edge edge : v3Edges) {
      Node node = nodesByOldId.get(edge.start());
      if (node == null) {
        continue;
      }
      Node other = nodesByOldId.get(edge.end());
      if (other == null) {
        continue;
      }
      node.connect(other, edge.weight());
      nodesToSave.add(node);
      progress++;
    }
    for (Node node : nodesToSave) {
      storage.saveNode(node).join();
    }

    Map<NamespacedKey, Collection<String>> searchTerms = new HashMap<>();
    for (V3Storage.V3SearchTerm term : v3SearchTerms) {
      searchTerms.computeIfAbsent(term.group(), s -> new HashSet<>()).add(term.key());
      progress++;
    }

    Map<NamespacedKey, NodeGroup> groups = new HashMap<>();
    for (V3Storage.V3NodeGroup group : v3NodeGroups) {
      NodeGroup newGroup;
      try {
        newGroup = storage.createAndLoadGroup(group.key()).join();
      } catch (Throwable t) {
        newGroup = storage.createAndLoadGroup(NamespacedKey.fromString(group.key().toString() + "_group")).join();
      }
      groups.put(group.key(), newGroup);

      if (group.perm() != null) {
        newGroup.addModifier(new CommonPermissionModifier(group.perm()));
      }
      if (group.discoverable()) {
        newGroup.addModifier(new CommonDiscoverableModifier(group.nameFormat()));
      }
      if (group.findDistance() != null) {
        newGroup.addModifier(new CommonFindDistanceModifier(group.findDistance()));
      }
      if (group.navigable() && searchTerms.containsKey(newGroup.getKey())) {
        newGroup.addModifier(new CommonNavigableModifier(searchTerms.get(newGroup.getKey()).toArray(String[]::new)));
      }
      progress++;
    }
    for (V3Storage.V3GroupNode groupNodeMapping : v3GroupNodes) {
      groups.computeIfPresent(groupNodeMapping.group(), (key, group) -> {
        var node = nodesByOldId.get(groupNodeMapping.id());
        group.add(node.getNodeId());
        return group;
      });
      progress++;
    }
    for (NodeGroup group : groups.values()) {
      storage.saveGroup(group).join();
    }

    for (V3Storage.V3Discovering discovering : v3Discoverings) {
      storage.createAndLoadDiscoverinfo(
          discovering.player(), discovering.group(), discovering.time()
      ).join();
      progress++;
    }

    for (V3Storage.V3Visualizer vis : v3Visualizers) {
      VisualizerType<PathVisualizer<?, ?>> type = visualizerTypes.getType(vis.type()).orElseThrow();

      PathVisualizer<?, ?> visualizer = storage.createAndLoadVisualizer(type, vis.key()).join();
      if (vis.permission() != null) {
        visualizer.setPermission(vis.permission());
      }
      type.deserialize(visualizer, YamlConfiguration.loadConfiguration(new StringReader(vis.cfg())).getValues(false));
      progress++;
    }

    oldStorage.disconnect();
    progress += 10;
  }
}
