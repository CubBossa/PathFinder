package de.cubbossa.pathfinder.data;

import de.cubbossa.pathfinder.core.node.Discoverable;

import java.util.Date;
import java.util.UUID;

public record DiscoverInfo(
		UUID playerId,
		Discoverable discoverable,
		Date foundDate
) {

}
