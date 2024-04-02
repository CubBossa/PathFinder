package de.cubbossa.pathfinder.editmode;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.editor.GraphEditor;
import de.cubbossa.pathapi.editor.GraphRenderer;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.event.NodeCreateEvent;
import de.cubbossa.pathapi.event.NodeDeleteEvent;
import de.cubbossa.pathapi.event.NodeGroupDeleteEvent;
import de.cubbossa.pathapi.event.NodeSaveEvent;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.AbstractPathFinder;
import de.cubbossa.pathfinder.PathFinderPlugin;
import de.cubbossa.pathfinder.editmode.menu.EditModeMenu;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.util.BukkitUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@Getter
@Setter
@RequiredArgsConstructor
public class DefaultGraphEditor implements GraphEditor<Player>, GraphRenderer<Player>, Listener {

  public static final Action<TargetContext<PlayerInteractEvent>> INTERACT = new Action<>();

  private final PathFinder pathFinder;
  private final NamespacedKey groupKey;

  private final Map<PathPlayer<Player>, BottomInventoryMenu> editingPlayers;
  private final Map<PathPlayer<Player>, GameMode> preservedGameModes;

  private final Collection<GraphRenderer<Player>> renderers;
  private final Collection<de.cubbossa.pathapi.event.Listener<?>> listeners;
  private final EntityInteractListener entityInteractListener = new EntityInteractListener();

  private final ExecutorService renderExecutor;

  public DefaultGraphEditor(NodeGroup group) {
    this.pathFinder = PathFinderProvider.get();
    this.groupKey = group.getKey();

    this.renderers = new ArrayList<>();
    this.editingPlayers = new HashMap<>();
    this.preservedGameModes = new HashMap<>();

    this.renderExecutor = Executors.newSingleThreadExecutor();

    EventDispatcher<?> eventDispatcher = PathFinderProvider.get().getEventDispatcher();
    listeners = new HashSet<>();

    listeners.add(eventDispatcher.listen(NodeCreateEvent.class, e -> renderAll(e.getNode())));
    listeners.add(eventDispatcher.listen(NodeSaveEvent.class, e -> renderAll(e.getNode())));
    listeners.add(eventDispatcher.listen(NodeDeleteEvent.class, e -> eraseAll(e.getNode())));
    Bukkit.getPluginManager().registerEvents(entityInteractListener, PathFinderPlugin.getInstance());

    eventDispatcher.listen(NodeGroupDeleteEvent.class, event -> {
      if (!event.getGroup().getKey().equals(groupKey)) {
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

  private void renderAll(Node node) {
    renderAll(Collections.singleton(node));
  }

  private void renderAll(Collection<Node> nodes) {
    for (PathPlayer<Player> player : editingPlayers.keySet()) {
      List<Node> sortedNodes = new ArrayList<>(nodes);
      sortedNodes.sort(Comparator.comparing(node -> node.getLocation().distanceSquared(player.getLocation())));

      renderNodes(player, sortedNodes);
    }
  }

  private void eraseAll(Node node) {
    eraseAll(Collections.singleton(node));
  }

  private void eraseAll(Collection<Node> nodes) {
    for (PathPlayer<Player> player : editingPlayers.keySet()) {
      eraseNodes(player, nodes);
    }
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

  @SneakyThrows
  public void dispose() {
    PlayerInteractEvent.getHandlerList().unregister(this);
    listeners.forEach(pathFinder.getEventDispatcher()::drop);
    entityInteractListener.close();

    for (GraphRenderer<Player> renderer : renderers) {
      renderer.close();
    }
    renderExecutor.shutdown();
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
    return isEditing(AbstractPathFinder.getInstance().wrap(player));
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    return CompletableFuture.allOf(renderers.stream()
            .map(r -> r.clear(player))
            .toArray(CompletableFuture[]::new))
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.allOf(renderers.stream()
            .map(r -> r.renderNodes(player, nodes))
            .toArray(CompletableFuture[]::new))
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node> nodes) {
    return CompletableFuture.allOf(renderers.stream()
            .map(r -> r.eraseNodes(player, nodes))
            .toArray(CompletableFuture[]::new))
        .exceptionally(throwable -> {
          throwable.printStackTrace();
          return null;
        });
  }

  private class EntityInteractListener implements Listener, AutoCloseable {
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
      PathPlayer<Player> player = BukkitUtils.wrap(e.getPlayer());
      if (!editingPlayers.containsKey(player)) {
        return;
      }
      int slot = e.getPlayer().getInventory().getHeldItemSlot();
      // slots 0-5 are part of the editmode -> cancel interaction
      // it does not affect interacting with nodes or edges, those are handled beforehand
      // and are no valid entities, hence they don't trigger actual Events
      if (slot < 5) {
        e.setCancelled(true);
      }
    }

    @Override
    public void close() throws Exception {
      PlayerInteractAtEntityEvent.getHandlerList().unregister(this);
    }
  }
}
