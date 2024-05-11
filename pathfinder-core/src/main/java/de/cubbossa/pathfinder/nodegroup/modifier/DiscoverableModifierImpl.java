package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.group.DiscoverableModifier;
import de.cubbossa.pathfinder.group.Modifier;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class DiscoverableModifierImpl implements DiscoverableModifier {

  private String nameFormat;
  private Component displayName;

  public DiscoverableModifierImpl(String nameFormat) {
    setNameFormat(nameFormat);
  }

  @Override
  public void setNameFormat(String name) {
    this.nameFormat = name;
    this.displayName = PathFinder.get().getMiniMessage().deserialize(nameFormat);
  }

  @Override
  public boolean equals(Object obj) {
    return !(obj instanceof Modifier mod) || getKey().equals(mod.getKey());
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}
