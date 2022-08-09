package de.bossascrew.pathfinder.module.discovering;

import de.bossascrew.pathfinder.data.FoundInfo;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.core.node.Findable;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import org.bukkit.NamespacedKey;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscoverHandler {

	@Getter
	private static DiscoverHandler instance;

	private final Map<UUID, Map<NamespacedKey, FoundInfo>> foundFindables;

	public DiscoverHandler() {
		instance = this;

		foundFindables = new HashMap<>();
	}

	public void find(UUID playerId, Findable findable, boolean group, Date date) {

	}

	public void forget(UUID playerId, Findable findable, boolean deep) {

	}

	public boolean hasFound(UUID playerId, Findable findable) {
		return true;
	}

	/**
	 * Wie viele FoundInfo Objekte der Spieler zu einer Roadmap hat
	 */
	public int getFoundAmount(PathPlayer pathPlayer, RoadMap roadMap) {
		return roadMap.getFoundFindables(pathPlayer).size();
	}
}
