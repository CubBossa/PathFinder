package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.Modifier;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class CommonDiscoverableModifier implements DiscoverableModifier {

  private String nameFormat;
  private Component displayName;

  public CommonDiscoverableModifier(String nameFormat) {
    setNameFormat(nameFormat);
  }

  @Override
  public void setNameFormat(String name) {
    this.nameFormat = name;
    this.displayName = PathFinderProvider.get().getMiniMessage().deserialize(nameFormat);
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
