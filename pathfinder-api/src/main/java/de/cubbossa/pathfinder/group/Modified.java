package de.cubbossa.pathfinder.group;

import de.cubbossa.pathfinder.misc.NamespacedKey;
import java.util.Collection;
import java.util.Optional;

public interface Modified {

  Collection<Modifier> getModifiers();

  <M extends Modifier> boolean hasModifier(Class<M> modifierClass);

  <M extends Modifier> boolean hasModifier(NamespacedKey modifierType);

  default <M extends Modifier> void addModifier(M modifier) {
    addModifier(modifier.getKey(), modifier);
  }

  void addModifier(NamespacedKey key, Modifier modifier);

  <M extends Modifier> Optional<M> getModifier(NamespacedKey key);

  <M extends Modifier> void removeModifier(Class<M> modifierClass);

  <M extends Modifier> void removeModifier(M modifier);

  <M extends Modifier> void removeModifier(NamespacedKey key);

  void clearModifiers();
}
