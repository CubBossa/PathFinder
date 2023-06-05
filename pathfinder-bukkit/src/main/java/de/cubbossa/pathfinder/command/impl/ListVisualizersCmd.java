package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.CollectionUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ListVisualizersCmd extends PathFinderSubCommand {
  public ListVisualizersCmd(PathFinder pathFinder) {
    super(pathFinder, "listvisualizers");

    withPermission(PathPerms.PERM_CMD_PV_LIST);
    executes((commandSender, objects) -> {
      onList(commandSender, Pagination.page(0, 10));
    });
    then(CustomArgs.pagination(10)
        .executes((commandSender, objects) -> {
          onList(commandSender, objects.getUnchecked(0));
        })
    );
  }

  public void onList(CommandSender sender, Pagination pagination) {
    getPathfinder().getStorage().loadVisualizers().thenAccept(pathVisualizers -> {
      getPathfinder().getStorage().loadVisualizerTypes(pathVisualizers.stream()
          .map(Keyed::getKey).collect(Collectors.toList())).thenAccept(map -> {

        //TODO pagination in load
        CommandUtils.printList(sender, pagination,
            CollectionUtils.subList(new ArrayList<>(pathVisualizers), pagination),
            visualizer -> {
              TagResolver r = TagResolver.builder()
                  .resolver(Messages.formatter().namespacedKey("key", visualizer.getKey()))
                  .resolver(Messages.formatter().namespacedKey("type", map.get(visualizer.getKey()).getKey()))
                  .build();

              BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_LIST_ENTRY.formatted(r));
            },
            Messages.CMD_VIS_LIST_HEADER,
            Messages.CMD_VIS_LIST_FOOTER);
      });
    });
  }
}
