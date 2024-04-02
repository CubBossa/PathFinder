package de.cubbossa.pathapi.group;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathapi.misc.NamespacedKey;

import java.util.Collection;
import java.util.Optional;

public interface ModifierRegistry extends Disposable {

  <M extends Modifier> void registerModifierType(ModifierType<M> modifierType);

  Collection<ModifierType<?>> getTypes();

  <M extends Modifier> Optional<ModifierType<M>> getType(NamespacedKey key);
}
