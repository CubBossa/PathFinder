package de.cubbossa.pathapi.storage;

import java.time.LocalDateTime;
import java.util.UUID;
import de.cubbossa.pathapi.misc.NamespacedKey;

public record DiscoverInfo(
    UUID playerId,
    NamespacedKey discoverable,
    LocalDateTime foundDate
) {

}
