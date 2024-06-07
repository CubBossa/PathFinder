package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.group.DiscoverProgressModifier;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.Pagination;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.CollectionUtils;
import dev.jorel.commandapi.CommandTree;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscoveriesCommand extends CommandTree {

  private final PathFinder pathFinder;

  public DiscoveriesCommand() {
    super("discoveries");

    withPermission(PathPerms.PERM_CMD_DISCOVERIES);

    pathFinder = PathFinder.get();

    executesPlayer((sender, args) -> {
      printList(sender, Pagination.page(0, 10));
    });
    then(Arguments.pagination(10).executesPlayer((sender, args) -> {
      printList(sender, args.getUnchecked(0));
    }));
  }

  private void printList(Player sender, Pagination pagination) {
    pathFinder.getStorage().loadGroups(DiscoverProgressModifier.KEY).thenAccept(groups -> {
      List<Map.Entry<DiscoverProgressModifier, Double>> l = groups.stream()
          .map(group -> group.getModifier(DiscoverProgressModifier.KEY))
          .parallel()
          .filter(Optional::isPresent).map(Optional::get)
          .map(m -> (DiscoverProgressModifier) m)
          .map(modifier -> Map.entry(modifier, modifier.calculateProgress(sender.getUniqueId()).join()))
          .sorted(Comparator.comparingDouble(Map.Entry::getValue))
          .toList();

      PathPlayer<CommandSender> p = BukkitUtils.wrap(sender);
      CommandUtils.printList(sender, pagination,
          CollectionUtils.subList(l, pagination),
          e -> {
            p.sendMessage(Messages.CMD_DISCOVERIES_ENTRY.formatted(
                    Placeholder.component("name", e.getKey().getDisplayName()),
                    Messages.formatter().number("percentage", e.getValue() * 100),
                    Messages.formatter().number("ratio", e.getValue())
            ));
          },
          Messages.CMD_DISCOVERIES_HEADER,
          Messages.CMD_DISCOVERIES_FOOTER);
    });
  }
}
