package de.cubbossa.pathfinder.core.nodegroup;

import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.api.group.ModifierType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModifierRegistry implements de.cubbossa.pathfinder.api.group.ModifierRegistry {

  private final Map<Class<? extends Modifier>, ModifierType<?>> modifiers;

  public ModifierRegistry() {
    modifiers = new HashMap<>();
  }

  @Override
  public <M extends Modifier> void registerModifierType(ModifierType<M> modifierType) {
    modifiers.put(modifierType.getModifierClass(), modifierType);
  }

  @Override
  public Collection<ModifierType<?>> getModifiers() {
    return modifiers.values();
  }
}
