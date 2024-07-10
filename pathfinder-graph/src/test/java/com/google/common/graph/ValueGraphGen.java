package com.google.common.graph;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.GeneratorConfiguration;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public class ValueGraphGen<N, V> extends Generator<ValueGraph<N, V>> {

  @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
  @Retention(RetentionPolicy.RUNTIME)
  @GeneratorConfiguration
  @interface Configuration {
    float directed = 0.1f;
    boolean allowSelfLoops = false;
    int maxSize = Integer.MAX_VALUE;
    int maxConnectionsPerNode = 0;
    int minConnectionsPerNode = 4;
  }

  private Configuration configuration;
  private final Class<N> nodeType;
  private final Class<V> edgeType;

  @Configuration
  protected ValueGraphGen(Class<ValueGraph<N, V>> graphType, Class<N> nodeType, Class<V> edgeType) {
    super(graphType);
    this.nodeType = nodeType;
    this.edgeType = edgeType;
  }

  @Override
  public ValueGraph<N, V> generate(SourceOfRandomness random, GenerationStatus status) {
    int size = random.nextInt(0, configuration.maxSize);
    float directed = Float.min(Float.max(configuration.directed, 0f), 1f);

    MutableValueGraph<N, V> graph = ValueGraphBuilder.directed()
        .allowsSelfLoops(configuration.allowSelfLoops)
        .expectedNodeCount(size)
        .build();

    List<N> nodes = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      N node = gen().type(nodeType).generate(random, status);
      graph.addNode(node);
      nodes.add(node);
    }
    for (N node : nodes) {
      int edges = random.nextInt(configuration.minConnectionsPerNode, configuration.maxConnectionsPerNode);
      edges = Integer.min(edges, size);

      for (int i = 0; i < edges; i++) {
        graph.putEdgeValue(node, nodes.stream().findAny().get(), gen().type(edgeType).generate(random, status));
      }
    }
    return graph;
  }
}
