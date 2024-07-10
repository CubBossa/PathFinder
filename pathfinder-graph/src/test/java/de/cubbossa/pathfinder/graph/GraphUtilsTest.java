package de.cubbossa.pathfinder.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.graph.ValueGraphGen;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(JUnitQuickcheck.class)
public class GraphUtilsTest {

  @Property
  public void islands() {

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

  @Property
  public void lol(String s) {
    System.out.println(s);
  }

  @Property
  public void testMerge(@From(ValueGraphGen.class) ValueGraph<String, Integer> graph) {
    System.out.println(graph);
    Assertions.assertEquals(
        graph,
        GraphUtils.merge(graph, graph)
    );
  }
}