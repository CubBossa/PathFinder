package de.cubbossa.pathfinder.editmode;

import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.PathFinderProvider;
import de.cubbossa.pathfinder.api.editor.GraphRenderer;
import de.cubbossa.pathfinder.api.editor.NodeGroupEditor;
import de.cubbossa.pathfinder.api.event.EventDispatcher;
import de.cubbossa.pathfinder.api.event.NodeCreateEvent;
import de.cubbossa.pathfinder.api.event.NodeDeleteEvent;
import de.cubbossa.pathfinder.api.event.NodeEvent;
import de.cubbossa.pathfinder.api.event.NodeSaveEvent;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.editmode.menu.EditModeMenu;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@RequiredArgsConstructor
public class DefaultNodeGroupEditor implements NodeGroupEditor<Player>, GraphRenderer<Player>, Listener {

  private final PathFinder pathFinder;
  private final NamespacedKey key;

  private final Map<PathPlayer<Player>, BottomInventoryMenu> editingPlayers;
  private final Map<PathPlayer<Player>, GameMode> preservedGameModes;

  private final Collection<GraphRenderer<Player>> renderers;


  public DefaultNodeGroupEditor(NodeGroup group) {
    this.pathFinder = PathFinderProvider.get();
    this.key = group.getKey();

    this.renderers = new ArrayList<>();
    this.editingPlayers = new HashMap<>();
    this.preservedGameModes = new HashMap<>();

    Bukkit.getPluginManager().registerEvents(this, PathPlugin.getInstance());
    EventDispatcher eventDispatcher = PathPlugin.getInstance().getEventDispatcher();

    Consumer<NodeEvent> render = event -> {
      for (PathPlayer<Player> player : editingPlayers.keySet()) {
        renderNodes(player, List.of(event.getNode()));
      }
    };
    eventDispatcher.listen(NodeCreateEvent.class, render);
    eventDispatcher.listen(NodeSaveEvent.class, render);

    eventDispatcher.listen(NodeDeleteEvent.class, nodeDeleteEvent -> {
      for (PathPlayer<Player> player : editingPlayers.keySet()) {
        eraseNodes(player, List.of(nodeDeleteEvent.getNode()));
      }
    });
  }

  public void dispose() {
    HandlerList.unregisterAll(this);
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

      BottomInventoryMenu menu = new EditModeMenu(pathFinder, key,
          PathPlugin.getInstance().getNodeTypeRegistry().getTypes()).createHotbarMenu(this, bukkitPlayer);
      editingPlayers.put(player, menu);
      menu.openSync(bukkitPlayer);

      preservedGameModes.put(player, bukkitPlayer.getGameMode());
      bukkitPlayer.setGameMode(GameMode.CREATIVE);

      pathFinder.getStorage().loadGroup(key).thenAccept(group -> {
        pathFinder.getStorage().loadNodes(group.orElseThrow()).thenAccept(n -> {
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
    return isEditing(PathPlugin.wrap(player));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDelete(NodeGroupDeleteEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getGroup().equals(key)) {
      dispose();
    }
  }

  @Override
  public CompletableFuture<Void> clear(PathPlayer<Player> player) {
    return CompletableFuture.allOf(renderers.stream()
        .map(r -> r.clear(player))
        .toArray(CompletableFuture[]::new));
  }

  @Override
  public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node<?>> nodes) {
    return CompletableFuture.allOf(renderers.stream()
        .map(r -> r.renderNodes(player, nodes))
        .toArray(CompletableFuture[]::new));
  }

  @Override
  public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node<?>> nodes) {
    return CompletableFuture.allOf(renderers.stream()
        .map(r -> r.eraseNodes(player, nodes))
        .toArray(CompletableFuture[]::new));
  }
}
