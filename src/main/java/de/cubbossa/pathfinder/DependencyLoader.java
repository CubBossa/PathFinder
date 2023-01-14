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

  public DependencyLoader(Function<PathPlugin, Dependency> factory) {
    this(factory, plugin -> true);
  }

  public DependencyLoader(Function<PathPlugin, Dependency> factory, Predicate<Plugin> condition) {
    this.factory = factory;
    this.condition = condition;
    this.logger = PathPlugin.getInstance().getLogger();
  }

  public void enable() {
    state = State.PENDING;
    try {
      this.dependency = factory.apply(PathPlugin.getInstance());
      logger.log(Level.INFO, "Enabling dependency '" + this.dependency.getName() + "'.");

      Plugin plugin = Bukkit.getPluginManager().getPlugin(this.dependency.getName());
      if (plugin == null) {
        throw new Exception("Dependency not found");
      }
      if (!condition.test(plugin)) {
        throw new Exception("Dependency conditions not met");
      }
      this.dependency = factory.apply(PathPlugin.getInstance());
      this.state = State.LOADED;
      logger.log(Level.INFO,
          "Successfully enabled dependency '" + this.dependency.getName() + "'.");
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
