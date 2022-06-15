package de.bossascrew.pathfinder.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class FoundInfo {

    private final UUID playerId;
    private final int foundId;
    private final Date foundDate;
}
