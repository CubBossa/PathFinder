package de.bossascrew.pathfinder.visualizer;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.handler.PathPlayerHandler;
import de.bossascrew.pathfinder.node.Node;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.splinelib.util.Spline;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class ParticlePath extends ArrayList<Node> {

    private final RoadMap roadMap;
    private final UUID playerUuid;
    private final PathVisualizer visualizer;

    private boolean active;
    private BukkitTask task;

    private double cachedDistance = -1;
    private List<Location> calculatedPoints;

    public ParticlePath(RoadMap roadMap, UUID playerUuid, SimpleCurveVisualizer visualizer) {
        this.roadMap = roadMap;
        this.playerUuid = playerUuid;
        this.active = false;
        this.visualizer = visualizer;
        this.calculatedPoints = new ArrayList<>();
    }

    public void prepare(List<Node> path) {
        calculatedPoints.clear();

        Spline spline = visualizer.makeSpline(path);
        List<Vector> curve = visualizer.interpolate(spline);
        calculatedPoints.addAll(visualizer.transform(curve).stream().map(vector -> vector.toLocation(roadMap.getWorld())).collect(Collectors.toList()));
    }

    public void run() {
        run(playerUuid);
    }

    public void run(UUID uuid) {
        Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
            cancelSync();
            PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(uuid);
            if (pathPlayer == null) {
                return;
            }
            this.active = true;

            World world = roadMap.getWorld();

            task = Bukkit.getScheduler().runTaskTimer(PathPlugin.getInstance(), () -> {
                Player searching = Bukkit.getPlayer(uuid);
                if (searching == null) {
                    return;
                }
                calculatedPoints.forEach(location -> visualizer.playParticle(searching, location, , ));
            }, 0L, visualizer.getTickDelay());
        });
    }

    public void cancel() {
        Bukkit.getScheduler().runTask(PathPlugin.getInstance(), this::cancelSync);
    }

    /**
     * Nur im Mainthread aufrufen
     */
    public void cancelSync() {
        List<SchedulerHandler> handlers = new ArrayList<>(schedulerHandlers);
        for (SchedulerHandler handler : handlers) {
            handler.setCancelled(true);
            for (int i : handler.getSchedulerIds()) {
                Bukkit.getScheduler().cancelTask(i);
            }
            schedulerHandlers.remove(handler);
        }
        this.active = false;
    }
}
