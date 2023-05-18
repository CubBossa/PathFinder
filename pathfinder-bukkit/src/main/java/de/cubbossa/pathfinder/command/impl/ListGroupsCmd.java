package de.cubbossa.pathfinder.command.impl;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
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

import java.util.stream.Collectors;

public class ListGroupsCmd extends PathFinderSubCommand {
  public ListGroupsCmd(PathFinder pathFinder) {
    super(pathFinder, "listgroups");

    withPermission(PathPerms.PERM_CMD_NG_LIST);
    executes((sender, objects) -> {
      listGroups(sender, Pagination.page(0, 10));
    });
    then(CustomArgs.pagination(10)
        .displayAsOptional()
        .executes((sender, args) -> {
          listGroups(sender, args.getUnchecked(0));
        })
    );
  }

  private void listGroups(CommandSender sender, Pagination pagination) {
    getPathfinder().getStorage().loadAllGroups().thenAccept(nodeGroups -> {
      sender.sendMessage(nodeGroups.stream().map(NodeGroup::getKey).map(NamespacedKey::getKey)
          .collect(Collectors.joining(", ")));
    });
    getPathfinder().getStorage().loadGroups(pagination).thenApply(nodeGroups -> {
      CommandUtils.printList(
          sender,
          pagination,
          p -> getPathfinder().getStorage().loadGroups(p).join().stream().toList(),
          group -> {
            TagResolver r = TagResolver.builder()
                .resolver(Placeholder.component("key", Component.text(group.getKey().toString())))
                .resolver(Placeholder.component("size", Component.text(group.size())))
                .build();
            BukkitUtils.wrap(sender).sendMessage(Messages.CMD_NG_LIST_LINE.formatted(r));
          },
          Messages.CMD_NG_LIST_HEADER,
          Messages.CMD_NG_LIST_FOOTER
      );
      return nodeGroups;
    });
  }
}
