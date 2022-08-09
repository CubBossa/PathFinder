package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.core.node.Findable;

import java.util.Date;
import java.util.UUID;

public record FoundInfo(UUID playerId, Findable found,
                        Date foundDate) {

}
