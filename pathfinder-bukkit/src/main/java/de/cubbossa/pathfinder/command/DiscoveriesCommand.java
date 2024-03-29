package de.cubbossa.pathfinder.command;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.DiscoverProgressModifier;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.util.ListSection;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DiscoveriesCommand extends CommandTree {

  private final PathFinder pathFinder;

  public DiscoveriesCommand() {
    super("discoveries");

    withPermission(PathPerms.PERM_CMD_DISCOVERIES);

    pathFinder = PathFinderProvider.get();

    executesPlayer((sender, args) -> {
      printList(sender, Pagination.page(0, 10));
    });
    then(Arguments.pagination(10).executesPlayer((sender, args) -> {
      printList(sender, args.getUnchecked(0));
    }));
  }

  private void printList(Player sender, Pagination pag) {
    pathFinder.getStorage().loadGroups(DiscoverProgressModifier.KEY).thenAccept(groups -> {
      List<DiscoverProgress> l = groups.stream()
          .map(group -> group.getModifier(DiscoverProgressModifier.KEY))
          .parallel()
          .filter(Optional::isPresent).map(Optional::get)
          .map(m -> (DiscoverProgressModifier) m)
          .map(modifier -> new DiscoverProgress(modifier, modifier.calculateProgress(sender.getUniqueId()).join()))
          .sorted()
          .toList();

      BukkitUtils.wrap(sender).sendMessage(Messages.CMD_DISCOVERIES_ENTRY.insertList(
          "discoveries", l, ListSection.paged(pag.getPage(), pag.getSize()), Collections.emptyList(),
          List.of(TinyObjectResolver.builder(DiscoverProgress.class)
              .with("name", e -> e.mod.getDisplayName())
              .with("percentage", e -> e.percent * 100)
              .with("ratio", DiscoverProgress::percent)
              .withFallback(e -> e.mod.getDisplayName())
              .build())));
    });
  }

  record DiscoverProgress(DiscoverProgressModifier mod, double percent) implements Comparable<DiscoverProgress> {
    @Override
    public int compareTo(@NotNull DiscoveriesCommand.DiscoverProgress o) {
      int r = Double.compare(percent, o.percent);
      if (r != 0) {
        return r;
      }
      return String.CASE_INSENSITIVE_ORDER.compare(mod.getKey().toString(), o.mod.getKey().toString());
    }
  }
}
