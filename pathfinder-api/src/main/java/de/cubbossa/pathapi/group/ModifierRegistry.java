package de.cubbossa.pathapi.group;

import java.util.Collection;
import java.util.Optional;

public interface ModifierRegistry {

  <M extends Modifier> void registerModifierType(ModifierType<M> modifierType);

  Collection<ModifierType<?>> getTypes();

  <M extends Modifier> Optional<ModifierType<M>> getType(String clazzName) throws ClassNotFoundException;
  <M extends Modifier> Optional<ModifierType<M>> getType(Class<M> clazz);
}
