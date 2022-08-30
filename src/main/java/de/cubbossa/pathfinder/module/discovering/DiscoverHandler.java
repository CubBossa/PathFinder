package de.cubbossa.pathfinder.module.discovering;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Discoverable;
import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.data.DiscoverInfo;
import de.cubbossa.pathfinder.module.discovering.event.PlayerDiscoverEvent;
import de.cubbossa.pathfinder.module.discovering.event.PlayerForgetEvent;
import de.cubbossa.pathfinder.module.visualizing.FindModule;
import de.cubbossa.serializedeffects.EffectHandler;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DiscoverHandler {

	@Getter
	private static DiscoverHandler instance;

	private final Map<UUID, Map<NamespacedKey, DiscoverInfo>> discovered;

	public DiscoverHandler() {
		instance = this;

		discovered = new HashMap<>();
		discovered.putAll(PathPlugin.getInstance().getDatabase().loadDiscoverInfo());
		if (!PathPlugin.getInstance().getConfiguration().isDiscoveryEnabled()) {
			return;
		}
		Bukkit.getPluginManager().registerEvents(new MoveListener(), PathPlugin.getInstance());

		if (PathPlugin.getInstance().getConfiguration().isFindLocationRequiresDiscovery()) {
			FindModule.getInstance().registerFindPredicate(context -> {
				if (context.navigable() instanceof Discoverable discoverable) {
					return hasDiscovered(context.playerId(), discoverable);
				}
				return true;
			});
		}
	}

	public void playDiscovery(UUID playerId, Discoverable discoverable) {
		Player player = Bukkit.getPlayer(playerId);
		if (player == null) {
			throw new IllegalStateException("Player is null");
		}
		EffectHandler.getInstance().playEffect(
				PathPlugin.getInstance().getEffectsFile(),
				"discover",
				player,
				player.getLocation(),
				TagResolver.builder()
						.resolver(Placeholder.component("name", discoverable.getDisplayName()))						.resolver(Placeholder.component("roadmaps", Component.text("Test, 10%")))
						.build());
	}

	public void discover(UUID playerId, Discoverable discoverable, Date date) {
		if (hasDiscovered(playerId, discoverable)) {
			return;
		}
		PlayerDiscoverEvent event = new PlayerDiscoverEvent(playerId, discoverable, date);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).put(discoverable.getUniqueKey(), new DiscoverInfo(playerId, discoverable.getUniqueKey(), date));
		playDiscovery(playerId, discoverable);
	}

	public void forget(UUID playerId, Discoverable discoverable) {
		if (!hasDiscovered(playerId, discoverable)) {
			return;
		}
		PlayerForgetEvent event = new PlayerForgetEvent(playerId, discoverable);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).remove(discoverable.getUniqueKey());
	}

	public boolean hasDiscovered(UUID playerId, Discoverable discoverable) {
		return discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).containsKey(discoverable.getUniqueKey());
	}

	public Collection<NamespacedKey> getDiscovered(UUID playerId, RoadMap roadMap) {
		return discovered.computeIfAbsent(playerId, uuid -> new HashMap<>()).values().stream()
				.map(DiscoverInfo::discoverable)
				.collect(Collectors.toSet());
	}

	public int getDiscoveredCount(UUID playerId, RoadMap roadMap) {
		return getDiscovered(playerId, roadMap).size();
	}

	public float getDiscoveredPercent(UUID uuid, RoadMap roadMap) {
		int count = 0, sum = 0;
		for (NamespacedKey discoverable : getDiscovered(uuid, roadMap)) {
			count++; //TODO weight
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
