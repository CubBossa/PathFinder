package de.cubbossa.pathfinder.visualizer.impl;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.PathFinderPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public abstract class IntervalVisualizer<ViewT extends IntervalVisualizer<ViewT>.IntervalView> extends BukkitVisualizer<ViewT> {

  public static final Property<IntervalVisualizer<?>, Integer> PROP_INTERVAL = new SimpleProperty<>(
      "interval", Integer.class, IntervalVisualizer::getInterval, IntervalVisualizer::setInterval
  );

  private int interval;

  public IntervalVisualizer(NamespacedKey key) {
    super(key);
  }

  public abstract class IntervalView extends BukkitVisualizer<ViewT>.BukkitView {

    private BukkitTask task;

    public IntervalView() {
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

    abstract void play(int interval);
  }
}
