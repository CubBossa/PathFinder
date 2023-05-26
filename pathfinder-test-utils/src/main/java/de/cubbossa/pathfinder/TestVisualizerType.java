package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.misc.NamespacedKey;

import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import java.util.HashMap;
import java.util.Map;

public class TestVisualizerType extends AbstractVisualizerType<TestVisualizer> {

  public TestVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public TestVisualizer create(NamespacedKey key) {
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
