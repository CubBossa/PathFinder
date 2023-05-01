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

  private final PathPlayer<PlayerT> player;
  private final Collection<SubPath<?, PlayerT>> paths;
  private boolean active;

  public VisualizerPath(PathPlayer<PlayerT> player) {
    this.player = player;
    this.paths = new HashSet<>();
    this.active = false;
  }

  public void prepare(List<Node> path, PathPlayer<PlayerT> player) {
    // build sub paths for every visualizer change
    SortedMap<Node, Collection<PathVisualizer<?, ?>>> nodeVisualizerMap = new TreeMap<>();
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
      nodeVisualizerMap.computeIfAbsent(node, n -> new HashSet<>()).add(mod.visualizer());
    }

    // create SubPaths from map
    while (!nodeVisualizerMap.isEmpty()) {

      // skip all leading nodes that do not have a visualizer
      while (nodeVisualizerMap.get(nodeVisualizerMap.firstKey()).isEmpty()) {
        nodeVisualizerMap.remove(nodeVisualizerMap.firstKey());
      }

      // follow one visualizer along path
      paths.add(new SubPath<>(null, null, null)); //TODO
    }
    for (SubPath<?, PlayerT> subPath : paths) {
      subPath.visualizer.prepare(subPath.path, player);
    }
  }

  public void run() {
    run(player);
  }

  public void run(PathPlayer<PlayerT> player) {
      prepare(this, player);
      cancel();
      this.active = true;

      AtomicInteger interval = new AtomicInteger(0);
      for (SubPath<?, PlayerT> path : paths) {
          path.task = runTask(() -> {
              play(path, player, interval);
          }, 0L, path.visualizer.getInterval());
      }
  }

  private <DataT> void play(SubPath<DataT, PlayerT> path, PathPlayer<PlayerT> player,
                            AtomicInteger interval) {
    long fullTime = 0; //TODO player..getWorld().getFullTime();
    path.visualizer.play(new PathVisualizer.VisualizerContext<>(Lists.newArrayList(player),
        interval.getAndIncrement(), fullTime, path.data));
  }

  public void cancel() {
      paths.forEach(this::cancel);
  }

    private <DataT> void cancel(SubPath<DataT, PlayerT> path) {
        if (path.task == null) {
            return;
        }
        cancelTask(path.task);
        this.active = false;

        path.visualizer.destruct(player, path.data);
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true)
    private static class SubPath<DataT, PlayerT> {
        private final List<Node> path;
        private final PathVisualizer<DataT, PlayerT> visualizer;
        private final DataT data;
        private Task task;
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
