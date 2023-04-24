package de.cubbossa.pathfinder.nodegroup;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.ModifierType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModifierRegistryImpl implements ModifierRegistry {

  private final Map<Class<? extends Modifier>, ModifierType<?>> modifiers;

  public ModifierRegistryImpl() {
    modifiers = new HashMap<>();
  }

  @Override
  public <M extends Modifier> void registerModifierType(ModifierType<M> modifierType) {
    if (modifiers.containsKey(modifierType.getModifierClass())) {
      throw new IllegalArgumentException(
          "Another ModifierType with this modifier class has already been registered.");
    }
    modifiers.put(modifierType.getModifierClass(), modifierType);
  }

  @Override
  public Collection<ModifierType<?>> getTypes() {
    return modifiers.values();
  }

  @Override
  public <M extends Modifier> Optional<ModifierType<M>> getType(Class<M> clazz) {
    ModifierType<M> type = (ModifierType<M>) modifiers.get(clazz);
    return Optional.ofNullable(type);
  }

  @Override
  public <M extends Modifier> Optional<ModifierType<M>> getType(String clazzName)
      throws ClassNotFoundException {
    return getType((Class<M>) Class.forName(clazzName));
  }
}
