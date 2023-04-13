package de.cubbossa.pathfinder.api.storage;

import java.time.LocalDateTime;
import java.util.UUID;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;

public record DiscoverInfo(
    UUID playerId,
    NamespacedKey discoverable,
    LocalDateTime foundDate
) {

}
