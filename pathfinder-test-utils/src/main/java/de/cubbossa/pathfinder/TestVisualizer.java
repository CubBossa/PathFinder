package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TestVisualizer extends AbstractVisualizer<TestVisualizer.Data, Logger> {

  public TestVisualizer(NamespacedKey key) {
    super(key);
  }

  record Data(List<Node> nodes) {
  }

  @Override
  public Class<Logger> getTargetType() {
    return Logger.class;
  }

  @Override
  public Data prepare(List<Node> nodes, PathPlayer<Logger> player) {
    return new Data(nodes);
  }

  @Override
  public void play(VisualizerContext<Data, Logger> context) {
    context.player().unwrap().log(Level.FINE, context.data().nodes.stream().map(Node::getNodeId).map(UUID::toString).collect(Collectors.joining(",")));
  }

  @Override
  public void destruct(PathPlayer<Logger> player, Data data) {

  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TestVisualizer visualizer && getKey().equals(visualizer.getKey());
  }
}
