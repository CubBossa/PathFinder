package de.cubbossa.pathfinder.examples;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizer;
import de.cubbossa.pathfinder.visualizer.AbstractVisualizerType;
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

  private static final String COMMON_REPO = "https://api.github.com/repos/CubBossa/PathFinder/contents/examples";

  private final String url;
  private final List<ExamplesFileReader.ExampleFile> files;
  private final ExamplesFileReader reader;
  private final VisualizerTypeRegistry registry;
  @Getter
  private boolean cached = false;

  public ExamplesLoader(VisualizerTypeRegistry registry, String url) {
    this.registry = registry;
    this.reader = new ExamplesFileReader();
    this.files = new ArrayList<>();
    this.url = url;
  }

  public ExamplesLoader(VisualizerTypeRegistry registry) {
    this(registry, COMMON_REPO);
  }

  public CompletableFuture<Collection<ExamplesFileReader.ExampleFile>> getExampleFiles() {
    if (!cached) {
      return reader.getExamples(url).thenApply(exampleFiles -> {
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
  CompletableFuture<Map.Entry<V, VisualizerType<V>>> loadVisualizer(ExamplesFileReader.ExampleFile file) {
    return reader.read(file.fetchUrl()).thenApply(s -> {
      Map<String, Object> values = YamlConfiguration.loadConfiguration(new StringReader(s)).getValues(false);
      String typeString = (String) values.get("type");
      Optional<VisualizerType<V>> type = registry.<V>getType(NamespacedKey.fromString(typeString));
      if (type.isEmpty()) {
        throw new RuntimeException(
            "Could not load visualizer of type '" + typeString + "'. Make sure that required PathFinder extensions are installed.");
      }
      return new AbstractMap.SimpleEntry<>(parse(file, type.get(), values), type.get());
    });
  }

  private <VisualizerT extends PathVisualizer<?, ?>> VisualizerT parse(
      ExamplesFileReader.ExampleFile file, VisualizerType<VisualizerT> type,
      Map<String, Object> values
  ) {
    NamespacedKey name = NamespacedKey.fromString(file.name()
        .replace(".yml", "")
        .replace("$", ":"));


    if (type instanceof AbstractVisualizerType abstractType) {
      VisualizerT visualizer = (VisualizerT) abstractType.createVisualizer(name);
      abstractType.deserialize((AbstractVisualizer<?, ?>) visualizer, values);
      return visualizer;
    } else {
      throw new RuntimeException("Only visualizers of a type that extends 'AbstractVisualizerType' can be loaded from yml files.");
    }
  }
}
