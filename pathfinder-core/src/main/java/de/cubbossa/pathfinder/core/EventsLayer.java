package de.cubbossa.pathfinder.core;

import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupCreateEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroup;
import de.cubbossa.pathfinder.data.ApplicationLayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class EventsLayer extends PassLayer implements ApplicationLayer {

	private final ApplicationLayer subLayer;

	public MessageLayer messageLayer(CommandSender sender) {
		return new MessageLayer(sender, this);
	}

	@Override
	public CompletableFuture<NodeGroup> createNodeGroup(NamespacedKey key) {
		NodeGroupCreateEvent event = new NodeGroupCreateEvent(key);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return CompletableFuture.failedFuture(new RuntimeException("Event cancelled"));
		}
		return subLayer.createNodeGroup(key);
	}

	@Override
	public CompletableFuture<Void> deleteNodeGroup(NamespacedKey key) {
		NodeGroupDeleteEvent event = new NodeGroupDeleteEvent(key);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return CompletableFuture.failedFuture(new RuntimeException("Event cancelled"));
		}
		return subLayer.deleteNodeGroup(key);
	}
}
