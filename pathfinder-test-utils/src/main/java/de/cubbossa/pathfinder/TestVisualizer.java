package de.cubbossa.pathfinder;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.navigation.UpdatingPath;
import de.cubbossa.pathfinder.node.Node;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import java.util.List;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;

public class TestVisualizer extends AbstractVisualizer<TestVisualizer.View, Logger> {

  public TestVisualizer(NamespacedKey key) {
    super(key);
  }

  @Override
  public Class<Logger> getTargetType() {
    return Logger.class;
  }

  @Override
  public View createView(UpdatingPath nodes, PathPlayer<Logger> player) {
    return new View(player, nodes);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TestVisualizer visualizer && getKey().equals(visualizer.getKey());
  }

  @Getter
  @Setter
  public class View extends AbstractVisualizer<View, Logger>.AbstractView {

    public View(PathPlayer<Logger> player, UpdatingPath nodes) {
      super(player, nodes);
    }
  }
}
