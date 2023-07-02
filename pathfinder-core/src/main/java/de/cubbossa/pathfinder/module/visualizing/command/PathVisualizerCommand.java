package de.cubbossa.pathfinder.module.visualizing.command;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.commands.Command;
import de.cubbossa.pathfinder.core.commands.CustomArgs;
import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class PathVisualizerCommand extends Command {

  public PathVisualizerCommand() {
    super("pathvisualizer");
    withAliases("visualizer");
    withGeneratedHelp();

    then(CustomArgs.literal("list")
        .withPermission(PathPlugin.PERM_CMD_PV_LIST)
        .executes((commandSender, objects) -> {
          onList(commandSender, 1);
        })
        .then(CustomArgs.integer("page", 1)
            .displayAsOptional()
            .executes((commandSender, objects) -> {
              onList(commandSender, objects.getUnchecked(0));
            })));

    then(CustomArgs.literal("create")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_PV_CREATE)
        .then(CustomArgs.visualizerTypeArgument("type")
            .then(new StringArgument("key")
                .executes((commandSender, objects) -> {
                  onCreate(commandSender, (VisualizerType<?>) objects.get(0),
                      new NamespacedKey(PathPlugin.getInstance(), objects.<String>getUnchecked(1)));
                }))));

    then(CustomArgs.literal("delete")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_PV_DELETE)
        .then(CustomArgs.pathVisualizerArgument("visualizer")
            .executes((commandSender, objects) -> {
              onDelete(commandSender, (PathVisualizer<?, ?>) objects.get(0));
            })));

    then(CustomArgs.literal("info")
        .withGeneratedHelp()
        .withPermission(PathPlugin.PERM_CMD_PV_INFO)
        .then(CustomArgs.pathVisualizerArgument("visualizer")
            .executes((commandSender, objects) -> {
              onInfo(commandSender, (PathVisualizer<?, ?>) objects.get(0));
            })));

    then(new VisualizerImportCommand("import", 0));
  }

  @Override
  public void register() {

    Argument<String> lit = CustomArgs.literal("edit");
    for (VisualizerType<?> type : VisualizerHandler.getInstance().getVisualizerTypes()) {

      Argument<?> typeArg = CustomArgs.pathVisualizerArgument("visualizer", type);
      type.appendEditCommand(typeArg, 0, 1);

      typeArg.then(CustomArgs.literal("name")
          .withPermission(PathPlugin.PERM_CMD_PV_SET_NAME)
          .then(CustomArgs.miniMessageArgument("name")
              .executes((commandSender, objects) -> {
                if (objects.get(0) instanceof PathVisualizer<?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(commandSender, visualizer, objects.<String>getUnchecked(1), "name", true,
                          visualizer::getNameFormat, visualizer::setNameFormat);
                }
              })));
      typeArg.then(CustomArgs.literal("permission")
          .withPermission(PathPlugin.PERM_CMD_PV_SET_PERMISSION)
          .then(new GreedyStringArgument("permission")
              .executes((commandSender, objects) -> {
                if (objects.get(0) instanceof PathVisualizer<?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(commandSender, visualizer, objects.<String>getUnchecked(1), "permission",
                          true,
                          visualizer::getPermission, visualizer::setPermission,
                          Messages::formatPermission);
                }
              })));
      typeArg.then(CustomArgs.literal("interval")
          .withPermission(PathPlugin.PERM_CMD_PV_INTERVAL)
          .then(CustomArgs.integer("ticks", 1)
              .executes((commandSender, objects) -> {
                if (objects.get(0) instanceof PathVisualizer<?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(commandSender, visualizer, objects.<Integer>getUnchecked(1), "interval",
                          true,
                          visualizer::getInterval, visualizer::setInterval, Formatter::number);
                }
              })));

      lit.then(new LiteralArgument(type.getCommandName()).then(typeArg));
    }
    then(lit).withPermission(PathPlugin.PERM_CMD_PV_EDIT);
    super.register();
  }

  /**
   * @param page Begins with 1, not 0!
   */
  public void onList(CommandSender sender, int page) {

    CommandUtils.printList(sender, page, 10,
        new ArrayList<>(VisualizerHandler.getInstance().getPathVisualizerMap().values()),
        visualizer -> {
          TagResolver r = TagResolver.builder()
              .tag("key", Messages.formatKey(visualizer.getKey()))
              .resolver(Placeholder.component("name", visualizer.getDisplayName()))
              .resolver(
                  Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
              .resolver(Placeholder.component("type", Component.text(visualizer.getNameFormat())))
              .build();

          TranslationHandler.getInstance()
              .sendMessage(Messages.CMD_VIS_LIST_ENTRY.format(r), sender);
        },
        Messages.CMD_VIS_LIST_HEADER,
        Messages.CMD_VIS_LIST_FOOTER);
  }

  public void onCreate(CommandSender sender, VisualizerType<?> type, NamespacedKey key) {

    if (VisualizerHandler.getInstance().getPathVisualizer(key) != null) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_NAME_EXISTS, sender);
      return;
    }
    PathVisualizer<?, ?> visualizer =
        VisualizerHandler.getInstance().createPathVisualizer(type, key);

    TranslationHandler.getInstance()
        .sendMessage(Messages.CMD_VIS_CREATE_SUCCESS.format(TagResolver.builder()
            .tag("key", Messages.formatKey(visualizer.getKey()))
            .resolver(Placeholder.component("name", visualizer.getDisplayName()))
            .resolver(
                Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
            .resolver(Placeholder.component("type",
                Component.text(visualizer.getType().getCommandName())))
            .build()), sender);
  }

  public void onDelete(CommandSender sender, PathVisualizer<?, ?> visualizer) {
    if (!VisualizerHandler.getInstance().deletePathVisualizer(visualizer)) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_ERROR, sender);
      return;
    }
    TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_SUCCESS
        .format(TagResolver.builder()
            .tag("key", Messages.formatKey(visualizer.getKey()))
            .resolver(Placeholder.component("name", visualizer.getDisplayName()))
            .resolver(
                Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
            .build()), sender);
  }

  public <T extends PathVisualizer<T, ?>> void onInfo(CommandSender sender,
                                                      PathVisualizer<T, ?> visualizer) {

    FormattedMessage message =
        visualizer.getType().getInfoMessage((T) visualizer).format(TagResolver.builder()
            .tag("key", Messages.formatKey(visualizer.getKey()))
            .resolver(Placeholder.component("name", visualizer.getDisplayName()))
            .resolver(
                Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
            .resolver(Placeholder.component("type", Component.text(visualizer.getNameFormat())))
            .resolver(Placeholder.component("permission",
                Messages.formatPermission(visualizer.getPermission())))
            .resolver(Placeholder.component("interval", Component.text(visualizer.getInterval())))
            .build());

    TranslationHandler.getInstance().sendMessage(message, sender);
  }
}
