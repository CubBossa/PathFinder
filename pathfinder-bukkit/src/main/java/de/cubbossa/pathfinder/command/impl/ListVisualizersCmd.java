package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.CustomArgs;
import de.cubbossa.pathfinder.command.PathFinderSubCommand;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.util.BukkitUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class ListVisualizersCmd extends PathFinderSubCommand {
  public ListVisualizersCmd(PathFinder pathFinder) {
    super(pathFinder, "listvisualizers");

    withPermission(PathPerms.PERM_CMD_PV_LIST);
    executes((commandSender, objects) -> {
      onList(commandSender, Pagination.page(0, 10));
    });
    then(CustomArgs.pagination(10)
        .displayAsOptional()
        .executes((commandSender, objects) -> {
          onList(commandSender, objects.getUnchecked(0));
        })
    );
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

            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_VIS_LIST_ENTRY.formatted(r));
          },
          Messages.CMD_VIS_LIST_HEADER,
          Messages.CMD_VIS_LIST_FOOTER);
    });
  }
}
