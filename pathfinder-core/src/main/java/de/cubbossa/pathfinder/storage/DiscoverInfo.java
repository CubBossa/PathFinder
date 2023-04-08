package de.cubbossa.pathfinder.storage;

import java.time.LocalDateTime;
import java.util.UUID;
import org.bukkit.NamespacedKey;

public record DiscoverInfo(
    UUID playerId,
    NamespacedKey discoverable,
    LocalDateTime foundDate
) {

}
