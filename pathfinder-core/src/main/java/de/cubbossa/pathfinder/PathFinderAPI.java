package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.core.EventsLayer;
import de.cubbossa.pathfinder.core.MessageLayer;
import de.cubbossa.pathfinder.storage.ApplicationLayer;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;

public class PathFinderAPI {

	public static Builder builder() {
		return new Builder();
	}

	private static final ApplicationLayer generalAPI = new Builder()
			.withEvents()
			.withLogging(PathPlugin.getInstance().getLogger())
			.withPersistence()
			.build();

	public static ApplicationLayer get() {
		return generalAPI;
	}

	public static class Builder {

		private boolean persistenceLayer;
		private Logger loggingLayer;
		private boolean eventsLayer;
		private CommandSender messageLayer;
		private CommandSender permissionLayer;

		public Builder withEvents() {
			eventsLayer = true;
			return this;
		}

		public Builder withMessages(CommandSender receiver) {
			messageLayer = receiver;
			return this;
		}
		public Builder withPersistence() {
			persistenceLayer = true;
			return this;
		}

		public Builder withPermissionChecks(CommandSender sender) {
			permissionLayer = sender;
			return this;
		}

		public Builder withLogging(Logger logger) {
			loggingLayer = logger;
			return this;
		}

		public ApplicationLayer build() {
			ApplicationLayer layer = PathPlugin.getInstance().getStorage();

			if (eventsLayer) {
				layer = new EventsLayer(layer);
			}
			if (messageLayer != null) {
				layer = new MessageLayer(messageLayer, layer);
			}
			return layer;
		}
	}
}
