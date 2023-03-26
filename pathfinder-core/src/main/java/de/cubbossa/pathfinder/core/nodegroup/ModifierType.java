package de.cubbossa.pathfinder.core.nodegroup;

import de.cubbossa.pathfinder.Modifier;
import dev.jorel.commandapi.CommandTree;
import java.io.IOException;
import java.util.Map;

public interface ModifierType<M extends Modifier> {

  Class<M> getModifierClass();

  String getSubCommandLiteral();

  CommandTree registerAddCommand(CommandTree tree, int groupIndex);

  Map<String, Object> serialize(M modifier);

  M deserialize(Map<String, Object> values) throws IOException;
}
