package de.bossascrew.pathfinder.old.listener;

import de.bossascrew.pathfinder.old.RoadMap;
import de.bossascrew.pathfinder.old.visualization.VisualizerEditMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import de.tr7zw.nbtapi.NBTItem;

public class EntityDamageListener implements Listener {

    @EventHandler
    public void onArmorstandDestroy(EntityDamageEvent e) {
        if (e == null) {
            return;
        }
        if (e.getEntityType() == EntityType.ARMOR_STAND) {
            ArmorStand as = (ArmorStand) e.getEntity();
            if (as.getName().equalsIgnoreCase(VisualizerEditMode.WAYPOINT_NAME)) {
                NBTItem i = new NBTItem(as.getEquipment().getHelmet());
                RoadMap rm = RoadMap.getRoadMap(i.getString(VisualizerEditMode.ROADMAP_KEY));
                if (rm == null) {
                    return;
                }

                if (!rm.getEditMode().isEmpty()) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
