package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.EventsLayer;
import de.cubbossa.pathfinder.core.MessageLayer;
import de.cubbossa.pathfinder.data.ApplicationLayer;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PathFinderAPI implements ApplicationLayer {

	@Getter
	private static PathFinderAPI instance;

	private final ApplicationLayer dataStorage;

	public PathFinderAPI(ApplicationLayer dataStorage) {
		instance = this;

		this.dataStorage = dataStorage;
	}

	public EventsLayer eventLayer() {
		return new EventsLayer(this);
	}

	public MessageLayer messageLayer(CommandSender sender) {
		return new MessageLayer(sender, this);
	}

	@Override
	public CompletableFuture<Collection<NamespacedKey>> getNodeGroupKeySet() {
		return dataStorage.getNodeGroupKeySet();
	}
}
