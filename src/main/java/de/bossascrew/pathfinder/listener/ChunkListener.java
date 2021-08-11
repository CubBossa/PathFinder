package de.bossascrew.pathfinder.listener;

import de.bossascrew.pathfinder.data.RoadMap;
import de.bossascrew.pathfinder.handler.RoadMapHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

	@EventHandler
	public void onLoad(ChunkLoadEvent event) {
		for (RoadMap roadMap : RoadMapHandler.getInstance().getRoadMaps(event.getWorld())) {
			roadMap.updateChunkArmorStands(event.getChunk(), false);
		}
	}

	@EventHandler
	public void onUnload(ChunkUnloadEvent event) {
		for (RoadMap roadMap : RoadMapHandler.getInstance().getRoadMaps(event.getWorld())) {
			roadMap.updateChunkArmorStands(event.getChunk(), true);
		}
	}
}
