package de.cubbossa.pathfinder.editmode;

import com.google.common.collect.Lists;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.pathfinder.PathFinderProvider;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.EdgesCreateEvent;
import de.cubbossa.pathfinder.core.events.node.EdgesDeleteEvent;
import de.cubbossa.pathfinder.core.events.node.NodeTeleportEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemovedEvent;
import de.cubbossa.pathfinder.core.node.SimpleEdge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.nodegroup.SimpleNodeGroup;
import de.cubbossa.pathfinder.core.nodegroup.NodeGroupEditor;
import de.cubbossa.pathfinder.editmode.menu.EditModeMenu;
import de.cubbossa.pathfinder.editmode.utils.ClientNodeHandler;
import de.cubbossa.pathfinder.util.LerpUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.task.TaskManager;

@Getter
@Setter
@RequiredArgsConstructor
public class DefaultNodeGroupEditor implements NodeGroupEditor, Listener {

  private final PathFinder pathFinder;
  private final NamespacedKey key;
  private final ClientNodeHandler armorstandHandler;

  private final Map<UUID, BottomInventoryMenu> editingPlayers;
  private final Map<UUID, GameMode> preservedGameModes;

  private final Collection<Integer> editModeTasks;

  private final Map<UUID, Node<?>> nodes = new HashMap<>();
  private final Collection<SimpleEdge> edges = new HashSet<>();

  private float particleDistance = .3f;
  private int tickDelay = 5;
  private Color colorFrom = new Color(255, 0, 0);
  private Color colorTo = new Color(0, 127, 255);

  public DefaultNodeGroupEditor(SimpleNodeGroup group) {
    this.pathFinder = PathFinderProvider.get();
    this.key = group.getKey();

    this.editModeTasks = new HashSet<>();
    this.armorstandHandler = new ClientNodeHandler(PathPlugin.getInstance());
    this.editingPlayers = new HashMap<>();
    this.preservedGameModes = new HashMap<>();

    Bukkit.getPluginManager().registerEvents(this, PathPlugin.getInstance());
  }

  public void dispose() {
    HandlerList.unregisterAll(this);
  }

  public boolean isEdited() {
    return !editingPlayers.isEmpty();
  }

  public boolean toggleEditMode(UUID uuid) {
    boolean isEditing = isEditing(uuid);
    setEditMode(uuid, !isEditing);
    return !isEditing;
  }

  public void cancelEditModes() {
    for (UUID uuid : editingPlayers.keySet()) {
      setEditMode(uuid, false);
    }
  }

  /**
   * Sets a player into edit mode for this roadmap.
   *
   * @param uuid     the player to set the edit mode for
   * @param activate activate or deactivate edit mode
   */
  public void setEditMode(UUID uuid, boolean activate) {
    Player player = Bukkit.getPlayer(uuid);

    if (activate) {
      if (player == null) {
        return;
      }

      if (!isEdited()) {
        startParticleTask();
      }
      BottomInventoryMenu menu = new EditModeMenu(pathFinder, key,
          PathPlugin.getInstance().getNodeTypeRegistry().getTypes()).createHotbarMenu(this, player);
      editingPlayers.put(uuid, menu);
      menu.openSync(player);

      preservedGameModes.put(player.getUniqueId(), player.getGameMode());
      player.setGameMode(GameMode.CREATIVE);

      showArmorStands(player);

    } else {

      if (player != null) {
        BottomInventoryMenu menu = editingPlayers.get(uuid);
        if (menu != null) {
          menu.close(player);
        }
        hideArmorStands(player);
        player.setGameMode(
            preservedGameModes.getOrDefault(player.getUniqueId(), GameMode.SURVIVAL));
      }

      editingPlayers.remove(uuid);

      if (!isEdited()) {
        stopParticleTask();
      }
    }
  }

  public void showArmorStands(Player player) {
    pathFinder.getStorage().loadGroup(key).thenAccept(opt -> {
      if (opt.isEmpty()) {
        return;
      }
      SimpleNodeGroup group = opt.get();
      group.resolve().thenAccept(nodes -> {
        for (Node<?> node : nodes) {
          this.nodes.put(node.getNodeId(), node);
          this.edges.addAll(node.getEdges());
        }
        armorstandHandler.showNodes(nodes, player);
        armorstandHandler.showEdges(edges, player);
      });
    });
  }

  public void hideArmorStands(Player player) {
    armorstandHandler.hideNodes(nodes.values(), player);
    armorstandHandler.hideEdges(edges, player);
    nodes.clear();
    edges.clear();
  }

  public boolean isEditing(UUID uuid) {
    return editingPlayers.containsKey(uuid);
  }

  public boolean isEditing(Player player) {
    return isEditing(player.getUniqueId());
  }

  private void startParticleTask() {
    updateEditModeParticles();
  }

  private void stopParticleTask() {
    var sched = Bukkit.getScheduler();
    editModeTasks.forEach(sched::cancelTask);
  }

  /**
   * Erstellt eine Liste aus Partikel Packets, die mithilfe eines Schedulers immerwieder an die Spieler im Editmode geschickt werden.
   * Um gelöschte und neue Kanten darstellen zu können, muss diese Liste aus Packets aktualisiert werden.
   * Wird asynchron ausgeführt
   */
  public void updateEditModeParticles() {
    CompletableFuture.runAsync(() -> {

      var sched = Bukkit.getScheduler();
      new ArrayList<>(editModeTasks).forEach(sched::cancelTask);

      Map<SimpleEdge, Boolean> undirected = new HashMap<>();
      for (SimpleEdge edge : edges) {
        SimpleEdge contained = edges.stream()
            .filter(e -> e.getEnd().equals(edge.getStart()) && e.getStart().equals(edge.getEnd()))
            .findAny().orElse(null);
        if (contained != null && undirected.containsKey(contained)) {
          undirected.put(contained, true);
        } else {
          undirected.put(edge, false);
        }
      }

      Map<Color, List<Object>> packets = new HashMap<>();
      Map<Color, ParticleBuilder> particles = new HashMap<>();

      for (var entry : undirected.entrySet()) {
        entry.getKey().resolveStart().thenAccept(start -> {
          entry.getKey().resolveEnd().thenAccept(end -> {
            if (!Objects.equals(start.getLocation().getWorld(),
                end.getLocation().getWorld())) {
              return;
            }
            boolean directed = !entry.getValue();

            Vector a = start.getLocation().toVector();
            Vector b = end.getLocation().toVector();
            double dist = a.distance(b);

            for (float i = 0; i < dist; i += particleDistance) {
              Color c = directed ? LerpUtils.lerp(colorFrom, colorTo, i / dist) : colorFrom;

              ParticleBuilder builder = particles.computeIfAbsent(c,
                  k -> new ParticleBuilder(ParticleEffect.REDSTONE).setColor(k));
              packets.computeIfAbsent(c, x -> new ArrayList<>()).add(builder.setLocation(
                  LerpUtils.lerp(a, b, i / dist)
                      .toLocation(start.getLocation().getWorld())).toPacket());
            }
          });
        });
      }
      for (var entry : packets.entrySet()) {
        editModeTasks.add(TaskManager.startSuppliedTask(entry.getValue(), tickDelay,
            () -> editingPlayers.keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .collect(Collectors.toSet())));
      }
    }).exceptionally(throwable -> {
      throwable.printStackTrace();
      return null;
    });
  }

  @EventHandler
  public void onNodeGroupAssign(NodeGroupAssignedEvent event) {
    pathFinder.getStorage().loadNodes(event.getGroupables()).thenAccept(nodes -> {
      nodes.forEach(node -> {
        editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player -> {
          armorstandHandler.showNode(node, player);
        });
        this.nodes.put(node.getNodeId(), node);
      });
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onNodeGroupRemove(NodeGroupRemovedEvent event) {
    Collection<Groupable<?>> groupables = event.getGroupables();
    for (UUID uuid : editingPlayers.keySet()) {
      Player player = Bukkit.getPlayer(uuid);
      for (Groupable<?> node : groupables) {
        armorstandHandler.updateNodeHead(player, node);
      }
    }
  }

  @EventHandler
  public void onEdgeCreated(EdgesCreateEvent.Post event) {
    // TODO check if in current group obv
    editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player -> {
      armorstandHandler.showEdges(event.getEdges(), player);
    });
    updateEditModeParticles();
  }

  @EventHandler
  public void onNodesDeleted(NodesDeletedEvent event) {

    editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player -> {
      for (Node<?> node : event.getNodes()) {
        this.nodes.remove(node.getNodeId());
      }
      armorstandHandler.hideNodes(event.getNodes(), player);
    });
    updateEditModeParticles();
  }

  @EventHandler
  public void onEdgesDeleted(EdgesDeleteEvent.Post event) {
    editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player ->
        armorstandHandler.hideEdges(Lists.newArrayList(event.getEdges()), player));
    updateEditModeParticles();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onTeleportNode(NodeTeleportEvent event) {
    Bukkit.getScheduler().runTaskLater(PathPlugin.getInstance(), () -> {
      if (event.isCancelled()) {
        return;
      }
      int updateCount = 0;
      for (UUID uuid1 : editingPlayers.keySet()) {
        Player player = Bukkit.getPlayer(uuid1);
        if (player == null) {
          continue;
        }
        for (Node<?> node : event.getNodes()) {
          if (node != null) {
            armorstandHandler.updateNodePosition(node, player, player.getLocation(), true);
            updateCount++;
          }
        }
      }
      if (updateCount > 0) {
        updateEditModeParticles();
      }
    }, 1);
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
}
