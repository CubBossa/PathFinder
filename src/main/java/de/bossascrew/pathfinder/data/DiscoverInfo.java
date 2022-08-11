package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.core.node.Discoverable;

import java.util.Date;
import java.util.UUID;

public record DiscoverInfo(
		UUID playerId,
		Discoverable discoverable,
		Date foundDate
) {

}
