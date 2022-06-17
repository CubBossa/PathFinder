package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.node.Findable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

public record FoundInfo(UUID playerId, Findable found,
                        Date foundDate) {

}
