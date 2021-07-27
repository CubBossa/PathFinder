package de.bossascrew.pathfinder.util.hooks;

import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

public class CitizensHook {

	@Getter
	private static CitizensHook instance;

	public CitizensHook() {
		instance = this;
	}

	public Integer getNpcID(Entity entity) {
		NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
		if(npc == null) {
			return null;
		}
		return npc.getId();
	}
}
