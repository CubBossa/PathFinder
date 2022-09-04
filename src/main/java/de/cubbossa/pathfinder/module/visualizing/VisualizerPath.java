package de.cubbossa.pathfinder.module.visualizing;

import com.google.common.collect.Lists;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class VisualizerPath<D> extends ArrayList<Node> {

    private final UUID playerUuid;
    private final PathVisualizer<?, D> visualizer;
    private D visualizerData;

    private boolean active;
    private BukkitTask task;

    public VisualizerPath(UUID playerUuid, PathVisualizer<?, D> visualizer) {
        this.playerUuid = playerUuid;
        this.active = false;
        this.visualizer = visualizer;
    }

    public void prepare(List<Node> path) {
        visualizerData = visualizer.prepare(path);
    }

    public void run() {
        run(playerUuid);
    }

    public void run(UUID uuid) {
        prepare(this);
        Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
            cancelSync();
            this.active = true;

            AtomicInteger interval = new AtomicInteger(0);
            task = Bukkit.getScheduler().runTaskTimer(PathPlugin.getInstance(), () -> {
                Player searching = Bukkit.getPlayer(uuid);
                if (searching == null) {
                    return;
                }
                long fullTime = searching.getWorld().getFullTime(); //TODO global time parameter?

                visualizer.play(new PathVisualizer.VisualizerContext<D>(Lists.newArrayList(searching), interval.getAndIncrement(), fullTime, visualizerData));
            }, 0L, visualizer.getInterval());
        });
    }

    public void cancel() {
        Bukkit.getScheduler().runTask(PathPlugin.getInstance(), this::cancelSync);
    }

    public void cancelSync() {
        if(task == null) {
            return;
        }
        Bukkit.getScheduler().cancelTask(task.getTaskId());
        this.active = false;
    }
}
