package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.ExamplesHandler;
import de.cubbossa.pathfinder.core.commands.CustomLiteralArgument;
import de.cubbossa.pathfinder.data.ExamplesReader;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;

public class VisualizerImportCommand extends CustomLiteralArgument {

  public VisualizerImportCommand(String literal, int argumentOffset) {
    super(literal);
    withPermission(PathPlugin.PERM_CMD_PF_IMPORT);
    withGeneratedHelp();

    then(new GreedyStringArgument("name")
        .replaceSuggestions((suggestionInfo, suggestionsBuilder) -> {
          ExamplesHandler.getInstance().getExamples().stream()
              .map(ExamplesReader.ExampleFile::name).forEach(suggestionsBuilder::suggest);
          suggestionsBuilder.suggest("*");
          return suggestionsBuilder.buildFuture();
        })
        .executes((commandSender, objects) -> {
          if (objects[argumentOffset].equals("*")) {
            ExamplesHandler eh = ExamplesHandler.getInstance();
            eh.getExamples().stream().map(eh::loadVisualizer).forEach(future ->
                future.thenAccept(visualizer ->
                    VisualizerHandler.getInstance().addPathVisualizer(visualizer)));
            // TODO handle double names
            return;
          }
          ExamplesReader.ExampleFile file = ExamplesHandler.getInstance().getExamples().stream()
              .filter(f -> f.name().equalsIgnoreCase((String) objects[argumentOffset])).findFirst()
              .orElse(null);

          if (file == null) {
            TranslationHandler.getInstance()
                .sendMessage(Messages.CMD_VIS_IMPORT_NOT_EXISTS, commandSender);
            return;
          }
          NamespacedKey key =
              NamespacedKey.fromString(file.name().replace(".yml", "").replace("$", ":"));
          if (VisualizerHandler.getInstance().getPathVisualizer(key) != null) {
            TranslationHandler.getInstance()
                .sendMessage(Messages.CMD_VIS_IMPORT_EXISTS, commandSender);
            return;
          }
          ExamplesHandler.getInstance().loadVisualizer(file).thenAccept(visualizer -> {
            VisualizerHandler.getInstance().addPathVisualizer(visualizer);
            TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_IMPORT_SUCCESS.format(
                TagResolver.resolver("key", Messages.formatKey(key)),
                Placeholder.component("name", visualizer.getDisplayName())
            ), commandSender);
          });
        })
    );
  }
}
