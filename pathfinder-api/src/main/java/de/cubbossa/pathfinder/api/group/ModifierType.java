package de.cubbossa.pathfinder.api.group;

import java.io.IOException;
import java.util.Map;

public interface ModifierType<M extends Modifier> {

  Class<M> getModifierClass();

  String getSubCommandLiteral();

  Map<String, Object> serialize(M modifier);

  M deserialize(Map<String, Object> values) throws IOException;
}
