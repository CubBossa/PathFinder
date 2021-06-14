package de.bossascrew.pathfinder.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class FoundInfo {

    private int globalPlayerId;
    private int nodeId;
    private Date foundDate;
}
