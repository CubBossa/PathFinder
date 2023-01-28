package de.cubbossa.pathfinder;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DependencyLoader {

  private final Logger logger;
  private final Function<PathPlugin, Dependency> factory;
  private final Predicate<Plugin> condition;
  @Getter
  private Dependency dependency;
  @Getter
  private State state = State.PENDING;
  private final String name;
  private final boolean isRequired;

  public DependencyLoader(String name, Function<PathPlugin, Dependency> factory,
                          boolean isRequired) {
    this(name, factory, isRequired, plugin -> true);
  }

  public DependencyLoader(String name, Function<PathPlugin, Dependency> factory, boolean isRequired,
                          Predicate<Plugin> condition) {
    this.name = name;
    this.isRequired = isRequired;
    this.factory = factory;
    this.condition = condition;
    this.logger = PathPlugin.getInstance().getLogger();
  }

  public void enable() {
    state = State.PENDING;
    try {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(this.name);
      if (plugin == null) {
        if (!isRequired) {
          return;
        } else {
          throw new Exception("Dependency not found");
        }
      }
      if (!condition.test(plugin)) {
        if (!isRequired) {
          return;
        } else {
          throw new Exception("Dependency conditions not met");
        }
      }

      this.dependency = factory.apply(PathPlugin.getInstance());
      logger.log(Level.INFO, "Enabling dependency '" + this.name + "'.");

      this.dependency = factory.apply(PathPlugin.getInstance());
      this.state = State.LOADED;
      logger.log(Level.INFO,
          "Successfully enabled dependency '" + this.name + "'.");
    } catch (Throwable t) {
      this.state = State.FAILED;
      this.dependency = null;
      this.logger.log(Level.SEVERE, "Could not enable dependency.", t);
    }
  }

  public enum State {
    PENDING, LOADED, FAILED, NOT_FOUND
  }
}
