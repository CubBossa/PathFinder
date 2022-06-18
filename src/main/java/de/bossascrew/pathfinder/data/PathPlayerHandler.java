package de.bossascrew.pathfinder.data;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class PathPlayerHandler {

	public static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	@Getter
	private static PathPlayerHandler instance;

	@Getter
	private final PathPlayer consolePathPlayer;
	private final Map<UUID, PathPlayer> pathPlayer;

	public PathPlayerHandler() {
		instance = this;
		consolePathPlayer = new PathPlayer(CONSOLE_UUID);
		pathPlayer = Maps.newHashMap();
	}

	public Collection<PathPlayer> getPlayers() {
		return pathPlayer.values();
	}

	public PathPlayer getPlayer(CommandSender sender) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			return getPlayer(player.getUniqueId());
		}
		return consolePathPlayer;
	}

	public PathPlayer getPlayer(UUID uuid) {
		PathPlayer pathPlayer = this.pathPlayer.get(uuid);
		if (pathPlayer == null) {
			if (uuid.equals(CONSOLE_UUID)) {
				return consolePathPlayer;
			}
			pathPlayer = createPlayer(uuid);
		}
		return pathPlayer;
	}

	private PathPlayer createPlayer(UUID uuid) {
		PathPlayer newPlayer = new PathPlayer(uuid);
		pathPlayer.put(uuid, newPlayer);
		return newPlayer;
	}
}
