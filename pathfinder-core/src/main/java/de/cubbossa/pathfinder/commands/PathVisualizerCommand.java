package de.cubbossa.pathfinder.commands;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import de.cubbossa.pathfinder.util.CommandUtils;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public class PathVisualizerCommand extends Command {

  public PathVisualizerCommand(PathFinder pathFinder) {
    super(pathFinder, "pathvisualizer");
    withAliases("visualizer");
    withGeneratedHelp();

    then(CustomArgs.literal("list")
        .withPermission(PathPerms.PERM_CMD_PV_LIST)
        .executes((commandSender, objects) -> {
          onList(commandSender, Pagination.page(0, 10));
        })
        .then(CustomArgs.pagination(10)
            .displayAsOptional()
            .executes((commandSender, objects) -> {
              onList(commandSender, (Pagination) objects[0]);
            })));

    then(CustomArgs.literal("create")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PV_CREATE)
        .then(CustomArgs.visualizerTypeArgument("type")
            .then(new StringArgument("key")
                .executes((commandSender, objects) -> {
                  onCreate(commandSender,
                      (VisualizerType<? extends PathVisualizer<?, ?, ?>>) objects[0],
                      PathPlugin.pathfinder((String) objects[1]));
                }))));

    then(CustomArgs.literal("delete")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PV_DELETE)
        .then(CustomArgs.pathVisualizerArgument("visualizer")
            .executes((commandSender, objects) -> {
              onDelete(commandSender, (PathVisualizer<?, ?, ?>) objects[0]);
            })));

    then(CustomArgs.literal("info")
        .withGeneratedHelp()
        .withPermission(PathPerms.PERM_CMD_PV_INFO)
        .then(CustomArgs.pathVisualizerArgument("visualizer")
            .executes((commandSender, objects) -> {
              onInfo(commandSender, (PathVisualizer<?, ?, ?>) objects[0]);
            })));

    then(new VisualizerImportCommand(pathFinder, "import", 0));
  }

  @Override
  public void register() {

    Argument<String> lit = CustomArgs.literal("edit");
    for (VisualizerType<? extends PathVisualizer<?, ?, ?>> type : VisualizerHandler.getInstance()
        .getVisualizerTypes()) {

      if (!(type instanceof VisualizerTypeCommandExtension cmdExt)) {
        continue;
      }

      ArgumentTree typeArg = CustomArgs.pathVisualizerArgument("visualizer", type);
      cmdExt.appendEditCommand(typeArg, 0, 1);

      typeArg.then(CustomArgs.literal("name")
          .withPermission(PathPerms.PERM_CMD_PV_SET_NAME)
          .then(CustomArgs.miniMessageArgument("name")
              .executes((commandSender, objects) -> {
                if (objects[0] instanceof PathVisualizer<?, ?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(commandSender, visualizer, (String) objects[1], "name", true,
                          visualizer::getNameFormat, visualizer::setNameFormat);
                }
              })));
      typeArg.then(CustomArgs.literal("permission")
          .withPermission(PathPerms.PERM_CMD_PV_SET_PERMISSION)
          .then(new GreedyStringArgument("permission")
              .executes((commandSender, objects) -> {
                if (objects[0] instanceof PathVisualizer<?, ?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(commandSender, visualizer, (String) objects[1], "permission",
                          true,
                          visualizer::getPermission, visualizer::setPermission,
                          Messages::formatPermission);
                }
              })));
      typeArg.then(CustomArgs.literal("interval")
          .withPermission(PathPerms.PERM_CMD_PV_INTERVAL)
          .then(CustomArgs.integer("ticks", 1)
              .executes((commandSender, objects) -> {
                if (objects[0] instanceof PathVisualizer<?, ?, ?> visualizer) {
                  VisualizerHandler.getInstance()
                      .setProperty(commandSender, visualizer, (Integer) objects[1], "interval",
                          true,
                          visualizer::getInterval, visualizer::setInterval, Formatter::number);
                }
              })));

      lit.then(new LiteralArgument(type.getCommandName()).then(typeArg));
    }
    then(lit).withPermission(PathPerms.PERM_CMD_PV_EDIT);
    super.register();
  }

  public void onList(CommandSender sender, Pagination pagination) {
    getPathfinder().getStorage().loadVisualizers().thenAccept(pathVisualizers -> {
      //TODO pagination in load
      CommandUtils.printList(sender, pagination,
          pag -> new ArrayList<>(pathVisualizers).subList(pag.getStart(), pag.getEndExclusive()),
          visualizer -> {
            TagResolver r = TagResolver.builder()
                .tag("key", Messages.formatKey(visualizer.getKey()))
                .resolver(Placeholder.component("name", visualizer.getDisplayName()))
                .resolver(
                    Placeholder.component("name-format",
                        Component.text(visualizer.getNameFormat())))
                .resolver(Placeholder.component("type", Component.text(visualizer.getNameFormat())))
                .build();

            TranslationHandler.getInstance()
                .sendMessage(Messages.CMD_VIS_LIST_ENTRY.format(r), sender);
          },
          Messages.CMD_VIS_LIST_HEADER,
          Messages.CMD_VIS_LIST_FOOTER);
    });
  }

  public void onCreate(CommandSender sender, VisualizerType<? extends PathVisualizer<?, ?, ?>> type,
                       NamespacedKey key) {

    Optional<?> opt = getPathfinder().getStorage().loadVisualizer(key).join();
    if (opt.isPresent()) {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_NAME_EXISTS, sender);
      return;
    }
    getPathfinder().getStorage().createAndLoadVisualizer(type, key).thenAccept(visualizer -> {
      TranslationHandler.getInstance()
          .sendMessage(Messages.CMD_VIS_CREATE_SUCCESS.format(TagResolver.builder()
              .tag("key", Messages.formatKey(visualizer.getKey()))
              .resolver(Placeholder.component("name", visualizer.getDisplayName()))
              .resolver(
                  Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
              .resolver(Placeholder.component("type",
                  Component.text(visualizer.getType().getCommandName())))
              .build()), sender);
    });
  }

  public void onDelete(CommandSender sender, PathVisualizer<?, ?, ?> visualizer) {
    getPathfinder().getStorage().deleteVisualizer(visualizer).thenRun(() -> {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_SUCCESS
          .format(TagResolver.builder()
              .tag("key", Messages.formatKey(visualizer.getKey()))
              .resolver(Placeholder.component("name", visualizer.getDisplayName()))
              .resolver(
                  Placeholder.component("name-format", Component.text(visualizer.getNameFormat())))
              .build()), sender);
    }).exceptionally(throwable -> {
      TranslationHandler.getInstance().sendMessage(Messages.CMD_VIS_DELETE_ERROR, sender);
      getPathfinder().getLogger().log(Level.WARNING, "Could not delete visualizer", throwable);
      return null;
    });
  }

  public <T extends PathVisualizer<T, ?, ?>> void onInfo(CommandSender sender, PathVisualizer<T, ?, ?> visualizer) {
    if (!(visualizer.getType() instanceof VisualizerTypeMessageExtension<?> msgExt)) {
      return;
    }
    // can be safely assumed, the type was extracted from the visualizer
    VisualizerTypeMessageExtension<T> cast = (VisualizerTypeMessageExtension<T>) msgExt;

    FormattedMessage message =
        cast.getInfoMessage((T) visualizer).format(TagResolver.builder()
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
