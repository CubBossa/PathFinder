package de.cubbossa.pathapi.group;

import java.util.Collection;

public interface ModifierRegistry {

  <M extends Modifier> void registerModifierType(ModifierType<M> modifierType);

  Collection<ModifierType<?>> getModifiers();
}
