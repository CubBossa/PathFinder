package de.cubbossa.pathfinder.util;

import de.cubbossa.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ExtensionPoint<T> implements Disposable {

  @Getter
  private final Class<T> type;
  private List<T> cache;

  public ExtensionPoint(final @NotNull Class<T> type) {
    this.type = type;
    this.cache = null;
  }

  public final Collection<T> getExtensions() {
    if (cache == null) {
      cache = new ArrayList<>();
      ServiceLoader<T> loader = ServiceLoader.load(type, this.getClass().getClassLoader());
      loader.forEach(cache::add);
    }
    return Collections.unmodifiableCollection(cache);
  }
}
