package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.group.ModifierType;

import java.io.IOException;
import java.util.Map;

public class TestModifierType implements ModifierType<TestModifier> {
  @Override
  public Class<TestModifier> getModifierClass() {
    return TestModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "mod";
  }

  @Override
  public Map<String, Object> serialize(TestModifier modifier) {
    return Map.of("data", modifier.data());
  }

  @Override
  public TestModifier deserialize(Map<String, Object> values) throws IOException {
    return new TestModifier((String) values.get("data"));
  }
}
