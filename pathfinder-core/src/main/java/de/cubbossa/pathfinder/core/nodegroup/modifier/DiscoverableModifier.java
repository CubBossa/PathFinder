package de.cubbossa.pathfinder.core.nodegroup.modifier;

import de.cubbossa.pathfinder.api.group.Modifier;
import de.cubbossa.pathfinder.api.Named;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public class DiscoverableModifier implements Modifier, Named {

  private String nameFormat;
  private Component displayName;

  public DiscoverableModifier(String nameFormat) {
    this.nameFormat = nameFormat;
  }

  @Override
  public void setNameFormat(String name) {
    this.nameFormat = name;
    this.displayName = TranslationHandler.getInstance().getMiniMessage().deserialize(nameFormat);
  }

}
