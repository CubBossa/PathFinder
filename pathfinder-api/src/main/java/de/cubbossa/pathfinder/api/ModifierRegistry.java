package de.cubbossa.pathfinder.api;

import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.api.group.ModifierType;
import java.util.Collection;

public interface ModifierRegistry {

  <M extends Modifier> void registerModifierType(ModifierType<M> modifierType);

  Collection<ModifierType<?>> getModifiers();
}
