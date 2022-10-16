package de.cubbossa.pathfinder.data;

import org.bukkit.NamespacedKey;

import java.util.Date;
import java.util.UUID;

public record DiscoverInfo(
		UUID playerId,
		NamespacedKey discoverable,
		Date foundDate
) {

}
