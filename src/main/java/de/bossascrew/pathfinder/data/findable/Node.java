package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.core.util.PluginUtils;
import de.bossascrew.pathfinder.data.DatabaseModel;
import de.bossascrew.pathfinder.data.RoadMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
public class Node extends Findable {

    public static final String SCOPE = "NODE";

    private Vector vector;

    public Node(int databaseId, RoadMap roadMap, String name, Vector vector) {
        super(databaseId, roadMap, name);
        this.vector = vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
        roadMap.updateArmorStandPosition(this);
        roadMap.updateEditModeParticles();
        updateData();
    }

    public Location getLocation() {
        return vector.toLocation(roadMap.getWorld());
    }

    @Override
    public String getScope() {
        return "NODE";
    }

    @Override
    public void updateData() {
        PluginUtils.getInstance().runAsync(() -> DatabaseModel.getInstance().updateFindable(this));
    }
}
