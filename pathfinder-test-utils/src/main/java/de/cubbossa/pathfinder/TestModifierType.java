package de.cubbossa.pathfinder;

import com.google.auto.service.AutoService;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.misc.NamespacedKey;
import java.io.IOException;
import java.util.Map;

@AutoService(ModifierType.class)
public class TestModifierType implements ModifierType<TestModifier> {

  public static final NamespacedKey KEY = NamespacedKey.fromString("pathfinder:test-modifier");

  @Override
  public NamespacedKey getKey() {
    return KEY;
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
