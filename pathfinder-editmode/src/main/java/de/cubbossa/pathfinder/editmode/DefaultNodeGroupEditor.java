package de.cubbossa.pathfinder.editmode;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.editor.NodeGroupEditor;
import de.cubbossa.pathapi.event.*;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Groupable;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.CommonPathFinder;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.editmode.menu.EditModeMenu;
import de.cubbossa.pathfinder.util.BukkitUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter
@Setter
@RequiredArgsConstructor
public class DefaultNodeGroupEditor implements NodeGroupEditor<Player>, GraphRenderer<Player>, Listener {

  public static final Action<TargetContext<PlayerInteractEvent>> INTERACT = new Action<>();

  private final PathFinder pathFinder;
  private final NamespacedKey groupKey;

  private final Map<PathPlayer<Player>, BottomInventoryMenu> editingPlayers;
  private final Map<PathPlayer<Player>, GameMode> preservedGameModes;

  private final Collection<GraphRenderer<Player>> renderers;
  private final Collection<de.cubbossa.pathapi.event.Listener<?>> listeners;

  public DefaultNodeGroupEditor(NodeGroup group) {
    this.pathFinder = PathFinderProvider.get();
    this.groupKey = group.getKey();

    this.renderers = new ArrayList<>();
    this.editingPlayers = new HashMap<>();
    this.preservedGameModes = new HashMap<>();

    EventDispatcher<?> eventDispatcher = PathFinderProvider.get().getEventDispatcher();
    listeners = new HashSet<>();

    Consumer<Node> erase = node -> {
      for (PathPlayer<Player> player : editingPlayers.keySet()) {
        eraseNodes(player, List.of(node));
      }
    };
    Consumer<NodeEvent> render = event -> {
      if (!renders(event.getNode())) {
        erase.accept(event.getNode());
        return;
      }
      for (PathPlayer<Player> player : editingPlayers.keySet()) {
        renderNodes(player, List.of(event.getNode()));
      }
    };
    listeners.add(eventDispatcher.listen(NodeCreateEvent.class, render));
    listeners.add(eventDispatcher.listen(NodeSaveEvent.class, render));
    listeners.add(eventDispatcher.listen(NodeDeleteEvent.class, e -> erase.accept(e.getNode())));

    eventDispatcher.listen(NodeGroupDeleteEvent.class, nodeGroupDeleteEvent -> {
      if (!nodeGroupDeleteEvent.getGroup().getKey().equals(groupKey)) {
        return;
      }
      for (PathPlayer<Player> player : editingPlayers.keySet()) {
        setEditMode(player, false);
        player.sendMessage(Messages.EDITM_NG_DELETED);
      }
      dispose();
    });

    Bukkit.getPluginManager().registerEvents(this, PathFinderPlugin.getInstance());
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    PathPlayer<Player> p = BukkitUtils.wrap(event.getPlayer());
    BottomInventoryMenu menu = editingPlayers.get(p);
    if (menu == null) {
      return;
    }
    event.setCancelled(menu.handleInteract(INTERACT, new TargetContext<>(
        event.getPlayer(), menu, event.getPlayer().getInventory().getHeldItemSlot(),
        INTERACT, true, event
    )));
  }

  private boolean renders(Node node) {
    return node instanceof Groupable groupable && groupable.getGroups().stream()
        .map(NodeGroup::getKey)
        .anyMatch(groupKey::equals);
  }

  public void dispose() {
    PlayerInteractEvent.getHandlerList().unregister(this);
    listeners.forEach(pathFinder.getEventDispatcher()::drop);
  }

  public boolean isEdited() {
    return !editingPlayers.isEmpty();
  }

  public boolean toggleEditMode(PathPlayer<Player> player) {
    boolean isEditing = isEditing(player);
    setEditMode(player, !isEditing);
    return !isEditing;
  }

  public void cancelEditModes() {
    for (PathPlayer<Player> player : editingPlayers.keySet()) {
      setEditMode(player, false);
    }
  }

  /**
   * Sets a player into edit mode for this roadmap.
   *
   * @param player   the player to set the edit mode for
   * @param activate activate or deactivate edit mode
   */
  @Override
  public void setEditMode(PathPlayer<Player> player, boolean activate) {
    Player bukkitPlayer = player.unwrap();

    if (activate) {
      if (bukkitPlayer == null || !bukkitPlayer.isOnline()) {
        return;
      }

      BottomInventoryMenu menu = new EditModeMenu(
          pathFinder.getStorage(), groupKey,
          pathFinder.getNodeTypeRegistry().getTypes(),
          pathFinder.getConfiguration().getEditMode()
      ).createHotbarMenu(this, bukkitPlayer);
      editingPlayers.put(player, menu);
      menu.openSync(bukkitPlayer);

      preservedGameModes.put(player, bukkitPlayer.getGameMode());
      bukkitPlayer.setGameMode(GameMode.CREATIVE);

      pathFinder.getStorage().loadGroup(groupKey).thenCompose(group -> {
        return pathFinder.getStorage().loadNodes(group.orElseThrow()).thenAccept(n -> {
          renderNodes(player, n);
        });
      });
    } else {

      if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
        BottomInventoryMenu menu = editingPlayers.get(player);
        if (menu != null) {
          menu.close(bukkitPlayer);
        }
        bukkitPlayer.setGameMode(preservedGameModes.getOrDefault(player, GameMode.SURVIVAL));
      }
      clear(player);
      editingPlayers.remove(player);
    }
  }

  public boolean isEditing(PathPlayer<Player> player) {
    return editingPlayers.containsKey(player);
  }

  public boolean isEditing(Player player) {
    return isEditing(CommonPathFinder.getInstance().wrap(player));
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    return CompletableFuture.allOf(renderers.stream()
        .map(r -> r.clear(player))
        .toArray(CompletableFuture[]::new));
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.allOf(renderers.stream()
        .map(r -> r.renderNodes(player, nodes))
        .toArray(CompletableFuture[]::new));
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.allOf(renderers.stream()
        .map(r -> r.eraseNodes(player, nodes))
        .toArray(CompletableFuture[]::new));
  }
}
