package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.node.Navigable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class FoundInfo {

    private final UUID playerId;
    private final Navigable found;
    private final Date foundDate;
}
