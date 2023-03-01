package de.cubbossa.pathfinder.data;

import java.util.Date;
import java.util.UUID;
import org.bukkit.NamespacedKey;

public record DiscoverInfo(
    UUID playerId,
    NamespacedKey discoverable,
    Date foundDate
) {

}
