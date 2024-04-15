package de.cubbossa.pathfinder.command;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.misc.PathPlayer;
import de.cubbossa.pathfinder.CommandRegistry;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.PathPerms;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.module.AbstractNavigationHandler;
import de.cubbossa.pathfinder.module.BukkitNavigationHandler;
import de.cubbossa.pathfinder.navigation.NavigationLocation;
import de.cubbossa.pathfinder.navigation.Route;
import de.cubbossa.pathfinder.node.implementation.PlayerNode;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.BukkitVectorUtils;
import dev.jorel.commandapi.CommandTree;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class FindPlayerManager implements Disposable {

  private final Map<UUID, Map<UUID, BukkitTask>> requests;

  public FindPlayerManager(PathFinder pathFinder, CommandRegistry commandRegistry) {

    pathFinder.getDisposer().register(commandRegistry, this);
    requests = new HashMap<>();

    commandRegistry.registerCommand(new CommandTree("findplayer")
        .withAliases("navigatetoplayer")
        .withPermission(PathPerms.PERM_CMD_FIND_PLAYER_REQUEST)
        .then(Arguments.player("target")
            .executesPlayer((sender, args) -> {
              makeRequest(sender, args.<Player>getUnchecked(0).getUniqueId());
            })
        )
    );
    commandRegistry.registerCommand(new CommandTree("findplayeraccept")
        .withAliases("fpaccept")
        .withPermission(PathPerms.PERM_CMD_FIND_PLAYER_ACCEPT)
        .executesPlayer((sender, args) -> {
          requests.getOrDefault(sender.getUniqueId(), new HashMap<>()).keySet().stream()
              .findAny()
              .ifPresentOrElse(
                  uuid -> acceptRequest(sender, uuid),
                  () -> BukkitUtils.wrap(sender).sendMessage(Messages.CMD_FINDP_NO_REQ)
              );
        })
        .then(Arguments.player("target")
            .replaceSuggestions((info, builder) -> {
              requests.getOrDefault(((Player) info.sender()).getUniqueId(), new HashMap<>()).keySet().stream()
                  .map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline)
                  .map(Player::getName).forEach(builder::suggest);
              return builder.buildFuture();
            })
            .executesPlayer((sender, args) -> {
              acceptRequest(sender, args.<Player>getUnchecked(0).getUniqueId());
            })
        )
    );
    commandRegistry.registerCommand(new CommandTree("findplayerdecline")
        .withAliases("fpdecline")
        .withPermission(PathPerms.PERM_CMD_FIND_PLAYER_DECLINE)
        .executesPlayer((sender, args) -> {
          requests.getOrDefault(sender.getUniqueId(), new HashMap<>()).keySet().stream()
              .findAny()
              .ifPresentOrElse(
                  uuid -> declineRequest(sender, uuid),
                  () -> BukkitUtils.wrap(sender).sendMessage(Messages.CMD_FINDP_NO_REQ)
              );
        })
        .then(Arguments.player("target")
            .replaceSuggestions((info, builder) -> {
              requests.getOrDefault(((Player) info.sender()).getUniqueId(), new HashMap<>()).keySet().stream()
                  .map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline)
                  .map(Player::getName).forEach(builder::suggest);
              return builder.buildFuture();
            })
            .executesPlayer((sender, args) -> {
              declineRequest(sender, args.<Player>getUnchecked(0).getUniqueId());
            })
        )
    );
  }

  private void makeRequest(Player requester, UUID target) {
    if (requester.getUniqueId().equals(target)) {
      BukkitUtils.wrap(requester).sendMessage(Messages.CMD_FINDP_NO_SELF);
      return;
    }
    Player targetPlayer = Bukkit.getPlayer(target);
    if (targetPlayer == null || !targetPlayer.isOnline()) {
      BukkitUtils.wrap(requester).sendMessage(Messages.CMD_FINDP_OFFLINE);
      return;
    }
    if (requests.getOrDefault(target, new HashMap<>()).containsKey(requester.getUniqueId())) {
      BukkitUtils.wrap(requester).sendMessage(Messages.CMD_FINDP_ALREADY_REQ);
      return;
    }

    requests.computeIfAbsent(target, uuid -> new HashMap<>()).put(requester.getUniqueId(),
        Bukkit.getScheduler().runTaskLater(PathFinderPlugin.getInstance(), () -> {
          requests.computeIfPresent(requester.getUniqueId(), (uuid, uuidBukkitTaskMap) -> {
            uuidBukkitTaskMap.remove(requester.getUniqueId());
            BukkitUtils.wrap(requester).sendMessage(Messages.CMD_FINDP_EXPIRED);
            return uuidBukkitTaskMap;
          });
        }, 20 * 30L));
    BukkitUtils.wrap(requester).sendMessage(Messages.CMD_FINDP_REQUEST.formatted(
        Placeholder.parsed("target", targetPlayer.getName()),
        Placeholder.parsed("requester", requester.getName())
    ));
    BukkitUtils.wrap(targetPlayer).sendMessage(Messages.CMD_FINDP_REQUESTED.formatted(
        Placeholder.parsed("target", targetPlayer.getName()),
        Placeholder.parsed("requester", requester.getName())
    ));
  }

  private void acceptRequest(Player target, UUID requester) {
    Map<UUID, BukkitTask> innerRequests = requests.get(target.getUniqueId());
    if (innerRequests == null || !innerRequests.containsKey(requester)) {
      BukkitUtils.wrap(target).sendMessage(Messages.CMD_FINDP_NO_REQ);
      return;
    }
    BukkitTask task = innerRequests.remove(requester);
    task.cancel();

    Player requesterPlayer = Bukkit.getPlayer(requester);
    if (requesterPlayer == null || !requesterPlayer.isOnline()) {
      BukkitUtils.wrap(target).sendMessage(Messages.CMD_FINDP_OFFLINE);
      return;
    }

    BukkitUtils.wrap(target).sendMessage(Messages.CMD_FINDP_ACCEPT.formatted(
        Placeholder.parsed("target", target.getName()),
        Placeholder.parsed("requester", requesterPlayer.getName())
    ));
    BukkitUtils.wrap(requesterPlayer).sendMessage(Messages.CMD_FINDP_ACCEPTED.formatted(
        Placeholder.parsed("target", target.getName()),
        Placeholder.parsed("requester", requesterPlayer.getName())
    ));

    PathPlayer<Player> requesterPathPlayer = BukkitUtils.wrap(requesterPlayer);
    BukkitNavigationHandler.getInstance().navigate(requesterPathPlayer, Route
        .from(NavigationLocation.movingExternalNode(new PlayerNode(requesterPathPlayer)))
        .to(NavigationLocation.movingExternalNode(new PlayerNode(PathPlayer.wrap(target))))
    ).whenComplete((path, throwable) -> {
      if (throwable != null) {
        requesterPlayer.sendMessage(throwable.getMessage()); // TODO
        return;
      }
      path.startUpdater(1000);
      BukkitNavigationHandler.getInstance().cancelPathWhenTargetReached(path);
    });
  }

  private void declineRequest(Player target, UUID requester) {
    Player requesterPlayer = Bukkit.getPlayer(requester);
    AtomicBoolean wasRemoved = new AtomicBoolean(false);
    requests.computeIfPresent(target.getUniqueId(), (uuid, uuids) -> {
      BukkitTask t = uuids.remove(requester);
      if (t != null) {
        t.cancel();
        wasRemoved.set(true);
      }
      return uuids;
    });
    if (!wasRemoved.get()) {
      BukkitUtils.wrap(target).sendMessage(Messages.CMD_FINDP_NO_REQ);
      return;
    }
    BukkitUtils.wrap(target).sendMessage(Messages.CMD_FINDP_DECLINE.formatted(
        Placeholder.parsed("target", target.getName()),
        Placeholder.parsed("requester", requesterPlayer.getName())
    ));
    BukkitUtils.wrap(requesterPlayer).sendMessage(Messages.CMD_FINDP_DECLINED.formatted(
        Placeholder.parsed("target", target.getName()),
        Placeholder.parsed("requester", requesterPlayer.getName())
    ));
  }
}