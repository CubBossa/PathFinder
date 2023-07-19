package de.cubbossa.pathfinder.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class BukkitMainThreadExecutor implements Executor {

  private Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

  public BukkitMainThreadExecutor(JavaPlugin plugin) {
    plugin.getServer().getScheduler().runTaskTimer(plugin, this::runTasks, 1, 1);
  }

  @Override
  public void execute(@NotNull Runnable command) {
    tasks.offer(command);
  }

  public void runTasks() {
    Runnable task;
    while ((task = tasks.poll()) != null) {
      task.run();
    }
  }
}
