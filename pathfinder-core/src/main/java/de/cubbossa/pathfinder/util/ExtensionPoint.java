package de.cubbossa.pathfinder.util;

import de.cubbossa.disposables.Disposable;
import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;

@Getter
public class ExtensionPoint<T> implements Disposable {

  static PluginManager pluginManager;

  private final Class<T> type;

  public ExtensionPoint(final @NotNull Class<T> type) {
    this.type = type;
    if (pluginManager == null) {
      pluginManager = new DefaultPluginManager();
    }
  }

  public final Collection<T> getExtensions() {
    return pluginManager.getExtensions(type);
  }

  public final Optional<T> getExtension() {
    return pluginManager.getExtensions(type).stream().findFirst();
  }
}
