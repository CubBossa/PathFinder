package de.cubbossa.pathfinder.visualizer;

import com.google.common.collect.Lists;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Getter
public class VisualizerPath<P> extends ArrayList<Node<?>> {

  private final PathPlayer<P> player;
  private final Collection<SubPath<?, P>> paths;
  private boolean active;

  public VisualizerPath(PathPlayer<P> player) {
    this.player = player;
    this.paths = new HashSet<>();
    this.active = false;
  }

  public void prepare(List<Node<?>> path, PathPlayer<P> player) {
    // build sub paths for every visualizer change
    SortedMap<Node<?>, Collection<PathVisualizer<?, ?, ?>>> nodeVisualizerMap = new TreeMap<>();
    for (Node<?> node : path) {
      if (!(node instanceof Groupable<?> groupable)) {
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
    for (SubPath<?, P> subPath : paths) {
      subPath.visualizer.prepare(subPath.path, player);
    }
  }

  public void run() {
    run(player);
  }

  public void run(PathPlayer<P> player) {
    prepare(this, player);
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
      cancelSync();
      this.active = true;

      AtomicInteger interval = new AtomicInteger(0);
      for (SubPath<?, P> path : paths) {
        path.task = Bukkit.getScheduler().runTaskTimer(PathPlugin.getInstance(), () -> {
          play(path, player, interval);
        }, 0L, path.visualizer.getInterval());
      }
    });
  }

  private <T> void play(SubPath<T, P> path, PathPlayer<P> player, AtomicInteger interval) {
    long fullTime = 0; //TODO player..getWorld().getFullTime();
    path.visualizer.play(new PathVisualizer.VisualizerContext<>(Lists.newArrayList(player),
        interval.getAndIncrement(), fullTime, path.data));
  }

  public void cancel() {
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> cancelSync());
  }

  public void cancelSync() {
    paths.forEach(this::cancelSync);
  }

  private <T> void cancelSync(SubPath<T, P> path) {
    if (path.task == null) {
      return;
    }
    Bukkit.getScheduler().cancelTask(path.task.getTaskId());
    this.active = false;

    path.visualizer.destruct(player, path.data);
  }

  @RequiredArgsConstructor
  @Accessors(fluent = true)
  private static class SubPath<D, P> {
    private final List<Node<?>> path;
    private final PathVisualizer<?, D, P> visualizer;
    private final D data;
    private BukkitTask task;
  }
}
