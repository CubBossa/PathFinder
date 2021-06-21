package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.data.visualisation.PathVisualizer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.task.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Enthält alle wichtigen Informationen zum Anzeigen des Pfades gecached und läuft die Repeating Tasks
 */
@Getter
public class ParticlePath extends ArrayList<Vector> {

    private final RoadMap roadMap;
    @Setter
    private boolean active;
    @Setter
    private PathVisualizer visualizer;

    private int steps;
    private final List<Integer> taskIds;

    public ParticlePath(RoadMap roadMap) {
        this.roadMap = roadMap;
        this.active = false;
        this.steps = steps;
        this.taskIds = new ArrayList<>();
    }

    public void run(UUID uuid) {
        if (active) {
            return;
        }
        this.active = true;

        int steps = visualizer.getParticleSteps();
        ParticleEffect effect = ParticleEffect.valueOf(visualizer.getParticle().name());
        int period = visualizer.getSchedulerPeriod();
        World world = roadMap.getWorld();

        for(int i = 0; i < steps; i++) {
            final int fi = i;
            List<Object> packets = new ArrayList<>();
            ParticleBuilder particle = new ParticleBuilder(effect);
            for (Vector vector : this) {
                packets.add(particle.setLocation(vector.toLocation(world)).toPacket());
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(PathPlugin.getInstance(), () -> {
                taskIds.add(TaskManager.startSingularTask(packets, 5, uuid));
            }, (long) fi * period);
        }
    }

    public void cancel() {
        if (!active) {
            return;
        }
        this.active = false;

        for(int taskId : taskIds) {
            TaskManager.getTaskManager().stopTask(taskId);
        }
        taskIds.clear();
    }
}
