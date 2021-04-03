package de.bossascrew.pathfinder.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

/**
 * nicht selbst erstellen sondern über DatabaseModel holen, damit gleich databaseid passt
 */
@AllArgsConstructor
@Getter
public class FoundInfo {

    private int databaseId;
    private int globalPlayerId;
    private int nodeId;
    private Date foundDate;

}
