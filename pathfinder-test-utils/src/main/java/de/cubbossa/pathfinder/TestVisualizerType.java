package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.visualizer.impl.InternalVisualizerType;

import java.util.HashMap;
import java.util.Map;

public class TestVisualizerType extends InternalVisualizerType<TestVisualizer> {

  public TestVisualizerType(NamespacedKey key) {
    super(key);
  }

  @Override
  public TestVisualizer create(NamespacedKey key, String nameFormat) {
    return new TestVisualizer(key, nameFormat);
  }

  @Override
  public Map<String, Object> serialize(TestVisualizer visualizer) {
    return new HashMap<>();
  }

  @Override
  public void deserialize(TestVisualizer visualizer, Map<String, Object> values) {
  }
}
