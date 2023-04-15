package de.cubbossa.pathfinder.editmode;

import com.google.common.collect.Lists;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.PathFinderProvider;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.group.NodeGroupEditor;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.api.node.Edge;
import de.cubbossa.pathfinder.api.node.Groupable;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.core.events.node.EdgesCreateEvent;
import de.cubbossa.pathfinder.core.events.node.EdgesDeleteEvent;
import de.cubbossa.pathfinder.core.events.node.NodeTeleportEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupDeleteEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemovedEvent;
import de.cubbossa.pathfinder.core.node.SimpleEdge;
import de.cubbossa.pathfinder.editmode.menu.EditModeMenu;
import de.cubbossa.pathfinder.editmode.utils.ClientNodeHandler;
import de.cubbossa.pathfinder.util.LerpUtils;
import de.cubbossa.pathfinder.util.VectorUtils;
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
public class DefaultNodeGroupEditor implements NodeGroupEditor<Player>, Listener {

  private final PathFinder pathFinder;
  private final NamespacedKey key;
  private final ClientNodeHandler armorstandHandler;

  private final Map<PathPlayer<Player>, BottomInventoryMenu> editingPlayers;
  private final Map<PathPlayer<Player>, GameMode> preservedGameModes;

  private final Collection<Integer> editModeTasks;

  private final Map<UUID, Node<?>> nodes = new HashMap<>();
  private final Collection<Edge> edges = new HashSet<>();

  private float particleDistance = .3f;
  private int tickDelay = 5;
  private Color colorFrom = new Color(255, 0, 0);
  private Color colorTo = new Color(0, 127, 255);

  public DefaultNodeGroupEditor(NodeGroup group) {
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

      if (!isEdited()) {
        startParticleTask();
      }
      BottomInventoryMenu menu = new EditModeMenu(pathFinder, key,
          PathPlugin.getInstance().getNodeTypeRegistry().getTypes()).createHotbarMenu(this, bukkitPlayer);
      editingPlayers.put(player, menu);
      menu.openSync(bukkitPlayer);

      preservedGameModes.put(player, bukkitPlayer.getGameMode());
      bukkitPlayer.setGameMode(GameMode.CREATIVE);

      showArmorStands(player);

    } else {

      if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
        BottomInventoryMenu menu = editingPlayers.get(player);
        if (menu != null) {
          menu.close(bukkitPlayer);
        }
        hideArmorStands(player);
        bukkitPlayer.setGameMode(
            preservedGameModes.getOrDefault(player, GameMode.SURVIVAL));
      }

      editingPlayers.remove(player);

      if (!isEdited()) {
        stopParticleTask();
      }
    }
  }

  public void showArmorStands(PathPlayer<Player> player) {
    pathFinder.getStorage().loadGroup(key).thenAccept(opt -> {
      if (opt.isEmpty()) {
        return;
      }
      NodeGroup group = opt.get();
      group.resolve().thenAccept(nodes -> {
        for (Node<?> node : nodes) {
          this.nodes.put(node.getNodeId(), node);
          this.edges.addAll(node.getEdges());
        }
        Player p = player.unwrap();
        armorstandHandler.showNodes(nodes, p);
        armorstandHandler.showEdges(edges, p);
      });
    });
  }

  public void hideArmorStands(PathPlayer<Player> player) {
    Player p = player.unwrap();
    armorstandHandler.hideNodes(nodes.values(), p);
    armorstandHandler.hideEdges(edges, p);
    nodes.clear();
    edges.clear();
  }

  public boolean isEditing(PathPlayer<Player> player) {
    return editingPlayers.containsKey(player);
  }

  public boolean isEditing(Player player) {
    return isEditing(PathPlugin.wrap(player));
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

      Map<Edge, Boolean> undirected = new HashMap<>();
      for (Edge edge : edges) {
        Edge contained = edges.stream()
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

            Vector a = VectorUtils.toBukkit(start.getLocation().asVector());
            Vector b = VectorUtils.toBukkit(end.getLocation().asVector());
            double dist = a.distance(b);

            for (float i = 0; i < dist; i += particleDistance) {
              Color c = directed ? LerpUtils.lerp(colorFrom, colorTo, i / dist) : colorFrom;

              ParticleBuilder builder = particles.computeIfAbsent(c,
                  k -> new ParticleBuilder(ParticleEffect.REDSTONE).setColor(k));
              packets.computeIfAbsent(c, x -> new ArrayList<>()).add(builder.setLocation(
                  LerpUtils.lerp(a, b, i / dist)
                      .toLocation(Bukkit.getWorld(start.getLocation().getWorld().getUniqueId()))).toPacket());
            }
          });
        });
      }
      for (var entry : packets.entrySet()) {
        editModeTasks.add(TaskManager.startSuppliedTask(entry.getValue(), tickDelay,
            () -> editingPlayers.keySet().stream()
                .map(PathPlayer::unwrap)
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
        editingPlayers.keySet().stream().map(PathPlayer::unwrap).forEach(player -> {
          armorstandHandler.showNode(node, player);
        });
        this.nodes.put(node.getNodeId(), node);
      });
    });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onNodeGroupRemove(NodeGroupRemovedEvent event) {
    Collection<Groupable<?>> groupables = event.getGroupables();
    for (PathPlayer<Player> pp : editingPlayers.keySet()) {
      for (Groupable<?> node : groupables) {
        armorstandHandler.updateNodeHead(pp.unwrap(), node);
      }
    }
  }

  @EventHandler
  public void onEdgeCreated(EdgesCreateEvent.Post event) {
    // TODO check if in current group obv
    editingPlayers.keySet().stream().map(PathPlayer::unwrap).forEach(player -> {
      armorstandHandler.showEdges(event.getEdges(), player);
    });
    updateEditModeParticles();
  }

  @EventHandler
  public void onNodesDeleted(NodesDeletedEvent event) {

    editingPlayers.keySet().stream().map(PathPlayer::unwrap).forEach(player -> {
      for (Node<?> node : event.getNodes()) {
        this.nodes.remove(node.getNodeId());
      }
      armorstandHandler.hideNodes(event.getNodes(), player);
    });
    updateEditModeParticles();
  }

  @EventHandler
  public void onEdgesDeleted(EdgesDeleteEvent.Post event) {
    editingPlayers.keySet().stream().map(PathPlayer::unwrap).forEach(player ->
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
      for (PathPlayer<Player> pp : editingPlayers.keySet()) {
        Player player = pp.unwrap();
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
