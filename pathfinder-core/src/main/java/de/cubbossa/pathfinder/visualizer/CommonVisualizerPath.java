package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.group.VisualizerModifier;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.Task;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathView;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * Common implementation of the {@link VisualizerPath} interface.
 *
 * @param <PlayerT> The abstract path player type.
 */
@Getter
@Setter
public class CommonVisualizerPath<PlayerT> implements VisualizerPath<PlayerT> {

  protected PathPlayer<PlayerT> targetViewer;
  protected final HashSet<PathPlayer<PlayerT>> viewers;
  protected final Collection<SubPath<?>> paths;

  public CommonVisualizerPath(List<Node> path, PathPlayer<PlayerT> player) {
    this.viewers = new HashSet<>();
    this.paths = new HashSet<>();
    targetViewer = player;
    this.viewers.add(targetViewer);

    // build sub paths for every visualizer change
    LinkedHashMap<Node, Collection<PathVisualizer<?, PlayerT>>> nodeVisualizerMap = new LinkedHashMap<>();
    for (Node node : path) {
      if (!(node instanceof Groupable groupable)) {
        // this node cannot be rendered, it cannot be grouped
        continue;
      }
      groupable.getGroups().stream()
          .filter(g -> g.hasModifier(VisualizerModifier.KEY))
          .sorted()
          .map(g -> g.<VisualizerModifier>getModifier(VisualizerModifier.KEY))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(vis -> {
            nodeVisualizerMap.computeIfAbsent(node, n -> new HashSet<>()).add((PathVisualizer<?, PlayerT>) vis.visualizer());
          });
    }


    // reduce collection with each iteration until no subpaths are left
    while (!nodeVisualizerMap.isEmpty()) {

      // Choose random Visualizer from first node and create subpath by adding each next node until it does not have
      // the visualizer applied.

      int index = 0;
      PathVisualizer<?, PlayerT> selection = null;
      // holds the current visualizer selection path
      List<Node> subPathCollection = new ArrayList<>();

      // Start iterating from first node and break, as soon as current does not contain selected visualizer
      while (index < nodeVisualizerMap.size()) {
        Node current = nodeVisualizerMap.keySet().toArray(Node[]::new)[index];
        Collection<PathVisualizer<?, PlayerT>> present = nodeVisualizerMap.get(current);

        if (selection == null) {
          // First iteration, select one random visualizer
          selection = present.iterator().next();
        } else if (!present.contains(selection)) {
          // Visualizer is not null -> we had another node before, but the current node does not contain
          // our selected visualizer -> end of path.
          // break, save and start new one.
          break;
        }
        // our node will be added to the path, therefore remove the visualizer
        present.remove(selection);
        subPathCollection.add(current);
        index++;
      }

      // store sub path with given visualizer
      paths.add(new SubPath<>(new ArrayList<>(subPathCollection), selection));
      // prepare restart from first node by resetting
      subPathCollection.clear();

      // skip all leading nodes that do not have a visualizer
      while (!nodeVisualizerMap.isEmpty() && nodeVisualizerMap.get(nodeVisualizerMap.keySet().iterator().next()).isEmpty()) {
        nodeVisualizerMap.remove(nodeVisualizerMap.keySet().iterator().next());
      }
    }
    for (SubPath<?> subPath : paths) {
      injectSubPathView(subPath, player);
    }
  }

  private <ViewT extends PathView<PlayerT>> void injectSubPathView(SubPath<ViewT> subPath, PathPlayer<PlayerT> player) {
    subPath.data = subPath.visualizer.createView(subPath.path, player);
  }

  @Override
  public boolean isActive() {
    return viewers.size() > 0;
  }

  @Override
  public boolean isActive(PathPlayer<PlayerT> player) {
    return viewers.contains(player);
  }

  @Override
  public void addViewer(PathPlayer<PlayerT> player) {
    viewers.add(player);
    paths.forEach(subPath -> subPath.data.addViewer(player));
  }

  @Override
  public void removeViewer(PathPlayer<PlayerT> player) {
    viewers.remove(player);
    paths.forEach(subPath -> subPath.data.removeViewer(player));
  }

  @Override
  public void removeAllViewers() {
    getViewers().forEach(this::removeViewer);
  }

  @RequiredArgsConstructor
  @Accessors(fluent = true)
  class SubPath<ViewT extends PathView<PlayerT>> {
    protected final List<Node> path;
    protected final PathVisualizer<ViewT, PlayerT> visualizer;
    protected ViewT data;
    protected Task task;
  }
}