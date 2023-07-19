package de.cubbossa.pathapi.group;

import de.cubbossa.pathapi.misc.Keyed;

import java.io.IOException;
import java.util.Map;

public interface ModifierType<M extends Modifier> extends Keyed {

  String getSubCommandLiteral();

  Map<String, Object> serialize(M modifier);

  M deserialize(Map<String, Object> values) throws IOException;
}
