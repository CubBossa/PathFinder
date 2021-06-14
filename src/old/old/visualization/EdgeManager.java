package de.bossascrew.pathfinder.old.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import main.de.bossascrew.pathfinder.PathSystem;

public class EdgeManager {

    private boolean running;
    private int taskID;

    private List<EdgeCreator> edgeCreators;

    public EdgeManager() {
        edgeCreators = new ArrayList<EdgeCreator>();
    }

    public EdgeCreator getCreator(UUID uuid) {
        for (EdgeCreator ec : edgeCreators) {
            if (ec.p.getUniqueId().equals(uuid)) {
                return ec;
            }
        }
        return null;
    }

    public void removeCreator(UUID uuid) {
        EdgeCreator toBeRemoved = null;
        for (EdgeCreator ec : edgeCreators) {
            if (ec.p.getUniqueId().equals(uuid)) {
                toBeRemoved = ec;
            }
        }
        edgeCreators.remove(toBeRemoved);
        if (edgeCreators.size() < 1) {
            Bukkit.getScheduler().cancelTask(taskID);
            running = false;
        }
    }

    public void setCreator(UUID uuid, EdgeCreator ec) {
        List<EdgeCreator> removers = new ArrayList<EdgeCreator>();
        for (EdgeCreator ecc : edgeCreators) {
            if (ec.p.getUniqueId().equals(uuid)) {
                removers.add(ecc);
            }
        }
        for (EdgeCreator removed : removers) {
            edgeCreators.remove(removed);
        }
        edgeCreators.add(ec);
        if (!running) {
            startScheduler();
        }
    }

    private void startScheduler() {
        running = true;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PathSystem.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (EdgeCreator ec : edgeCreators) {
                    ec.playLine();
                }
            }
        }, 0, 5);
    }
}
