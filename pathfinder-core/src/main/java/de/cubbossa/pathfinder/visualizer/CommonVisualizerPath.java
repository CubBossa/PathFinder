package de.cubbossa.pathfinder.visualizer;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerPath;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;

/**
 * Common implementation of the {@link VisualizerPath} interface.
 *
 * @param <PlayerT> The abstract path player type.
 */
@Getter
public class CommonVisualizerPath<PlayerT> extends ArrayList<Node> implements VisualizerPath<PlayerT> {

  protected PathPlayer<PlayerT> renderingTarget;
  protected HashSet<PathPlayer<PlayerT>> viewers;
  protected final Collection<SubPath<?>> paths;

  public CommonVisualizerPath() {
    this.viewers = new HashSet<>();
    this.paths = new HashSet<>();
  }

  @Override
  public void prepare(List<Node> path, PathPlayer<PlayerT> player) {
    cancelAll();

    // build sub paths for every visualizer change
    LinkedHashMap<Node, Collection<PathVisualizer<?, PlayerT>>> nodeVisualizerMap = new LinkedHashMap<>();
    for (Node node : path) {
      if (!(node instanceof Groupable groupable)) {
        // this node cannot be rendered, it cannot be grouped
        continue;
      }
      VisualizerModifier mod = groupable.getGroups().stream()
          .filter(g -> g.hasModifier(VisualizerModifier.class))
          .sorted()
          .map(g -> g.getModifier(VisualizerModifier.class))
          .findFirst().orElse(null);
      if (mod == null) {
        continue;
      }
      nodeVisualizerMap.computeIfAbsent(node, n -> new HashSet<>()).add((PathVisualizer<?, PlayerT>) mod.visualizer());
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
      prepareSubPath(subPath, player);
    }
  }

  private <DataT> void prepareSubPath(SubPath<DataT> subPath, PathPlayer<PlayerT> player) {
    subPath.data = subPath.visualizer.prepare(subPath.path, player);
  }

  @Override
  public void run(PathPlayer<PlayerT> player) {
    if (renderingTarget == null) {
      throw new IllegalStateException("A visualizer path must not be run before preparing its caches.");
    }
    AtomicInteger interval = new AtomicInteger(0);
    for (SubPath<?> path : paths) {
      path.task = runTask(() -> {
        play(path, player, interval);
      }, 0L, path.visualizer.getInterval());
    }
  }

  private <DataT> void play(SubPath<DataT> path, PathPlayer<PlayerT> player, AtomicInteger interval) {
    long fullTime = 0; //TODO player..getWorld().getFullTime();
    path.visualizer.play(new PathVisualizer.VisualizerContext<>(Lists.newArrayList(player),
        interval.getAndIncrement(), fullTime, path.data));
  }

  private void cancelAll() {
    getViewers().forEach(this::cancel);
  }

  @Override
  public void cancel(PathPlayer<PlayerT> player) {
    paths.forEach(subPath -> cancel(player, subPath));
  }

  private <DataT> void cancel(PathPlayer<PlayerT> player, SubPath<DataT> path) {
    if (path.task == null) {
      return;
    }
    cancelTask(path.task);

    path.visualizer.destruct(player, path.data);
  }

  @Override
  public boolean isActive() {
    return viewers.size() > 0;
  }

  @Override
  public boolean isActive(PathPlayer<PlayerT> player) {
    return viewers.contains(player);
  }

  @RequiredArgsConstructor
  @Accessors(fluent = true)
  class SubPath<DataT> {
    protected final List<Node> path;
    protected final PathVisualizer<DataT, PlayerT> visualizer;
    protected DataT data;
    protected Task task;
  }

  Task runTask(Runnable task, long delay, long interval) {
    int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(PathFinderPlugin.getInstance(), task, delay, interval);
    return new BukkitTask(id);
  }

  void cancelTask(Task task) {
    if (task instanceof BukkitTask b) {
      Bukkit.getScheduler().cancelTask(b.id());
    }
  }

  interface Task {
  }

  record BukkitTask(int id) implements Task {
  }
}
