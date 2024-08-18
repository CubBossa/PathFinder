package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.Arguments;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.Keyed;
import de.cubbossa.pathfinder.misc.Pagination;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.tinytranslations.util.ListSection;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

public class ListVisualizersCmd extends PathFinderSubCommand {
  public ListVisualizersCmd(PathFinder pathFinder) {
    super(pathFinder, "listvisualizers");

    withPermission(PathPerms.PERM_CMD_PV_LIST);
    executes((commandSender, objects) -> {
      onList(commandSender, Pagination.page(0, 10));
    });
    then(Arguments.pagination(10)
        .executes((commandSender, objects) -> {
          onList(commandSender, objects.getUnchecked(0));
        })
    );
  }

  public void onList(CommandSender sender, Pagination pagination) {
    getPathfinder().getStorage().loadVisualizers().thenAccept(pathVisualizers -> {
      getPathfinder().getStorage().loadVisualizerTypes(pathVisualizers.stream()
          .map(Keyed::getKey).collect(Collectors.toList())).thenAccept(map -> {

        PathPlayer.wrap(sender).sendMessage(Messages.CMD_VIS_LIST
            .insertList("visualizers", pathVisualizers, ListSection.paged(pagination.getPage(), pagination.getSize())));
      });
    });
  }
}
