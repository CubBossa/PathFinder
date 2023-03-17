package de.cubbossa.pathfinder.module.visualizing;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.modifier.VisualizerModifier;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
public class VisualizerPath<D> extends ArrayList<Node<?>> {

  private final UUID playerUuid;
  private final Collection<SubPath<?>> paths;
  private boolean active;

  public VisualizerPath(UUID playerUuid) {
    this.playerUuid = playerUuid;
    this.paths = new HashSet<>();
    this.active = false;
  }

  public void prepare(List<Node<?>> path, Player player) {
    // build sub paths for every visualizer change
    SortedMap<Node<?>, Collection<PathVisualizer<?, ?>>> nodeVisualizerMap = new TreeMap<>();
    for (Node<?> node : path) {
      if (!(node instanceof Groupable<?> groupable)) {
        // this node cannot be rendered, it cannot be grouped
        continue;
      }
      for (NodeGroup group : groupable.getGroups()) {
        // check if groups apply visualizers
        VisualizerModifier modifier = group.getModifier(VisualizerModifier.class);
        if (modifier == null) {
          // group does not apply visualizer to node
          continue;
        }
        nodeVisualizerMap.computeIfAbsent(node, n -> new HashSet<>()).add(modifier.visualizer());
      }
    }

    PathVisualizer<?, ?> current = null;

    // create SubPaths from map
    while (!nodeVisualizerMap.isEmpty()) {

      // skip all leading nodes that do not have a visualizer
      while (nodeVisualizerMap.get(nodeVisualizerMap.firstKey()).isEmpty()) {
        nodeVisualizerMap.remove(nodeVisualizerMap.firstKey());
      }

      // follow one visualizer along path
      paths.add(new SubPath<>(null, null, null)); //TODO
    }
  }

  public void run() {
    run(playerUuid);
  }

  public void run(UUID uuid) {
    prepare(this, Bukkit.getPlayer(uuid));
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
      cancelSync();
      this.active = true;

      AtomicInteger interval = new AtomicInteger(0);
      for (SubPath<?> path : paths) {
        path.task = Bukkit.getScheduler().runTaskTimer(PathPlugin.getInstance(), () -> {
          play(path, uuid, interval);
        }, 0L, path.visualizer.getInterval());
      }
    });
  }

  private <T> void play(SubPath<T> path, UUID uuid, AtomicInteger interval) {
    Player searching = Bukkit.getPlayer(uuid);
    if (searching == null) {
      return;
    }
    long fullTime = searching.getWorld().getFullTime();

    path.visualizer.play(new PathVisualizer.VisualizerContext<>(Lists.newArrayList(searching),
            interval.getAndIncrement(), fullTime, path.data));
  }

  public void cancel() {
    Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> cancelSync());
  }

  public void cancelSync() {
    paths.forEach(this::cancelSync);
  }

  private <T> void cancelSync(SubPath<T> path) {
    if (path.task == null) {
      return;
    }
    Bukkit.getScheduler().cancelTask(path.task.getTaskId());
    this.active = false;

    Player player = Bukkit.getPlayer(playerUuid);
    path.visualizer.destruct(player, path.data);
  }

  @RequiredArgsConstructor
  @Accessors(fluent = true)
  private static class SubPath<D> {
    private final List<Node<?>> path;
    private final PathVisualizer<?, D> visualizer;
    private final D data;
    private BukkitTask task;
  }
}
