package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import java.time.LocalDateTime;
import java.util.UUID;

public record DiscoverInfo(
    UUID playerId,
    NamespacedKey discoverable,
    LocalDateTime foundDate
) {

}
