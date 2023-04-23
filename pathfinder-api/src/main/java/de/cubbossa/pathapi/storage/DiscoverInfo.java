package de.cubbossa.pathapi.storage;

import de.cubbossa.pathapi.misc.NamespacedKey;
import java.time.LocalDateTime;
import java.util.UUID;

public record DiscoverInfo(
    UUID playerId,
    NamespacedKey discoverable,
    LocalDateTime foundDate
) {

}
