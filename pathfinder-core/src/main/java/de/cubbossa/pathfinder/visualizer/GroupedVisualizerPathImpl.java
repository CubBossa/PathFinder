package de.cubbossa.pathfinder.visualizer;

import com.google.common.util.concurrent.AtomicDouble;
import de.cubbossa.pathfinder.group.VisualizerModifier;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.Route;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Common implementation of the {@link VisualizerPath} interface.
 *
 * @param <PlayerT> The abstract path player type.
 */
public class GroupedVisualizerPathImpl<PlayerT> extends AbstractVisualizerPath<PlayerT> {

  protected final Route route;
  protected final Collection<SubPath<?>> paths;

  public GroupedVisualizerPathImpl(PathPlayer<PlayerT> target, Route route) {
    this.route = route;
    this.paths = new HashSet<>();
    setTargetViewer(target);
    update();
  }

  @Override
  public void update() {

    // build sub paths for every visualizer change
    LinkedHashMap<Node, Collection<PathVisualizer<?, PlayerT>>> nodeVisualizerMap = new LinkedHashMap<>();
    for (Node ungrouped : route.calculateNodes()) {
      if (ungrouped instanceof GroupedNode node) {
        AtomicDouble highest = new AtomicDouble();
        node.groups().stream()
            .filter(g -> g.hasModifier(VisualizerModifier.KEY))
            .peek(group -> {
              if (highest.get() < group.getWeight()) {
                highest.set(group.getWeight());
              }
            })
            .filter(g -> g.getWeight() == highest.get())
            .sorted()
            .map(g -> g.<VisualizerModifier>getModifier(VisualizerModifier.KEY))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(VisualizerModifier::getVisualizer)
            .map(CompletableFuture::join)
            .filter(Optional::isPresent).map(Optional::get)
            .forEach(vis -> {
              nodeVisualizerMap.computeIfAbsent(node.node(), n -> new HashSet<>()).add((PathVisualizer<?, PlayerT>) vis);
            });
      }
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
      injectSubPathView(subPath);
    }
  }

  private <ViewT extends PathView<PlayerT>> void injectSubPathView(SubPath<ViewT> subPath) {
    subPath.data = subPath.visualizer.createView(subPath.path, getTargetViewer());
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
  protected class SubPath<ViewT extends PathView<PlayerT>> {
    protected final List<Node> path;
    protected final PathVisualizer<ViewT, PlayerT> visualizer;
    protected ViewT data;
  }
}
