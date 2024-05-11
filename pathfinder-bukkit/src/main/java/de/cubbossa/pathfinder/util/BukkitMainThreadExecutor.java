package de.cubbossa.pathfinder.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BukkitMainThreadExecutor implements Executor {

  private final double MAX_MILLIS_PER_TICK;
  private final int MAX_NANOS_PER_TICK;

  private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

  public BukkitMainThreadExecutor(JavaPlugin plugin) {
    this(plugin, 50);
  }

  public BukkitMainThreadExecutor(JavaPlugin plugin, double maxMillis) {
    this.MAX_MILLIS_PER_TICK = maxMillis;
    this.MAX_NANOS_PER_TICK = (int) (MAX_MILLIS_PER_TICK * 1E6);
    plugin.getServer().getScheduler().runTaskTimer(plugin, this::runTasks, 1, 1);
  }

  @Override
  public void execute(@NotNull Runnable command) {
    tasks.offer(command);
  }

  public void runTasks() {
    long stopTime = System.nanoTime() + MAX_NANOS_PER_TICK;
    Runnable task;
    while (System.nanoTime() < stopTime && (task = tasks.poll()) != null) {
      task.run();
    }
  }
}
