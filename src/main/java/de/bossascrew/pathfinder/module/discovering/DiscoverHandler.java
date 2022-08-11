package de.bossascrew.pathfinder.module.discovering;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.core.node.Findable;
import de.bossascrew.pathfinder.core.node.Navigable;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.data.FoundInfo;
import de.bossascrew.pathfinder.data.PathPlayer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
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

	public void playDiscovery(UUID playerId, Navigable findable) {
		Player player = Bukkit.getPlayer(playerId);

		PathPlugin.getInstance().getAudiences().player(playerId).showTitle(Title.title(
				Messages.LOCATION_FOUND_TITLE_1.asComponent(player, Placeholder.component("name", Component.text(""))),
				Messages.LOCATION_FOUND_TITLE_2.asComponent(player),
				Title.Times.times(
						Duration.ofSeconds(1),
						Duration.ofSeconds(2),
						Duration.ofSeconds(1)
				)
		));
		player.playSound(player.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1f, 1f);
	}

	public void discover(UUID playerId, Findable findable, boolean group, Date date) {

	}

	public void forget(UUID playerId, Findable findable, boolean deep) {

	}

	public boolean hasDiscovered(UUID playerId, Findable findable) {
		return true;
	}

	public int getDiscoveredCount(PathPlayer pathPlayer, RoadMap roadMap) {
		return roadMap.getFoundFindables(pathPlayer).size();
	}

	public float getDiscoveredPercent(PathPlayer pathPlayer, RoadMap roadMap) {
		return getDiscoveredCount(pathPlayer, roadMap) / (float) roadMap.getFindables().size();
	}
}
