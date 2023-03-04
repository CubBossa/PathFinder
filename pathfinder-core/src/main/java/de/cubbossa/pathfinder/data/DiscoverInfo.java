package de.cubbossa.pathfinder.data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import org.bukkit.NamespacedKey;

public record DiscoverInfo(
    UUID playerId,
    NamespacedKey discoverable,
    LocalDateTime foundDate
) {

}
