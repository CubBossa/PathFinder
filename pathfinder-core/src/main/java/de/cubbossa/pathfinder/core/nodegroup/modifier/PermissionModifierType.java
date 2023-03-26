package de.cubbossa.pathfinder.core.nodegroup.modifier;

import de.cubbossa.nbo.LinkedHashMapBuilder;
import de.cubbossa.pathfinder.PathFinderAPI;
import de.cubbossa.pathfinder.core.nodegroup.ModifierType;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.StringArgument;
import java.io.IOException;
import java.util.Map;
import org.bukkit.NamespacedKey;

public class PermissionModifierType implements ModifierType<PermissionModifier> {

  @Override
  public Class<PermissionModifier> getModifierClass() {
    return PermissionModifier.class;
  }

  @Override
  public String getSubCommandLiteral() {
    return "permission";
  }

  @Override
  public CommandTree registerAddCommand(CommandTree tree, int groupIndex) {
    return tree
        .then(new StringArgument("permission-node")
            .executes((commandSender, objects) -> {
              NamespacedKey group = (NamespacedKey) objects[groupIndex];
              PathFinderAPI.get().assignNodeGroupModifier(
                  group,
                  new PermissionModifier((String) objects[groupIndex + 1])
              );
            })
        );
  }

  @Override
  public Map<String, Object> serialize(PermissionModifier modifier) {
    return new LinkedHashMapBuilder<String, Object>()
        .put("node", modifier.permission())
        .build();
  }

  @Override
  public PermissionModifier deserialize(Map<String, Object> values) throws IOException {
    if (values.containsKey("node") && values.get("node") instanceof String string) {
      return new PermissionModifier(string);
    }
    throw new IOException("Could not deserialize permission modifier, missing 'node' attribute.");
  }
}
