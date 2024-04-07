package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinderExtension;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class PathFinderExtensionBase implements PathFinderExtension {

  private boolean disabled = false;

  @Override
  public void disable() {
    disabled = true;
  }
}
