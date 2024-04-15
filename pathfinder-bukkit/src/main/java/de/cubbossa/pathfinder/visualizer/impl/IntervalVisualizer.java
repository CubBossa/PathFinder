package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.node.Node;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Setter
public abstract class IntervalVisualizer<ViewT extends IntervalVisualizer<ViewT>.IntervalView> extends BukkitVisualizer<ViewT> {

  public static final Property<IntervalVisualizer<?>, Integer> PROP_INTERVAL = new PropertyImpl<>(
      "interval", Integer.class, IntervalVisualizer::getInterval, IntervalVisualizer::setInterval
  );

  private int interval = 10;

  public IntervalVisualizer(NamespacedKey key) {
    super(key);
  }

  public abstract class IntervalView extends BukkitVisualizer<ViewT>.BukkitView {

    private BukkitTask task;

    public IntervalView(PathPlayer<Player> player, List<Node> path) {
      super(player, path);
      start();
    }

    void start() {
      stop();
      AtomicInteger i = new AtomicInteger(0);
      task = Bukkit.getScheduler().runTaskTimer(PathFinderPlugin.getInstance(), () -> {
        play(i.getAndIncrement());
      }, 0, interval);
    }

    void stop() {
      if (task != null) {
        task.cancel();
      }
    }

    @Override
    public void removeViewer(PathPlayer<Player> player) {
      super.removeViewer(player);
      if (getViewers().size() == 0) {
        stop();
      }
    }

    @Override
    public void removeAllViewers() {
      super.removeAllViewers();
      stop();
    }

    abstract void play(int interval);
  }
}
