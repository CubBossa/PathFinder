package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.data.findable.Node;
import de.bossascrew.pathfinder.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class FindableGroup {

    private final int databaseId;

    private final RoadMap roadMap;
    @Setter
    private String name;
    @Setter
    private boolean findable;
    private Collection<Node> findables;

    public FindableGroup(int databaseId, RoadMap roadMap, String name) {
        this(databaseId, roadMap, name, null);
    }

    public FindableGroup(int databaseId, RoadMap roadMap, String name, Collection<Node> nodes) {
        this.databaseId = databaseId;
        this.roadMap = roadMap;
        this.name = name;
        this.findables = new ArrayList<>();
        this.findable = false;
        if (nodes != null) {
            this.findables = nodes;
        }
    }

    public void setName(String name, boolean update) {
        this.name = name;
        if(update) {
            update();
        }
    }

    public void setFindable(boolean findable, boolean update) {
        this.findable = findable;
        if(update) {
            update();
        }
    }

    public void update() {
        PluginUtils.getInstance().runAsync(() -> SqlStorage.getInstance().updateFindableGroup(this));
    }

    public void delete() {
        for (Node findable : findables) {
            findable.removeFindableGroup(true);
        }
    }

    public String getFriendlyName() {
        return StringUtils.replaceBlanks(name);
    }
}
