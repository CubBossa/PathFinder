package de.bossascrew.pathfinder.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@RequiredArgsConstructor
@Getter
public class FoundInfo {

    private final int globalPlayerId;
    private final int foundId;
    private final Date foundDate;
}
