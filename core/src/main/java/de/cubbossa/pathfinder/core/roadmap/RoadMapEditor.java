package de.cubbossa.pathfinder.core.roadmap;

import com.google.common.collect.Lists;
import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.events.node.EdgesCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.EdgesDeletedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeCreatedEvent;
import de.cubbossa.pathfinder.core.events.node.NodeTeleportEvent;
import de.cubbossa.pathfinder.core.events.node.NodesDeletedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupAssignedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupRemovedEvent;
import de.cubbossa.pathfinder.core.events.nodegroup.NodeGroupSearchTermsChangedEvent;
import de.cubbossa.pathfinder.core.events.roadmap.RoadMapDeletedEvent;
import de.cubbossa.pathfinder.core.menu.EditModeMenu;
import de.cubbossa.pathfinder.core.node.Edge;
import de.cubbossa.pathfinder.core.node.Groupable;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.node.NodeTypeHandler;
import de.cubbossa.pathfinder.util.ClientNodeHandler;
import de.cubbossa.pathfinder.util.LerpUtils;
import java.awt.*;
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
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
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
public class RoadMapEditor implements Keyed, Listener {

  private final NamespacedKey key;
  private final RoadMap roadMap;
  private final ClientNodeHandler armorstandHandler;

  private final Map<UUID, BottomInventoryMenu> editingPlayers;
  private final Map<UUID, GameMode> preservedGameModes;

  private final Collection<Integer> editModeTasks;

  private float particleDistance = .3f;
  private int tickDelay = 5;
  private Color colorFrom = new Color(255, 0, 0);
  private Color colorTo = new Color(0, 127, 255);

  public RoadMapEditor(RoadMap roadMap) {
    this.key = roadMap.getKey();
    this.roadMap = roadMap;

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

      BottomInventoryMenu menu = new EditModeMenu(roadMap,
          NodeTypeHandler.getInstance().getTypes().values()).createHotbarMenu(this, player);
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
    armorstandHandler.showNodes(roadMap.getNodes(), player);
    armorstandHandler.showEdges(roadMap.getEdges(), player);
  }

  public void hideArmorStands(Player player) {
    armorstandHandler.hideNodes(roadMap.getNodes(), player);
    armorstandHandler.hideEdges(roadMap.getEdges(), player);
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

      Map<Edge, Boolean> undirected = new HashMap<>();
      for (Edge edge : roadMap.getEdges()) {
        Edge contained = roadMap.getEdge(edge.getEnd(), edge.getStart());
        if (contained != null && undirected.containsKey(contained)) {
          undirected.put(contained, true);
        } else {
          undirected.put(edge, false);
        }
      }

      Map<Color, List<Object>> packets = new HashMap<>();
      Map<Color, ParticleBuilder> particles = new HashMap<>();

      for (var entry : undirected.entrySet()) {
        if (!Objects.equals(entry.getKey().getStart().getLocation().getWorld(),
            entry.getKey().getEnd().getLocation().getWorld())) {
          continue;
        }
        boolean directed = !entry.getValue();

        Vector a = entry.getKey().getStart().getLocation().toVector();
        Vector b = entry.getKey().getEnd().getLocation().toVector();
        double dist = a.distance(b);

        for (float i = 0; i < dist; i += particleDistance) {
          Color c = directed ? LerpUtils.lerp(colorFrom, colorTo, i / dist) : colorFrom;

          ParticleBuilder builder = particles.computeIfAbsent(c,
              k -> new ParticleBuilder(ParticleEffect.REDSTONE).setColor(k));
          packets.computeIfAbsent(c, x -> new ArrayList<>()).add(builder.setLocation(
              LerpUtils.lerp(a, b, i / dist)
                  .toLocation(entry.getKey().getStart().getLocation().getWorld())).toPacket());
        }
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
    editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player ->
        event.getGroupables().stream()
            .map(groupable -> (Node) groupable)
            .forEach(node -> {
              armorstandHandler.updateNodeHead(player, node);
              armorstandHandler.updateNodeName(player, node);
            }));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onNodeGroupRemove(NodeGroupRemovedEvent event) {
    Collection<Groupable> groupables = event.getGroupables();
    for (UUID uuid : editingPlayers.keySet()) {
      Player player = Bukkit.getPlayer(uuid);
      for (Groupable node : groupables) {
        armorstandHandler.updateNodeHead(player, node);
        armorstandHandler.updateNodeName(player, node);
      }
    }
  }

  @EventHandler
  public void onNodeGroupSeachTermsChanged(NodeGroupSearchTermsChangedEvent event) {
    Collection<? extends Node> nodes = event.getGroup();
    editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player ->
        nodes.forEach(node -> armorstandHandler.updateNodeName(player, node)));
  }

  @EventHandler
  public void onNodeCreated(NodeCreatedEvent event) {
    editingPlayers.keySet().stream().map(Bukkit::getPlayer)
        .forEach(player -> armorstandHandler.showNode(event.getNode(), player));
  }

  @EventHandler
  public void onEdgeCreated(EdgesCreatedEvent event) {
    Collection<Edge> edges = new HashSet<>();
    for (Edge edge : event.getEdges()) {
      Edge otherDirection = roadMap.getEdge(edge.getEnd(), edge.getStart());
      if (otherDirection != null && !edges.contains(otherDirection)) {
        edges.add(otherDirection);
      }
    }
    editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player -> {
      armorstandHandler.showEdges(event.getEdges(), player);
    });
    updateEditModeParticles();
  }

  @EventHandler
  public void onNodesDeleted(NodesDeletedEvent event) {
    // No need to remove edges here, they are being removed by the roadmap
    // beforehand with the according EdgeDeletedEvent

    editingPlayers.keySet().stream().map(Bukkit::getPlayer).forEach(player -> {
      armorstandHandler.hideNodes(event.getNodes(), player);
    });
    updateEditModeParticles();
  }

  @EventHandler
  public void onEdgesDeleted(EdgesDeletedEvent event) {
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
      editingPlayers.keySet().stream()
          .map(Bukkit::getPlayer)
          .filter(Objects::nonNull)
          .forEach(player -> {
            for (Node node : event.getNodes()) {
              armorstandHandler.updateNodePosition(node, player, player.getLocation(), true);
            }
          });
      updateEditModeParticles();
    }, 1);
  }

  @EventHandler
  public void onDelete(RoadMapDeletedEvent event) {
    if (event.getRoadMap().getKey().equals(roadMap.getKey())) {
      dispose();
    }
  }
}
