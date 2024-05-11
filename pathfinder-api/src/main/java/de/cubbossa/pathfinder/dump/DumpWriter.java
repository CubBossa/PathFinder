package de.cubbossa.pathfinder.dump;

import de.cubbossa.disposables.Disposable;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * The DumpWriter interface creates a full report of the plugin, configuration, environment and data, to
 * achieve smooth data conversions between different major versions or to make error detection simpler.
 */
public interface DumpWriter extends Disposable {

  void addProperty(String name, Object data);

  void addProperty(String name, Supplier<Object> data);

  boolean removeProperty(String name);

  String toString();

  void save(File file) throws IOException;
}
