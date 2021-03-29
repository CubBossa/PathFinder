package pathfinder;

import lombok.Getter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

@Getter
public class NodeGroup {

    private int databaseId;
    private String name;
    private Collection<Node> nodes;

    public NodeGroup(int databaseId, String name) {
        this(databaseId, name, null);
    }

    public NodeGroup(int databaseId, String name, Collection<Node> nodes) {
        this.databaseId = databaseId;
        this.name = name;
        this.nodes = new ArrayList<Node>();
        if(nodes != null) {
            this.nodes = nodes;
        }
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void addNodes(Collection<Node> nodes) {
        this.nodes.addAll(nodes);
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
    }
}
