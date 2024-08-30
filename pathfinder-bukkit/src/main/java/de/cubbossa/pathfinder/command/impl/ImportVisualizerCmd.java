package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.examples.ExamplesFileReader;
import de.cubbossa.pathfinder.examples.ExamplesLoader;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.NamespacedKey;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.visualizer.VisualizerType;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;

public class ImportVisualizerCmd extends PathFinderSubCommand {

  private final ExamplesLoader loader;

  public ImportVisualizerCmd(PathFinder pathFinder) {
    super(pathFinder, "importvisualizer");
    withPermission(PathPerms.PERM_CMD_PF_IMPORT_VIS);
    withGeneratedHelp();
    
    loader = new ExamplesLoader(pathFinder.getVisualizerTypeRegistry());

    then(new GreedyStringArgument("name")
        .replaceSuggestions((suggestionInfo, suggestionsBuilder) -> {
          return loader.getExampleFiles().thenApply(files -> {
            files.stream()
                .map(ExamplesFileReader.ExampleFile::name)
                .forEach(suggestionsBuilder::suggest);
            suggestionsBuilder.suggest("*");
            return suggestionsBuilder.build();
          });
        })
        .executes((commandSender, objects) -> {
          if (Objects.equals(objects.<String>getUnchecked(0), "*")) {
            loader.getExampleFiles().thenAccept(files -> files
                .forEach(exampleFile -> importVisualizer(commandSender, exampleFile)));
            return;
          }
          loader.getExampleFiles()
              .thenApply(files -> files.stream().filter(f -> f.name().equalsIgnoreCase(objects.getUnchecked(0))).findFirst())
              .thenCompose(exampleFile -> importVisualizer(commandSender, exampleFile.orElse(null)))
              .exceptionally(throwable -> {
                BukkitUtils.wrap(commandSender).sendMessage(Messages.GEN_ERROR.insert("cause", throwable));
                return null;
              });
        })
    );
  }

  private CompletableFuture<Void> importVisualizer(CommandSender commandSender, ExamplesFileReader.ExampleFile exampleFile) {
    if (exampleFile == null) {
      BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_IMPORT_NOT_EXISTS);
      return CompletableFuture.completedFuture(null);
    }
    NamespacedKey key = NamespacedKey.fromString(exampleFile.name().replace(".yml", "").replace("$", ":"));
    return getPathfinder().getStorage().loadVisualizer(key).thenCompose(pathVisualizer -> {
      if (pathVisualizer.isPresent()) {
        BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_IMPORT_EXISTS);
        return CompletableFuture.completedFuture(null);
      }
      return loader
          .loadVisualizer(exampleFile)
          .thenCompose(v -> save(v.getValue(), v.getKey()))
          .thenAccept(visualizer -> {
            BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_IMPORT_SUCCESS
                .insert("vis", visualizer)
                .insert("visualizer", visualizer));
          })
          .exceptionally(throwable -> {
            BukkitUtils.wrap(commandSender).sendMessage(Messages.GEN_ERROR
                .insert("cause", throwable));
            return null;
          });
    });
  }

  private <V extends PathVisualizer<?, ?>> CompletableFuture<V> save(VisualizerType<V> type, V vis) {
    return getPathfinder().getStorage()
        .createAndLoadVisualizer(type, vis.getKey())
        .thenCompose(v -> getPathfinder().getStorage().saveVisualizer(vis).thenApply(u -> v));
  }
}
