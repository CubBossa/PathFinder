package de.cubbossa.pathfinder.navigation;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import de.cubbossa.pathfinder.Changes;
import de.cubbossa.pathfinder.graph.GraphEntryNotEstablishedException;
import de.cubbossa.pathfinder.graph.GraphEntrySolver;
import de.cubbossa.pathfinder.graph.NoPathFoundException;
import de.cubbossa.pathfinder.misc.Location;
import de.cubbossa.pathfinder.misc.World;
import de.cubbossa.pathfinder.node.Edge;
import de.cubbossa.pathfinder.node.Node;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RouteTest {

  World world = new World() {

    UUID uuid = UUID.randomUUID();

    @NotNull
    @Override
    public UUID getUniqueId() {
      return uuid;
    }

    @NotNull
    @Override
    public String getName() {
      return uuid.toString();
    }
  };

  @BeforeAll
  static void beforeAll() {
    NavigationLocationImpl.GRAPH_ENTRY_SOLVER = new GraphEntrySolver<>() {
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
    Node a = new TestNode(UUID.randomUUID(), new Location(0, 0, 0, world));
    Node b = new TestNode(UUID.randomUUID(), new Location(10, 0, 0, world));
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
    Node a = new TestNode(UUID.randomUUID(), new Location(0, 0, 0, world));
    Node b = new TestNode(UUID.randomUUID(), new Location(10, 0, 0, world));
    Node c = new TestNode(UUID.randomUUID(), new Location(20, 0, 0, world));
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
    Node a = new TestNode(UUID.randomUUID(), new Location(-10, 0, 0, world));
    Node b = new TestNode(UUID.randomUUID(), new Location(0, 0, 0, world));
    Node c = new TestNode(UUID.randomUUID(), new Location(10, 0, 0, world));
    Node d = new TestNode(UUID.randomUUID(), new Location(20, 0, 0, world));
    MutableValueGraph<Node, Double> graph = ValueGraphBuilder.directed().build();
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(c, b, 10d);
    graph.putEdgeValue(b, c, 10d);

    var result = Route
        .from(NavigationLocation.fixedExternalNode(a))
        .to(NavigationLocation.fixedExternalNode(d))
        .calculatePath(graph);

    assertEquals(30, result.getCost());
    assertEquals(List.of(a, b, c, d), result.getPath());
  }

  @Getter
  @Setter
  @AllArgsConstructor
  private class TestNode implements Node {

    private UUID nodeId;
    private Location location;

    @Override
    public @NotNull Changes<Edge> getEdgeChanges() {
      return new Changes<>();
    }

    @Override
    public @NotNull Collection<Edge> getEdges() {
      return Collections.emptyList();
    }

    @Override
    public Edge connect(@NotNull UUID other, double weight) {
      return null;
    }

    @Override
    public @NotNull Node clone() {
      return new TestNode(nodeId, location);
    }

    @Override
    public @NotNull Node clone(@NotNull UUID id) {
      return new TestNode(id, location);
    }

    @Override
    public String toString() {
      return nodeId.toString().substring(0, 8);
    }
  }
}