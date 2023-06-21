package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.logging.Logger;

public class TestVisualizer extends AbstractVisualizer<TestVisualizer.View, Logger> {

  public TestVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public Class<Logger> getTargetType() {
    return Logger.class;
  }

  @Override
  public View createView(List<Node> nodes, PathPlayer<Logger> player) {
    return new View(player, nodes);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TestVisualizer visualizer && getKey().equals(visualizer.getKey());
  }

  @Getter
  @Setter
  public class View extends AbstractVisualizer<View, Logger>.AbstractView {
    List<Node> nodes;

    public View(PathPlayer<Logger> player, List<Node> nodes) {
      super(player);
      this.nodes = nodes;
    }
  }
}
