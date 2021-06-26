package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Getter
public class FindableGroup extends HashSet<Findable> {

    private final int databaseId;

    private final RoadMap roadMap;
    @Setter
    private String name;
    @Setter
    private boolean findable;
    private Collection<Findable> findables;

    public FindableGroup(int databaseId, RoadMap roadMap, String name) {
        this(databaseId, roadMap, name, null);
    }

    public FindableGroup(int databaseId, RoadMap roadMap, String name, Collection<Findable> nodes) {
        this.databaseId = databaseId;
        this.roadMap = roadMap;
        this.name = name;
        this.findables = new ArrayList<>();
        this.findable = false;
        if (nodes != null) {
            this.findables = nodes;
        }
    }

    public void delete() {
        for (Findable findable : findables) {
            findable.removeFindableGroup();
        }
    }
}
