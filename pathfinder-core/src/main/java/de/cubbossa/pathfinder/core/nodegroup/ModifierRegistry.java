package de.cubbossa.pathfinder.core.nodegroup;

import de.cubbossa.pathfinder.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModifierRegistry {

  private final Map<Class<? extends Modifier>, ModifierType<?>> modifiers;

  public ModifierRegistry() {
    modifiers = new HashMap<>();
  }

  public <M extends Modifier> void registerModifierType(ModifierType<M> modifierType) {
    modifiers.put(modifierType.getModifierClass(), modifierType);
  }

  public Collection<ModifierType<?>> getModifiers() {
    return modifiers.values();
  }
}
