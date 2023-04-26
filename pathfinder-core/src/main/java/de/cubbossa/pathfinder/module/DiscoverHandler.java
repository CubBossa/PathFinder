package de.cubbossa.pathfinder.module;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.events.discovering.PlayerDiscoverEvent;
import de.cubbossa.pathfinder.events.discovering.PlayerForgetEvent;
import de.cubbossa.pathfinder.listener.DiscoverListener;
import de.cubbossa.pathfinder.nodegroup.modifier.DiscoverableModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.FindDistanceModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.PermissionModifier;
import de.cubbossa.serializedeffects.EffectHandler;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DiscoverHandler {

  @Getter
  private static DiscoverHandler instance;
  private PathPlugin plugin;

  public DiscoverHandler(PathPlugin plugin) {
    instance = this;
    this.plugin = plugin;

    if (!plugin.getConfiguration().moduleConfig.discoveryModule) {
      return;
    }
    Bukkit.getPluginManager().registerEvents(new DiscoverListener(this.plugin), plugin);

    if (plugin.getConfiguration().navigation.requireDiscovery) {
      FindModule.getInstance().registerFindPredicate(context -> {
        if (!(context.node() instanceof Groupable groupable)) {
          return true;
        }
        Collection<NodeGroup> groups = groupable.getGroups();

        for (NodeGroup group : groups) {
          if (!group.hasModifier(DiscoverableModifier.class)) {
            continue;
          }
          if (!hasDiscovered(context.playerId(), group).join()) {
            return false;
          }
        }
        return true;
      });
    }
  }

  public void playDiscovery(UUID playerId, DiscoverableModifier discoverable) {
    Player player = Bukkit.getPlayer(playerId);
    if (player == null) {
      throw new IllegalStateException("Player is null");
    }
    EffectHandler.getInstance().playEffect(
        plugin.getEffectsFile(),
        "discover",
        player,
        player.getLocation(),
        Placeholder.component("name", discoverable.getDisplayName()));
  }

  public boolean fulfillsDiscoveringRequirements(NodeGroup group, PathPlayer<?> player) {
    if (!group.hasModifier(DiscoverableModifier.class)) {
      return false;
    }
    PermissionModifier perm = group.getModifier(PermissionModifier.class);
    if (perm != null && !player.hasPermission(perm.permission())) {
      return false;
    }
    for (Node node : group.resolve().join()) {
      if (node == null) {
        plugin.getLogger().log(Level.SEVERE, "Node is null");
        continue;
      }
      float dist = getDiscoveryDistance(player.getUniqueId(), node);
      if (node.getLocation().getX() - player.getLocation().getX() > dist) {
        continue;
      }
      if (node.getLocation().distance(player.getLocation()) > dist) {
        continue;
      }
      return true;
    }
    return false;
  }

  public void discover(UUID playerId, NodeGroup group, LocalDateTime date) {
    if (!group.hasModifier(DiscoverableModifier.class)) {
      return;
    }
    plugin.getStorage().loadDiscoverInfo(playerId, group.getKey()).thenAccept(discoverInfo -> {
      if (discoverInfo.isPresent()) {
        return;
      }
      PlayerDiscoverEvent event = new PlayerDiscoverEvent(playerId, group, date);
      Bukkit.getPluginManager().callEvent(event);
      if (event.isCancelled()) {
        return;
      }
      DiscoverableModifier discoverable = group.getModifier(DiscoverableModifier.class);

      plugin.getStorage().createAndLoadDiscoverinfo(playerId, group.getKey(), date)
          .thenAccept(info -> {
            playDiscovery(playerId, discoverable);
          });
    });
  }

  public void forget(UUID playerId, NodeGroup group) {
    if (!group.hasModifier(DiscoverableModifier.class)) {
      return;
    }

    plugin.getStorage().loadDiscoverInfo(playerId, group.getKey()).thenAccept(discoverInfo -> {
      if (discoverInfo.isEmpty()) {
        return;
      }
      Bukkit.getScheduler().runTask(plugin, () -> {
        PlayerForgetEvent event = new PlayerForgetEvent(playerId, group);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
          return;
        }
        plugin.getStorage().deleteDiscoverInfo(discoverInfo.get());
      });
    });
  }

  public CompletableFuture<Boolean> hasDiscovered(UUID playerId, NodeGroup group) {
    return plugin.getStorage().loadDiscoverInfo(playerId, group.getKey())
        .thenApply(Optional::isPresent);
  }

  public float getDiscoveryDistance(UUID playerId, Node node) {
    if (!(node instanceof Groupable groupable)) {
      return 1.5f;
    }
    FindDistanceModifier mod = groupable.getGroups().stream()
        .filter(group -> group.hasModifier(FindDistanceModifier.class))
        .sorted()
        .findFirst()
        .map(group -> group.getModifier(FindDistanceModifier.class))
        .orElse(null);
    return mod == null ? 1.5f : (float) mod.distance();
  }
}
