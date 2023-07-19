package de.cubbossa.pathfinder.examples;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public class ExamplesLoader {

  private static final String LINK = "https://api.github.com/repos/CubBossa/PathFinder/contents/examples";
  private final List<ExamplesReader.ExampleFile> files = new ArrayList<>();
  private final ExamplesReader reader;
  private final VisualizerTypeRegistry registry;
  @Getter
  private boolean cached = false;

  public ExamplesLoader(VisualizerTypeRegistry registry) {
    this.registry = registry;
    reader = new ExamplesReader();
  }

  public CompletableFuture<Collection<ExamplesReader.ExampleFile>> getExampleFiles() {
    if (!cached) {
      return reader.getExamples(LINK).thenApply(exampleFiles -> {
        this.files.addAll(exampleFiles);
        cached = true;
        return new HashSet<>(this.files);
      });
    } else {
      return CompletableFuture.completedFuture(new HashSet<>(this.files));
    }
  }

  public CompletableFuture<Collection<PathVisualizer<?, ?>>> getExamples() {
    return getExampleFiles()
        .thenApply(exampleFiles -> exampleFiles.stream().parallel()
            .map(this::loadVisualizer)
            .map(CompletableFuture::join)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet()));
  }

  public <V extends PathVisualizer<?, ?>>
  CompletableFuture<Map.Entry<V, VisualizerType<V>>> loadVisualizer(ExamplesReader.ExampleFile file) {
    return reader.read(file.fetchUrl()).thenApply(s -> {
      Map<String, Object> values = YamlConfiguration.loadConfiguration(new StringReader(s)).getValues(false);
      String typeString = (String) values.get("type");
      Optional<VisualizerType<V>> type = registry.<V>getType(NamespacedKey.fromString(typeString));
      if (type.isEmpty()) {
        throw new RuntimeException(
            "Could not parse visualizer of type '" + typeString + "'. Are you missing some additional plugins?");
      }
      return new AbstractMap.SimpleEntry<>(parse(file, type.get(), values), type.get());
    });
  }

  private <VisualizerT extends PathVisualizer<?, ?>> VisualizerT parse(
      ExamplesReader.ExampleFile file, VisualizerType<VisualizerT> type,
      Map<String, Object> values
  ) {
    NamespacedKey name = NamespacedKey.fromString(file.name()
        .replace(".yml", "")
        .replace("$", ":"));

    VisualizerT visualizer = type.create(name);
    type.deserialize(visualizer, values);
    return visualizer;
  }
}
