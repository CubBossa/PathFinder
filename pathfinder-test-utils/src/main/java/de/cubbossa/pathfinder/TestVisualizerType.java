package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import java.util.HashMap;
import java.util.Map;
import org.pf4j.Extension;

@Extension(points = VisualizerType.class)
public class TestVisualizerType extends AbstractVisualizerType<TestVisualizer> {

  public TestVisualizerType() {
    super(AbstractPathFinder.pathfinder("particle"));
  }

  @Override
  public TestVisualizer createVisualizerInstance(NamespacedKey key) {
    return new TestVisualizer(key);
  }

  @Override
  public Map<String, Object> serialize(TestVisualizer visualizer) {
    return new HashMap<>();
  }

  @Override
  public void deserialize(TestVisualizer visualizer, Map<String, Object> values) {
  }
}
