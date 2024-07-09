package de.cubbossa.pathfinder.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
class GraphUtilsTest {

  @Test
  void islands() {

    MutableValueGraph<String, Double> aToB = ValueGraphBuilder.directed().build();
    aToB.addNode("a");
    aToB.addNode("b");
    aToB.putEdgeValue("a", "b", 1.5);
    Assertions.assertEquals(1, GraphUtils.islands(aToB).size());

    MutableValueGraph<String, Double> bToA = ValueGraphBuilder.directed().build();
    bToA.addNode("a");
    bToA.addNode("b");
    bToA.putEdgeValue("b", "a", 1.5);
    Assertions.assertEquals(1, GraphUtils.islands(bToA).size());

    MutableValueGraph<String, Double> aAndB = ValueGraphBuilder.directed().build();
    aAndB.addNode("a");
    aAndB.addNode("b");
    Assertions.assertEquals(2, GraphUtils.islands(aAndB).size());
  }
}