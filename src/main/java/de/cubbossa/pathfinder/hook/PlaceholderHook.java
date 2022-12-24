package de.cubbossa.pathfinder.hook;

import de.cubbossa.pathfinder.Dependency;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PlaceholderVisualizer;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.ArgumentTree;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class PlaceholderHook extends PlaceholderExpansion implements Dependency {

	public static final VisualizerType<PlaceholderVisualizer> PLACEHOLDER_VISUALIZER_TYPE = new VisualizerType<PlaceholderVisualizer>(
			new NamespacedKey(PathPlugin.getInstance(), "placeholderapi")
	) {
		@Override
		public PlaceholderVisualizer create(NamespacedKey key, String nameFormat) {
			return new PlaceholderVisualizer(key, nameFormat);
		}

		@Override
		public Message getInfoMessage(PlaceholderVisualizer element) {
			return null;
		}

		@Override
		public ArgumentTree appendEditCommand(ArgumentTree tree, int visualizerIndex, int argumentOffset) {
			Arrays.stream(PlaceholderVisualizer.PROPS).forEach(prop -> {
				tree.then(subCommand(prop.getKey(), CustomArgs.miniMessageArgument("format",
						Objects.equals(prop.getKey(), PlaceholderVisualizer.PROP_DISTANCE.getKey()) ? i -> Set.of("distance") : i -> new HashSet<>()), prop));
			});
			return tree;
		}

		@Override
		public Map<String, Object> serialize(PlaceholderVisualizer visualizer) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<>();
			Arrays.stream(PlaceholderVisualizer.PROPS).forEach(prop -> serialize(map, prop, visualizer));
			return super.serialize(visualizer);
		}

		@Override
		public void deserialize(PlaceholderVisualizer visualizer, Map<String, Object> values) {
			Arrays.stream(PlaceholderVisualizer.PROPS).forEach(prop -> loadProperty(values, visualizer, prop));
		}
	};

	public static final String DIRECTION = "direction";
	public static final String DISTANCE = "distance";

	@Getter
	private static PlaceholderHook instance;
	private final PathPlugin plugin;

	private final Map<String, Map<OfflinePlayer, Supplier<String>>> resolvers = new HashMap<>();

	public PlaceholderHook(PathPlugin plugin) {
		instance = this;
		this.plugin = plugin;

		VisualizerHandler.getInstance().registerVisualizerType(PLACEHOLDER_VISUALIZER_TYPE);
	}

	public void register(String key, OfflinePlayer player, Supplier<String> placeholder) {
		resolvers.computeIfAbsent(key, s -> new HashMap<>()).put(player, placeholder);
	}

	@Override
	public @NotNull String getIdentifier() {
		return plugin.getName();
	}

	@Override
	public @NotNull String getAuthor() {
		return String.join(", ", plugin.getDescription().getAuthors());
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
		if (player == null) {
			return "";
		}
		if (params.equals("")) {
			return "";
		}
		var map = resolvers.get(params);
		if (map == null) {
			// unknown placeholder
			return null;
		}
		return map.getOrDefault(player, () -> "").get();
	}
}
