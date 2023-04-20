package de.cubbossa.pathfinder.nodegroup;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModifierRegistryImpl implements ModifierRegistry {

  private final Map<Class<? extends Modifier>, ModifierType<?>> modifiers;

  public ModifierRegistryImpl() {
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
