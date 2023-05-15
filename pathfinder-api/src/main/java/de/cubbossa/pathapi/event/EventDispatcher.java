package de.cubbossa.pathapi.event;

import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerPath;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.Consumer;

public interface EventDispatcher<PlayerT> {

  <N extends Node> void dispatchNodeCreate(N node);

  void dispatchNodeSave(Node node);

  void dispatchNodeLoad(Node node);

  <N extends Node> void dispatchNodeDelete(N node);

  void dispatchNodesDelete(Collection<Node> nodes);

  void dispatchNodeUnassign(Node node, Collection<NodeGroup> groups);

  void dispatchNodeAssign(Node node, Collection<NodeGroup> groups);

  void dispatchGroupCreate(NodeGroup group);

  void dispatchGroupDelete(NodeGroup group);

  boolean dispatchPlayerFindEvent(PathPlayer<PlayerT> player, NodeGroup group, DiscoverableModifier modifier, LocalDateTime findDate);

  boolean dispatchPlayerForgetEvent(PathPlayer<PlayerT> player, NamespacedKey group, LocalDateTime foundDate);

  boolean dispatchVisualizerChangeEvent(PathVisualizer<?, ?> visualizer);

  boolean dispatchPathStart(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path, Location target, float findDistanceRadius);

  boolean dispatchPathTargetReached(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path);

  void dispatchPathStopped(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path, Location target, float distance);

  boolean dispatchPathCancel(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path, Location target, float distance);

  <E extends PathFinderEvent> Listener<E> listen(Class<E> eventType, Consumer<? super E> event);

  void drop(Listener<?> listener);
}
