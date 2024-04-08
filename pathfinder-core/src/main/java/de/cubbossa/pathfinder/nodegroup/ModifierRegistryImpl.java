package de.cubbossa.pathfinder.nodegroup;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.group.Modifier;
import de.cubbossa.pathfinder.group.ModifierRegistry;
import de.cubbossa.pathfinder.group.ModifierType;
import de.cubbossa.pathfinder.misc.KeyedRegistry;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.util.ExtensionPoint;
import de.cubbossa.pathfinder.util.HashedRegistry;
import java.util.Collection;
import java.util.Optional;

public class ModifierRegistryImpl implements ModifierRegistry {

  public final ExtensionPoint<ModifierType> EXTENSION_POINT = new ExtensionPoint<>(ModifierType.class);

  private final KeyedRegistry<ModifierType<?>> modifiers;

  public ModifierRegistryImpl(PathFinder pathFinder) {
    modifiers = new HashedRegistry<>();
    pathFinder.getDisposer().register(pathFinder, this);

    EXTENSION_POINT.getExtensions().forEach(this::registerModifierType);
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
