package de.cubbossa.pathfinder.event;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerPath;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.Consumer;

public interface EventDispatcher<PlayerT> extends Disposable {

  void dispatchReloadEvent(boolean config, boolean locale);

  <N extends Node> void dispatchNodeCreate(N node);

  void dispatchNodeSave(Node node);

  void dispatchNodeLoad(Node node);

  <N extends Node> void dispatchNodeDelete(N node);

  void dispatchNodesDelete(Collection<Node> nodes);

  void dispatchNodeUnassign(Node node, Collection<NodeGroup> groups);

  void dispatchNodeAssign(Node node, Collection<NodeGroup> groups);

  void dispatchGroupCreate(NodeGroup group);

  void dispatchGroupDelete(NodeGroup group);

  void dispatchGroupSave(NodeGroup group);

  boolean dispatchPlayerFindEvent(PathPlayer<PlayerT> player, NodeGroup group, DiscoverableModifier modifier, LocalDateTime findDate);

  boolean dispatchPlayerFindProgressEvent(PathPlayer<PlayerT> player, NodeGroup found, NodeGroup observer);

  boolean dispatchPlayerForgetEvent(PathPlayer<PlayerT> player, NamespacedKey group);

  boolean dispatchVisualizerChangeEvent(PathVisualizer<?, ?> visualizer);

  boolean dispatchPathStart(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path);

  boolean dispatchPathTargetReached(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path);

  void dispatchPathStopped(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path);

  boolean dispatchPathCancel(PathPlayer<PlayerT> player, VisualizerPath<PlayerT> path);

  <E extends PathFinderEvent> Listener<E> listen(Class<E> eventType, Consumer<? super E> event);

  default <E extends PathFinderEvent> Listener<E> listen(Disposable owner, Class<E> eventType, Consumer<? super E> event) {
    var listener = listen(eventType, event);
    PathFinder.get().getDisposer().register(owner, listener);
    return listener;
  }

  void drop(Listener<?> listener);
}
