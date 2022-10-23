package de.cubbossa.pathfinder.core;

import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.data.ExamplesReader;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.StringReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ExamplesHandler {

	@Getter
	private static ExamplesHandler instance;
	private static final String LINK = "https://api.github.com/repos/CubBossa/PathFinder/contents/examples";

	private ExamplesReader reader;
	@Getter
	private boolean fetched = false;
	private final List<ExamplesReader.ExampleFile> files = new ArrayList<>();
	private final Collection<Runnable> afterFetch = new HashSet<>();

	public ExamplesHandler() {
		instance = this;
		reader = new ExamplesReader();
	}

	public void fetchExamples() {
		reader.getExamples(LINK).thenAccept(exampleFiles -> {
			this.files.addAll(exampleFiles);
			fetched = true;
			afterFetch.forEach(Runnable::run);
			afterFetch.clear();
		});
	}

	public void afterFetch(Runnable runnable) {
		if (fetched) {
			runnable.run();
			return;
		}
		this.afterFetch.add(runnable);
	}

	public List<ExamplesReader.ExampleFile> getExamples() {
		return files;
	}

	public CompletableFuture<PathVisualizer<?, ?>> loadVisualizer(ExamplesReader.ExampleFile file) {
		return reader.read(file.fetchUrl()).thenApply(s -> {
			Map<String, Object> values = YamlConfiguration.loadConfiguration(new StringReader(s)).getValues(false);
			VisualizerType<?> type = VisualizerHandler.getInstance().getVisualizerType(NamespacedKey.fromString((String) values.get("type")));
			if (type == null) {
				PathPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occured while parsing type: " + values.get("type") + " from " + values);
				return null;
			}
			return parse(file, type, values);
		});
	}

	private <T extends PathVisualizer<T, ?>> PathVisualizer<T, ?> parse(ExamplesReader.ExampleFile file, VisualizerType<T> type, Map<String, Object> values) {

		NamespacedKey name = NamespacedKey.fromString(file.name().replace(".yml", "").replace("$", ":"));
		String displayName = (String) values.get("display-name");

		T visualizer = type.create(name, displayName);
		type.deserialize(visualizer, values);
		return visualizer;
	}
}
