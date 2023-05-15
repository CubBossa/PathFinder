package de.cubbossa.pathfinder.nodegroup.modifier;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.DiscoverableModifier;
import de.cubbossa.pathapi.group.Modifier;
import de.cubbossa.pathapi.misc.Named;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class SimpleDiscoverableModifier implements DiscoverableModifier {

  private String nameFormat;
  private Component displayName;

  public SimpleDiscoverableModifier(String nameFormat) {
    this.nameFormat = nameFormat;
  }

  @Override
  public void setNameFormat(String name) {
    this.nameFormat = name;
    this.displayName = PathFinderProvider.get().getMiniMessage().deserialize(nameFormat);
  }
}
