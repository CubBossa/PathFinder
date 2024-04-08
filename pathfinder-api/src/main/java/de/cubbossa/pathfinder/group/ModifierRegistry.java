package de.cubbossa.pathfinder.group;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.misc.NamespacedKey;

import java.util.Collection;
import java.util.Optional;

public interface ModifierRegistry extends Disposable {

  <M extends Modifier> void registerModifierType(ModifierType<M> modifierType);

  Collection<ModifierType<?>> getTypes();

  <M extends Modifier> Optional<ModifierType<M>> getType(NamespacedKey key);
}
