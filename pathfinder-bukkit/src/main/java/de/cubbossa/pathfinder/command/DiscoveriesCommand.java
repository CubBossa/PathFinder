package de.cubbossa.pathfinder.command;

import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.group.DiscoverProgressModifier;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.misc.Pagination;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectMapping;
import de.cubbossa.tinytranslations.util.ListSection;
import dev.jorel.commandapi.CommandTree;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
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
      p.sendMessage(Messages.CMD_DISCOVERIES_LIST
          .insertList("discoveries", l.stream()
                  .map(e -> new Discovery(e.getKey().getDisplayName(), e.getValue()))
                  .toList(),
              ListSection.paged(pagination.getPage(), pagination.getSize()),
              Collections.emptyList(),
              List.of(MAPPING)
          ));
    });
  }

  private static final TinyObjectMapping MAPPING = TinyObjectMapping.builder(Discovery.class)
      .withFallbackConversion(Discovery::name)
      .with("name", Discovery::name)
      .with("ratio", Discovery::ratio)
      .with("percentage", d -> d.ratio * 100)
      .build();

  private record Discovery(Component name, double ratio) {
  }
}
