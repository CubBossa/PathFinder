package de.cubbossa.pathfinder.visualizer;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class VisualizerPath<PlayerT> extends ArrayList<Node> {

  protected final PathPlayer<PlayerT> player;
  protected final Collection<SubPath<?>> paths;
  protected boolean active;

  public VisualizerPath(PathPlayer<PlayerT> player) {
    this.player = player;
    this.paths = new HashSet<>();
    this.active = false;
  }

  public void prepare(List<Node> path) {
    // build sub paths for every visualizer change
    SortedMap<Node, Collection<PathVisualizer<?, PlayerT>>> nodeVisualizerMap = new TreeMap<>();
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

    // holds the current visualizer selection path
    List<Node> subPathCollection = new ArrayList<>();

    // reduce collection with each iteration until no subpaths are left
    while (!nodeVisualizerMap.isEmpty()) {

      // Choose random Visualizer from first node and create subpath by adding each next node until it does not have
      // the visualizer applied.

      int index = 0;
      PathVisualizer<?, PlayerT> selection = null;

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

      // follow one visualizer along path
      paths.add(new SubPath<>(subPathCollection, selection));
      subPathCollection.clear();

      // skip all leading nodes that do not have a visualizer
      while (!nodeVisualizerMap.isEmpty() && nodeVisualizerMap.get(nodeVisualizerMap.firstKey()).isEmpty()) {
        nodeVisualizerMap.remove(nodeVisualizerMap.firstKey());
      }
    }
    for (SubPath<?> subPath : paths) {
      prepareSubPath(subPath);
    }
  }

  private <DataT> void prepareSubPath(SubPath<DataT> subPath) {
    subPath.data = subPath.visualizer.prepare(subPath.path, player);
  }

  public void run() {
    prepare(this);
    cancel();
    this.active = true;

    AtomicInteger interval = new AtomicInteger(0);
    for (SubPath<?> path : paths) {
      path.task = runTask(() -> {
        play(path, player, interval);
      }, 0L, path.visualizer.getInterval());
    }
  }

  private <DataT> void play(SubPath<DataT> path, PathPlayer<PlayerT> player,
                            AtomicInteger interval) {
    long fullTime = 0; //TODO player..getWorld().getFullTime();
    path.visualizer.play(new PathVisualizer.VisualizerContext<>(Lists.newArrayList(player),
        interval.getAndIncrement(), fullTime, path.data));
  }

  public void cancel() {
    paths.forEach(this::cancel);
  }

  private <DataT> void cancel(SubPath<DataT> path) {
    if (path.task == null) {
      return;
    }
    cancelTask(path.task);
    this.active = false;

    path.visualizer.destruct(player, path.data);
  }

  @RequiredArgsConstructor
  @Accessors(fluent = true)
  public class SubPath<DataT> {
    protected final List<Node> path;
    protected final PathVisualizer<DataT, PlayerT> visualizer;
    protected DataT data;
    protected Task task;
  }

  public Task runTask(Runnable task, long delay, long interval) {
    int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(PathFinderPlugin.getInstance(), task, delay, interval);
    return new BukkitTask(id);
  }

  public void cancelTask(Task task) {
    if (task instanceof BukkitTask b) {
      Bukkit.getScheduler().cancelTask(b.id());
    }
  }

  interface Task {
  }

  record BukkitTask(int id) implements Task {
  }
}
