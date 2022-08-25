package de.bossascrew.pathfinder.module.discovering;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.core.node.Discoverable;
import de.bossascrew.pathfinder.core.node.Navigable;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.data.DiscoverInfo;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class DiscoverHandler {

	@Getter
	private static DiscoverHandler instance;


	private final Map<UUID, Map<NamespacedKey, DiscoverInfo>> discovered;

	public DiscoverHandler() {
		instance = this;

		discovered = new HashMap<>();
		Bukkit.getPluginManager().registerEvents(new MoveListener(), PathPlugin.getInstance());
	}

	public void playDiscovery(UUID playerId, Discoverable discoverable) {
		Player player = Bukkit.getPlayer(playerId);

		PathPlugin.getInstance().getAudiences().player(playerId).showTitle(Title.title(
				Messages.LOCATION_FOUND_TITLE_1.asComponent(player,
						Placeholder.component("name", discoverable.getDisplayName())),
				Messages.LOCATION_FOUND_TITLE_2.asComponent(player,
						Placeholder.component("name", discoverable.getDisplayName()),
						Placeholder.component("percent", Component.text("10%"))),
				Title.Times.times(
						Duration.ofSeconds(1),
						Duration.ofSeconds(2),
						Duration.ofSeconds(1)
				)
		));
		player.playSound(player.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1f, 1f);
	}

	public void discover(UUID playerId, Discoverable discoverable, Date date) {
		if(hasDiscovered(playerId, discoverable)) {
			return;
		}
		discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).put(discoverable.getUniqueKey(), new DiscoverInfo(playerId, discoverable, date));
		playDiscovery(playerId, discoverable);
	}

	public void forget(UUID playerId, Discoverable discoverable) {

	}

	public boolean hasDiscovered(UUID playerId, Discoverable discoverable) {
		return discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).containsKey(discoverable.getUniqueKey());
	}

	public Collection<Discoverable> getDiscovered(UUID playerId, RoadMap roadMap) {
		return discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).values().stream()
				.map(DiscoverInfo::discoverable)
				.collect(Collectors.toSet());
	}

	public int getDiscoveredCount(UUID playerId, RoadMap roadMap) {
		return getDiscovered(playerId, roadMap).size();
	}

	public float getDiscoveredPercent(UUID uuid, RoadMap roadMap) {
		int count = 0, sum = 0;
		for (Discoverable discoverable : getDiscovered(uuid, roadMap)) {
			count += discoverable.getDiscoveringWeight();
		}
		/* TODO for (Discoverable discoverable : roadMap.getDiscoverables()) {
			sum += discoverable.getDiscoveringWeight();
		}*/
		return count / (float) sum;
	}

	public float getDiscoveryDistance(UUID playerId, RoadMap roadMap) {
		return 3f;
	}
}
