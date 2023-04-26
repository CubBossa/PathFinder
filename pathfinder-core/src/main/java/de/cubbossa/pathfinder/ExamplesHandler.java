package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.storage.ExamplesReader;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public class ExamplesHandler {

  private static final String LINK =
      "https://api.github.com/repos/CubBossa/PathFinder/contents/examples";
  @Getter
  private static ExamplesHandler instance;
  private final List<ExamplesReader.ExampleFile> files = new ArrayList<>();
  private final Collection<Runnable> afterFetch = new HashSet<>();
  private final ExamplesReader reader;
  @Getter
  private boolean fetched = false;

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

  public CompletableFuture<PathVisualizer<?, ?>> loadVisualizer(
      ExamplesReader.ExampleFile file) {
    return reader.read(file.fetchUrl()).thenApply(s -> {
      Map<String, Object> values =
          YamlConfiguration.loadConfiguration(new StringReader(s)).getValues(false);
      AbstractVisualizerType<?> type = VisualizerHandler.getInstance()
          .getVisualizerType(NamespacedKey.fromString((String) values.get("type")));
      if (type == null) {
        PathPlugin.getInstance().getLogger().log(Level.SEVERE,
            "An error occurred while parsing type: " + values.get("type") + " from " + values);
        return null;
      }
      return parse(file, type, values);
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
