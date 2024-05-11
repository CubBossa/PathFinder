package de.cubbossa.pathfinder.group;

import de.cubbossa.pathfinder.misc.Keyed;
import java.io.IOException;
import java.util.Map;
import org.pf4j.ExtensionPoint;

public interface ModifierType<M extends Modifier> extends Keyed, ExtensionPoint {

  default String getSubCommandLiteral() {
    return getKey().getKey();
  }

  Map<String, Object> serialize(M modifier);

  M deserialize(Map<String, Object> values) throws IOException;
}
