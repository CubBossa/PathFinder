package de.cubbossa.pathfinder.storage;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.StringReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExamplesLoader {

  // TODO use storage instance

  private static final String LINK =
      "https://api.github.com/repos/CubBossa/PathFinder/contents/examples";
  @Getter
  private static ExamplesLoader instance;
  private final List<ExamplesReader.ExampleFile> files = new ArrayList<>();
  private final Collection<Runnable> afterFetch = new HashSet<>();
  private final ExamplesReader reader;
  private final Logger logger;
  @Getter
    private boolean fetched = false;

  public ExamplesLoader(Logger logger) {
    instance = this;
    this.logger = logger;
    reader = new ExamplesReader(logger);
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

  public CompletableFuture<PathVisualizer<?, ?>> loadVisualizer(
      ExamplesReader.ExampleFile file) {
    return reader.read(file.fetchUrl()).thenApply(s -> {
      Map<String, Object> values =
          YamlConfiguration.loadConfiguration(new StringReader(s)).getValues(false);
      Optional<VisualizerType<PathVisualizer<?, ?>>> type = VisualizerHandler.getInstance()
          .getType(NamespacedKey.fromString((String) values.get("type")));
      if (type.isEmpty()) {
          logger.log(Level.SEVERE, "An error occurred while parsing type: " + values.get("type") + " from " + values);
        return null;
      }
      return parse(file, type.get(), values);
    });
  }

  private <VisualizerT extends PathVisualizer<?, ?>> VisualizerT parse(
      ExamplesReader.ExampleFile file, VisualizerType<VisualizerT> type,
      Map<String, Object> values) {

    NamespacedKey name =
        NamespacedKey.fromString(file.name().replace(".yml", "").replace("$", ":"));
    String displayName = (String) values.get("display-name");

    VisualizerT visualizer = type.create(name, displayName);
    type.deserialize(visualizer, values);
    return visualizer;
  }
}
