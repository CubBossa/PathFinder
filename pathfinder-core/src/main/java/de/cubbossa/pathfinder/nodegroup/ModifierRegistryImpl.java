package de.cubbossa.pathfinder.nodegroup;

import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.ModifierType;
import de.cubbossa.pathapi.misc.KeyedRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.util.HashedRegistry;

import java.util.Collection;
import java.util.Optional;

public class ModifierRegistryImpl implements ModifierRegistry {

  private final KeyedRegistry<ModifierType<?>> modifiers;

  public ModifierRegistryImpl() {
    modifiers = new HashedRegistry<>();
  }

  @Override
  public <M extends Modifier> void registerModifierType(ModifierType<M> modifierType) {
    if (modifiers.containsKey(modifierType.getKey())) {
      throw new IllegalArgumentException(
          "Another ModifierType with this modifier class has already been registered.");
    }
    modifiers.put(modifierType.getKey(), modifierType);
  }

  @Override
  public Collection<ModifierType<?>> getTypes() {
    return modifiers.values();
  }

  @Override
  public <M extends Modifier> Optional<ModifierType<M>> getType(NamespacedKey key) {
    return Optional.ofNullable((ModifierType<M>) modifiers.get(key));
  }
}
