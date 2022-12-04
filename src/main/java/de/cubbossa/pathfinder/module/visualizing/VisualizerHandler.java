package de.cubbossa.pathfinder.module.visualizing;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerCreatedEvent;
import de.cubbossa.pathfinder.module.visualizing.events.VisualizerPropertyChangedEvent;
import de.cubbossa.pathfinder.module.visualizing.visualizer.*;
import de.cubbossa.pathfinder.util.HashedRegistry;
import de.cubbossa.pathfinder.util.StringUtils;
import de.cubbossa.translations.TranslationHandler;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
public class VisualizerHandler {

	public static final VisualizerType<ParticleVisualizer> PARTICLE_VISUALIZER_TYPE = new ParticleVisualizerType(new NamespacedKey(PathPlugin.getInstance(), "particle"));
	public static final VisualizerType<ScriptLineParticleVisualizer> ADV_PARTICLE_VISUALIZER_TYPE = new ScriptLineParticleVisualizerType(new NamespacedKey(PathPlugin.getInstance(), "scriptline"));
	public static final VisualizerType<CombinedVisualizer> COMBINED_VISUALIZER_TYPE = new CombinedVisualizerType(new NamespacedKey(PathPlugin.getInstance(), "combined"));
	public static final VisualizerType<CompassVisualizer> COMPASS_VISUALIZER_TYPE = new CompassVisualizerType(new NamespacedKey(PathPlugin.getInstance(), "compass"));

	@Getter
	private static VisualizerHandler instance;

	private final HashedRegistry<VisualizerType<?>> visualizerTypes;

	private final HashedRegistry<PathVisualizer<?, ?>> pathVisualizerMap;

	// Map<Player, Map<RoadMap, PathVisualizer>>
	private final Map<UUID, Map<NamespacedKey, PathVisualizer<?, ?>>> playerVisualizers;
	private final Map<Integer, HashedRegistry<PathVisualizer<?, ?>>> roadmapVisualizers;

	public VisualizerHandler() {
		instance = this;

		this.visualizerTypes = new HashedRegistry<>();
		visualizerTypes.put(PARTICLE_VISUALIZER_TYPE);
		visualizerTypes.put(COMBINED_VISUALIZER_TYPE);
		visualizerTypes.put(ADV_PARTICLE_VISUALIZER_TYPE);
		visualizerTypes.put(COMPASS_VISUALIZER_TYPE);

		this.pathVisualizerMap = new HashedRegistry<>();
		pathVisualizerMap.putAll(PathPlugin.getInstance().getDatabase().loadPathVisualizer());
		this.playerVisualizers = new HashMap<>();
		this.roadmapVisualizers = new HashMap<>();
	}

	public @Nullable <T extends PathVisualizer<T, ?>> VisualizerType<T> getVisualizerType(NamespacedKey key) {
		return (VisualizerType<T>) visualizerTypes.get(key);
	}

	public <T extends PathVisualizer<T, ?>> void registerVisualizerType(VisualizerType<T> type) {
		visualizerTypes.put(type);
	}

	public void unregisterVisualizerType(VisualizerType<?> type) {
		visualizerTypes.remove(type.getKey());
	}

	public @Nullable PathVisualizer<?, ?> getPathVisualizer(NamespacedKey key) {
		return pathVisualizerMap.get(key);
	}

	public void addPathVisualizer(PathVisualizer<?, ?> visualizer) {
		if (pathVisualizerMap.containsKey(visualizer.getKey())) {
			throw new IllegalArgumentException("Could not insert new path visualizer, another visualizer with key '" + visualizer.getKey() + "' already exists.");
		}
		PathPlugin.getInstance().getDatabase().updatePathVisualizer((PathVisualizer) visualizer);
		pathVisualizerMap.put(visualizer);

		Bukkit.getPluginManager().callEvent(new VisualizerCreatedEvent(visualizer));
	}

	public <T extends PathVisualizer<T, ?>> T createPathVisualizer(VisualizerType<T> type, NamespacedKey key) {
		return createPathVisualizer(type, key, StringUtils.insertInRandomHexString(StringUtils.capizalize(key.getKey())));
	}

	public <T extends PathVisualizer<T, ?>> T createPathVisualizer(VisualizerType<T> type, NamespacedKey key, String nameFormat) {

		if (pathVisualizerMap.containsKey(key)) {
			throw new IllegalArgumentException("Could not insert new path visualizer, another visualizer with key '" + key + "' already exists.");
		}
		T visualizer = type.create(key, nameFormat);
		PathPlugin.getInstance().getDatabase().updatePathVisualizer(visualizer);
		pathVisualizerMap.put(visualizer);

		Bukkit.getPluginManager().callEvent(new VisualizerCreatedEvent(visualizer));
		return visualizer;
	}

	public boolean deletePathVisualizer(PathVisualizer<?, ?> visualizer) {
		PathPlugin.getInstance().getDatabase().deletePathVisualizer(visualizer);
		return pathVisualizerMap.remove(visualizer.getKey()) != null;
	}

	public Stream<PathVisualizer<?, ?>> getPathVisualizerStream() {
		return pathVisualizerMap.values().stream();
	}

	public <V extends PathVisualizer<?, ?>, T> void setProperty(CommandSender sender, V visualizer, PathVisualizer.Property<V, T> prop, T val) {
		setProperty(sender, visualizer, val, prop.getKey(), prop.isVisible(), () -> prop.getValue(visualizer), v -> prop.setValue(visualizer, v));
	}

	public <T> void setProperty(CommandSender sender, PathVisualizer<?, ?> visualizer, T value, String property, boolean visual, Supplier<T> getter, Consumer<T> setter) {
		setProperty(sender, visualizer, value, property, visual, getter, setter, t -> Component.text(t.toString()));
	}

	public <T> void setProperty(CommandSender sender, PathVisualizer<?, ?> visualizer, T value, String property, boolean visual, Supplier<T> getter, Consumer<T> setter, Function<T, ComponentLike> formatter) {
		setProperty(sender, visualizer, value, property, visual, getter, setter, (s, t) -> Placeholder.component(s, formatter.apply(t)));
	}

	public <T> void setProperty(CommandSender sender, PathVisualizer<?, ?> visualizer, T value, String property, boolean visual, Supplier<T> getter, Consumer<T> setter, BiFunction<String, T, TagResolver> formatter) {
		T old = getter.get();
		setter.accept(value);
		Bukkit.getPluginManager().callEvent(new VisualizerPropertyChangedEvent<>(visualizer, property, visual, old, value));
		TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_SET_PROP.format(
				TagResolver.resolver("key", Messages.formatKey(visualizer.getKey())),
				Placeholder.component("name", visualizer.getDisplayName()),
				Placeholder.component("type", Component.text(visualizer.getType().getCommandName())),
				Placeholder.parsed("property", property),
				formatter.apply("old-value", old),
				formatter.apply("value", value)
		), sender);
	}
}
