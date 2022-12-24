package de.cubbossa.pathfinder;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;
import java.util.function.Predicate;

public class DependencyLoader {

	public enum State {
		PENDING, LOADED, FAILED, NOT_FOUND
	}

	@Getter
	private Dependency dependency;
	@Getter
	private State state = State.PENDING;
	private final Function<PathPlugin, Dependency> factory;
	private final Predicate<Plugin> condition;

	public DependencyLoader(Function<PathPlugin, Dependency> factory) {
		this(factory, plugin -> true);
	}

	public DependencyLoader(Function<PathPlugin, Dependency> factory, Predicate<Plugin> condition) {
		this.factory = factory;
		this.condition = condition;
	}

	public void enable() {
		state = State.PENDING;
		try {
			Plugin plugin = Bukkit.getPluginManager().getPlugin(this.dependency.getName());
			if (plugin == null) {
				throw new Exception("Dependency not found");
			}
			if (!condition.test(plugin)) {
				throw new Exception("Dependency conditions not met");
			}
			this.dependency = factory.apply(PathPlugin.getInstance());
			this.state = State.LOADED;
		} catch (Throwable t) {
			this.state = State.FAILED;
			this.dependency = null;
		}
	}
}
