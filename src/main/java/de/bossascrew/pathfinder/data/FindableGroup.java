package de.bossascrew.pathfinder.data;

import de.bossascrew.pathfinder.data.findable.Findable;
import de.bossascrew.pathfinder.data.findable.Node;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class FindableGroup {

    private final int databaseId;
    @Setter
    private String name;
    @Setter
    private boolean findable;
    private Collection<Findable> findables;

    public FindableGroup(int databaseId, String name) {
        this(databaseId, name, null);
    }

    public FindableGroup(int databaseId, String name, Collection<Findable> nodes) {
        this.databaseId = databaseId;
        this.name = name;
        this.findables = new ArrayList<>();
        this.findable = false;
        if (nodes != null) {
            this.findables = nodes;
        }
    }

    public void addNode(Node node) {
        this.findables.add(node);
    }

    public void addNodes(Collection<Node> nodes) {
        this.findables.addAll(nodes);
    }

    public void removeNode(Node node) {
        this.findables.remove(node);
    }

    public void delete() {
        for (Findable findable : findables) {
            findable.removeFindableGroup();
        }
    }
}
