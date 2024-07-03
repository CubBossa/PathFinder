package de.cubbossa.pathfinder.navigation;

import static de.cubbossa.pathfinder.navigation.NavigationLocation.fixedExternalNode;
import static de.cubbossa.pathfinder.navigation.NavigationLocation.fixedGraphNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.Changes;
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException;
import de.cubbossa.pathfinder.graph.GraphEntrySolver;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.group.NodeGroup;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.GroupedNode;
import de.cubbossa.pathfinder.node.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
class RouteTest {

  @BeforeAll
  static void beforeAll() {
    NavigationLocationImpl.GRAPH_ENTRY_SOLVER = new GraphEntrySolver<Node>() {
      @Override
      public MutableValueGraph<Node, Double> solveEntry(Node in, MutableValueGraph<Node, Double> scope)
          throws GraphEntryNotEstablishedException {
        return solveBoth(in, scope);
      }

      @Override
      public MutableValueGraph<Node, Double> solveExit(Node out, MutableValueGraph<Node, Double> scope)
          throws GraphEntryNotEstablishedException {
        return solveBoth(out, scope);
      }

      private MutableValueGraph<Node, Double> solveBoth(Node node, MutableValueGraph<Node, Double> scope)
          throws GraphEntryNotEstablishedException {

        Node nearest = scope.nodes().stream()
            .filter(n -> !n.getNodeId().equals(node.getNodeId()))
            .min(Comparator.comparingDouble(o -> o.getLocation().distance(node.getLocation())))
            .orElse(null);
        if (!scope.nodes().contains(node)) {
          scope.addNode(node);
        }
        double d = node.getLocation().distance(nearest.getLocation());
        scope.putEdgeValue(nearest, node, d);
        scope.putEdgeValue(node, nearest, d);
        return scope;
      }
    };
  }

  @Test
  void testA() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(0, 0, 0, null));
    Node b = new TestNode("b", new Location(10, 0, 0, null));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, a, 10d);
    var result = Route.from(a).to(b).calculatePath(graph);
    assertEquals(List.of(a, b), result.getPath());
    assertEquals(10, result.getCost());

    result = Route.from(a).to(b).to(a).to(b).calculatePath(graph);
    assertEquals(List.of(a, b, a, b), result.getPath());
    assertEquals(30, result.getCost());

    result = Route.from(a).to(a).to(a).calculatePath(graph);
    assertEquals(List.of(a), result.getPath());
  }

  @Test
  void testB() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(0, 0, 0, null));
    Node b = new TestNode("b", new Location(10, 0, 0, null));
    Node c = new TestNode("c", new Location(20, 0, 0, null));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, a, 10d);
    graph.putEdgeValue(c, b, 10d);
    graph.putEdgeValue(b, c, 10d);
    graph.putEdgeValue(a, c, 20d);
    graph.putEdgeValue(c, a, 20d);

    var result = Route
        .from(a)
        .to(Route.from(b).to(a).to(b))
        .to(c)
        .calculatePath(graph);
    assertEquals(40, result.getCost());
    assertEquals(List.of(a, b, a, b, c), result.getPath());

    result = Route
        .from(a)
        .to(c)
        .to(Route.from(b).to(a).to(b))
        .calculatePath(graph);
    assertEquals(50, result.getCost());
    assertEquals(List.of(a, c, b, a, b), result.getPath());

    result = Route
        .from(a)
        .to(Route.from(a).to(b).to(a))
        .to(a)
        .calculatePath(graph);
    assertEquals(20, result.getCost());
    assertEquals(List.of(a, b, a), result.getPath());
  }

  @Test
  void testC() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(-10, 0, 0, null));
    Node b = new TestNode("b", new Location(0, 0, 0, null));
    Node c = new TestNode("c", new Location(10, 0, 0, null));
    Node d = new TestNode("d", new Location(20, 0, 0, null));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(c, b, 10d);
    graph.putEdgeValue(b, c, 10d);

    var result = Route
        .from(fixedExternalNode(a))
        .to(fixedExternalNode(d))
        .calculatePath(graph);

    assertEquals(30, result.getCost());
    assertEquals(List.of(a, b, c, d), result.getPath());
  }

  @Test
  void testD() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(-10, 0, 0, null));
    Node b = new TestNode("b", new Location(0, 0, 0, null));
    Node c = new TestNode("c", new Location(10, 0, 0, null));
    Node d = new TestNode("d", new Location(0, 15, 0, null));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, c, 10d);
    graph.putEdgeValue(b, d, 15d);

    var results = Route
        .from(fixedGraphNode(a))
        .toAny(fixedExternalNode(c), fixedGraphNode(d))
        .calculatePaths(graph);
    assertEquals(2, results.size());
    assertEquals(results.get(0).getPath().get(0), a);
    assertEquals(3, results.get(0).getPath().size());
    assertEquals(3, results.get(1).getPath().size());
    assertEquals(c, results.get(0).getPath().get(results.get(0).getPath().size() - 1));
    assertEquals(d, results.get(1).getPath().get(results.get(1).getPath().size() - 1));

    var result = Route
        .from(fixedGraphNode(a))
        .toAny(fixedExternalNode(c), fixedGraphNode(d))
        .calculatePath(graph);

    assertEquals(List.of(a, b, c), result.getPath());
    assertEquals(20, result.getCost());
  }

  @Test
  void testIslands() throws NoPathFoundException {
    Node a = new TestNode("a", new Location(0, -5, 0, null));
    Node b = new TestNode("b", new Location(10, -5, 0, null));
    Node c = new TestNode("c", new Location(0, 5, 0, null));
    Node d = new TestNode("d", new Location(10, 5, 0, null));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(c, d, 10d);

    var results = Route
        .from(fixedExternalNode(new TestNode("start", new Location(5, -10, 0, null))))
        .to(fixedExternalNode(new TestNode("end", new Location(5, 10, 0, null))))
        .calculatePaths(graph);
    assertEquals(1, results.size());
    assertEquals(results.get(0).getPath().get(1), a);
    assertEquals(3, results.get(0).getPath().size());
  }

  @Test
  void testGroupedNodePreservance() throws NoPathFoundException {
    Node a = new GroupedTestNode(new TestNode("a", new Location(0, -5, 0, null)), new ArrayList<>());
    Node b = new GroupedTestNode(new TestNode("b", new Location(10, -5, 0, null)), new ArrayList<>());
    Node c = new GroupedTestNode(new TestNode("c", new Location(10, 5, 0, null)), new ArrayList<>());
    Node d = new GroupedTestNode(new TestNode("d", new Location(0, 5, 0, null)), new ArrayList<>());
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.putEdgeValue(a, b, 10d);
    graph.putEdgeValue(b, c, 10d);
    graph.putEdgeValue(c, d, 10d);

    var results = Route
        .from(fixedExternalNode(new TestNode("start", new Location(0, -10, 0, null))))
        .to(fixedExternalNode(new TestNode("end", new Location(0, 10, 0, null))))
        .calculatePaths(graph);
    assertEquals(1, results.size());
    assertEquals(results.get(0).getPath().get(1), a);
    assertEquals(6, results.get(0).getPath().size());
    assertEquals(40, results.get(0).getCost());

    assertInstanceOf(GroupedNode.class, results.get(0).getPath().get(0));
  }

  @Getter
  @Setter
  private class TestNode implements Node {

    private final String name;
    private final UUID nodeId;
    private Location location;

    public TestNode(String name, Location location) {
      this(name, UUID.randomUUID(), location);
    }

    public TestNode(String name, UUID uuid, Location location) {
      this.name = name;
      this.nodeId = uuid;
      this.location = location;
    }

    @Override
    public Changes<Edge> getEdgeChanges() {
      return new Changes<>();
    }

    @Override
    public Collection<Edge> getEdges() {
      return Collections.emptyList();
    }

    @Override
    public Optional<Edge> connect(UUID other, double weight) {
      return Optional.empty();
    }

    @Override
    public Node clone() {
      return new TestNode(name, location);
    }

    @Override
    public Node clone(UUID id) {
      return new TestNode(name, id, location);
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null) return false;
      Node testNode = (Node) object;
      return Objects.equals(getNodeId(), testNode.getNodeId());
    }

    @Override
    public int hashCode() {
      return getNodeId().hashCode();
    }
  }

  @RequiredArgsConstructor
  class GroupedTestNode implements GroupedNode {

    private final Node domain;
    private final Collection<NodeGroup> groups;

    @Override
    public boolean equals(Object obj) {
      return domain.equals(obj);
    }

    @Override
    public int hashCode() {
      return domain.hashCode();
    }

    @Override
    public String toString() {
      return "GroupedNode{domain=" + domain.toString() + "}";
    }

    @Override
    public Node node() {
      return domain;
    }

    @Override
    public Collection<NodeGroup> groups() {
      return groups;
    }

    @Override
    public GroupedNode merge(GroupedNode other) {
      return null;
    }

    @Override
    public UUID getNodeId() {
      return domain.getNodeId();
    }

    @Override
    public Location getLocation() {
      return domain.getLocation();
    }

    @Override
    public void setLocation(Location location) {
      domain.setLocation(location);
    }

    @Override
    public Changes<Edge> getEdgeChanges() {
      return domain.getEdgeChanges();
    }

    @Override
    public Collection<Edge> getEdges() {
      return domain.getEdges();
    }

    @Override
    public Optional<Edge> connect(UUID other, double weight) {
      return domain.connect(other, weight);
    }

    @Override
    public Node clone() {
      return new GroupedTestNode(domain.clone(), groups);
    }

    @Override
    public Node clone(UUID id) {
      return new GroupedTestNode(domain.clone(id), groups);
    }
  }
}