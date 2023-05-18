package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.storage.ExamplesLoader;
import de.cubbossa.pathfinder.storage.ExamplesReader;
import de.cubbossa.pathfinder.util.BukkitUtils;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ImportVisualizerCmd extends PathFinderSubCommand {

  public ImportVisualizerCmd(PathFinder pathFinder) {
    super(pathFinder, "importvisualizer");
    withPermission(PathPerms.PERM_CMD_PF_IMPORT_VIS);
    withGeneratedHelp();

    then(new GreedyStringArgument("name")
        .replaceSuggestions((suggestionInfo, suggestionsBuilder) -> {
          ExamplesLoader.getInstance().getExamples().stream()
              .map(ExamplesReader.ExampleFile::name)
              .forEach(suggestionsBuilder::suggest);
          suggestionsBuilder.suggest("*");
          return suggestionsBuilder.buildFuture();
        })
        .executes((commandSender, objects) -> {
          if (objects.<String>getUnchecked(0).equals("*")) {
            ExamplesLoader eh = ExamplesLoader.getInstance();
            eh.getExamples().stream().map(eh::loadVisualizer).forEach(future ->
                future.thenAccept(visualizer -> {
                }));
            // pathFinder.getStorage().createAndLoadVisualizer(visualizer)));
            // TODO handle double names
            return;
          }
          ExamplesReader.ExampleFile file = ExamplesLoader.getInstance().getExamples().stream()
              .filter(f -> f.name().equalsIgnoreCase(objects.getUnchecked(0))).findFirst()
              .orElse(null);

          if (file == null) {
            BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_IMPORT_NOT_EXISTS);
            return;
          }
          NamespacedKey key =
              NamespacedKey.fromString(file.name().replace(".yml", "").replace("$", ":"));
          if (pathFinder.getStorage().loadVisualizer(key) != null) {
            BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_IMPORT_EXISTS);
            return;
          }
          ExamplesLoader.getInstance().loadVisualizer(file).thenAccept(visualizer -> {
            // pathFinder.getStorage().createAndLoadVisualizer(visualizer);
            BukkitUtils.wrap(commandSender).sendMessage(Messages.CMD_VIS_IMPORT_SUCCESS.formatted(
                TagResolver.resolver("key", Messages.formatKey(key)),
                Placeholder.component("name", visualizer.getDisplayName())
            ));
          });
        })
    );
  }
}
