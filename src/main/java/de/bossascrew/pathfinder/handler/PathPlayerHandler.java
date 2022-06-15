package de.bossascrew.pathfinder.handler;

import com.google.common.collect.Maps;
import de.bossascrew.core.base.settings.SettingsHandler;
import de.bossascrew.core.player.GlobalPlayer;
import de.bossascrew.core.player.PlayerHandler;
import de.bossascrew.pathfinder.data.PathPlayer;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class PathPlayerHandler {

	@Getter
	private static PathPlayerHandler instance;

	@Getter
	private final PathPlayer consolePathPlayer;
	private final Map<Integer, PathPlayer> pathPlayer;

	public PathPlayerHandler() {
		instance = this;
		consolePathPlayer = new PathPlayer(-1, SettingsHandler.GLOBAL_UUID);
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
		PathPlayer pathPlayer = getLoadedPlayer(uuid);
		if (pathPlayer == null) {
			GlobalPlayer player = PlayerHandler.getInstance().getGlobalPlayer(uuid);
			if (player == null) {
				return null;
			}
			pathPlayer = createPlayer(player.getDatabaseId());
		}
		return pathPlayer;
	}

	public PathPlayer getPlayer(int globalPlayerId) {
		PathPlayer pathPlayer = getLoadedPlayer(globalPlayerId);
		if (pathPlayer == null) {
			//player wurde nicht aus der Datenbank geladen, also neuen anlegen
			pathPlayer = createPlayer(globalPlayerId);
		}
		return pathPlayer;
	}

	private @Nullable
	PathPlayer getLoadedPlayer(UUID uuid) {
		return pathPlayer.values().stream().filter(player -> player.getUuid().equals(uuid)).findAny().orElse(null);
	}

	private @Nullable
	PathPlayer getLoadedPlayer(int globalPlayerId) {
		return pathPlayer.get(globalPlayerId);
	}

	private PathPlayer createPlayer(int globalPlayerId) {
		PathPlayer newPlayer = new PathPlayer(globalPlayerId);
		pathPlayer.put(globalPlayerId, newPlayer);
		return newPlayer;
	}
}
